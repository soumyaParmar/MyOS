package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Java-based Flyway migration that encrypts existing plaintext user data
 * and populates the email_hash column.
 * <p>
 * This migration reads the encryption key from the {@code ENCRYPTION_KEY}
 * environment variable (same key used by the application at runtime).
 * <p>
 * After encrypting all rows, it creates a unique index on email_hash.
 */
public class V7__encrypt_existing_user_data extends BaseJavaMigration {

    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public void migrate(Context context) throws Exception {
        String base64Key = System.getenv("ENCRYPTION_KEY");
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY environment variable is required for migration V7. "
                    + "Set it to a Base64-encoded 32-byte AES key.");
        }

        SecretKey key = deriveKey(base64Key);
        Connection connection = context.getConnection();

        // Read all users
        try (Statement select = connection.createStatement();
             ResultSet rs = select.executeQuery(
                     "SELECT id, name, email, preferences, provider_id FROM users")) {

            PreparedStatement update = connection.prepareStatement(
                    "UPDATE users SET name = ?, email = ?, preferences = ?, "
                    + "provider_id = ?, email_hash = ? WHERE id = ?");

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String preferences = rs.getString("preferences");
                String providerId = rs.getString("provider_id");

                // Skip rows that appear already encrypted (Base64 pattern check)
                if (email != null && looksEncrypted(email)) {
                    continue;
                }

                String encryptedName = encrypt(name, key);
                String encryptedEmail = encrypt(email, key);
                String encryptedPreferences = encrypt(preferences, key);
                String encryptedProviderId = encrypt(providerId, key);
                String emailHash = hashForLookup(email);

                update.setString(1, encryptedName);
                update.setString(2, encryptedEmail);
                update.setString(3, encryptedPreferences);
                update.setString(4, encryptedProviderId);
                update.setString(5, emailHash);
                update.setObject(6, java.util.UUID.fromString(id));
                update.addBatch();
            }
            update.executeBatch();
        }

        // Now that all rows have email_hash populated, create the unique index
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE UNIQUE INDEX idx_users_email_hash ON users(email_hash)");
        }
    }

    /**
     * Heuristic to detect if a value is already encrypted (Base64-encoded ciphertext).
     * Encrypted values are always at least 28 chars (12-byte IV + 16-byte tag, Base64).
     */
    private boolean looksEncrypted(String value) {
        if (value == null || value.length() < 28) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return decoded.length >= GCM_IV_LENGTH_BYTES + 16; // IV + minimum GCM tag
        } catch (IllegalArgumentException e) {
            return false; // Not valid Base64
        }
    }

    // --- Self-contained crypto methods (no dependency on application classes) ---

    private String encrypt(String plaintext, SecretKey key) throws Exception {
        if (plaintext == null) {
            return null;
        }
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    private SecretKey deriveKey(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Encryption key must be 32 bytes (256 bits). Got: " + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    private String hashForLookup(String value) throws Exception {
        if (value == null) {
            return null;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.toLowerCase().getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
