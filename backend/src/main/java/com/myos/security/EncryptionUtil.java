package com.myos.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Utility class for AES-256-GCM encryption/decryption and SHA-256 hashing.
 *
 * WHAT IS AES-256-GCM?
 * AES (Advanced Encryption Standard) is the most widely used encryption algorithm.
 * - "256" means 256-bit key length (extremely secure — would take billions of years to crack)
 * - "GCM" (Galois/Counter Mode) is an encryption mode that provides both:
 *   1. Confidentiality — Data is unreadable without the key
 *   2. Integrity — Any tampering with the ciphertext is detected (via the GCM authentication tag)
 *
 * CIPHERTEXT FORMAT:
 * The encrypted output is: Base64(IV || ciphertext || GCM_tag)
 *   - IV (12 bytes): Initialization Vector — random bytes to ensure the same plaintext
 *     encrypts differently each time. This is NOT secret.
 *   - Ciphertext: The encrypted data
 *   - GCM tag (16 bytes): Authentication tag that detects tampering
 *
 * WHAT IS A UTILITY CLASS?
 * A class with only static methods — you call them directly without creating an instance:
 *   EncryptionUtil.encrypt("hello", key)  ✓
 *   new EncryptionUtil().encrypt(...)      ✗ (private constructor prevents this)
 *
 * The "final" keyword on the class means it cannot be subclassed (extended).
 */
public final class EncryptionUtil {

    /** The encryption algorithm: AES in GCM mode with no padding (GCM handles its own padding). */
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";

    /** IV (Initialization Vector) length: 12 bytes is the recommended size for GCM. */
    private static final int GCM_IV_LENGTH_BYTES = 12;

    /** GCM authentication tag length: 128 bits provides strong integrity protection. */
    private static final int GCM_TAG_LENGTH_BITS = 128;

    /**
     * SecureRandom — A cryptographically strong random number generator.
     * Unlike Math.random() (which is predictable), SecureRandom uses the OS's
     * entropy source to generate truly unpredictable random bytes.
     * Used here to generate random IVs for each encryption operation.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Private constructor — prevents instantiation.
     * Utility classes should never be instantiated because all their methods are static.
     */
    private EncryptionUtil() {
        // Utility class — no instantiation
    }

    /**
     * Encrypts plaintext using AES-256-GCM with a random IV.
     *
     * STEPS:
     * 1. Generate a random 12-byte IV (ensures same plaintext → different ciphertext each time)
     * 2. Initialize the AES-GCM cipher with the key and IV
     * 3. Encrypt the plaintext bytes
     * 4. Combine IV + ciphertext into one byte array
     * 5. Base64-encode the result (so it can be stored as a string in the database)
     *
     * WHY RANDOM IV?
     * If we used the same IV every time, encrypting "hello" would always produce the
     * same ciphertext — an attacker could detect repeated values. Random IVs prevent this.
     *
     * @param plaintext the string to encrypt
     * @param key       the 256-bit AES secret key
     * @return Base64-encoded string containing IV + ciphertext + GCM tag, or null if input is null
     */
    public static String encrypt(String plaintext, SecretKey key) {
        if (plaintext == null) {
            return null;
        }
        try {
            // Generate a random IV for this encryption
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(iv); // Fill with random bytes

            // Create and initialize the cipher
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            // Encrypt the plaintext (Java's GCM automatically appends the authentication tag)
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine: IV + ciphertext (ciphertext includes the GCM tag at the end)
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);             // Copy IV
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length); // Copy ciphertext

            // Base64-encode so we can store it as a string in the DB
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-256-GCM ciphertext.
     *
     * STEPS:
     * 1. Base64-decode the input string
     * 2. Extract the IV (first 12 bytes)
     * 3. Extract the ciphertext + tag (remaining bytes)
     * 4. Initialize the cipher in DECRYPT mode with the same key and IV
     * 5. Decrypt and verify the GCM tag (throws exception if tampered)
     *
     * @param ciphertext Base64-encoded string containing IV + ciphertext + GCM tag
     * @param key        the 256-bit AES secret key (must be the same key used for encryption)
     * @return the original plaintext string, or null if input is null
     * @throws EncryptionException if decryption fails (wrong key, tampered data, etc.)
     */
    public static String decrypt(String ciphertext, SecretKey key) {
        if (ciphertext == null) {
            return null;
        }
        try {
            // Decode from Base64 back to raw bytes
            byte[] combined = Base64.getDecoder().decode(ciphertext);

            // Extract the IV (first 12 bytes)
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            // Extract the encrypted bytes (everything after the IV)
            byte[] encryptedBytes = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);

            // Decrypt using the same algorithm, key, and IV
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            // doFinal() decrypts AND verifies the GCM tag — if data was tampered, it throws
            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Derives an AES SecretKey from a Base64-encoded 256-bit key string.
     *
     * WHY Base64?
     * Raw binary keys can't be stored in text config files (.env, application.yml).
     * Base64 encodes binary data as ASCII text, making it safe for config files.
     * A 32-byte key becomes a 44-character Base64 string.
     *
     * @param base64Key Base64-encoded 32-byte key
     * @return the SecretKey for AES-256
     * @throws IllegalArgumentException if key is not exactly 32 bytes after decoding
     */
    public static SecretKey deriveKey(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) { // 32 bytes = 256 bits
            throw new IllegalArgumentException(
                    "Encryption key must be exactly 32 bytes (256 bits). Got: " + keyBytes.length + " bytes");
        }
        // SecretKeySpec wraps raw key bytes into a SecretKey object for use with Cipher
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Produces a deterministic SHA-256 hex digest for indexed lookups.
     *
     * WHAT IS SHA-256?
     * A cryptographic hash function that produces a fixed 256-bit (32-byte) output
     * from any input. Key properties:
     *   1. Deterministic: Same input always produces the same hash
     *   2. One-way: You can't reverse a hash to get the original input
     *   3. Collision-resistant: It's practically impossible for two different inputs
     *      to produce the same hash
     *
     * WHY DO WE NEED THIS?
     * Encrypted email is non-deterministic (random IV each time), so we can't
     * use it in SQL WHERE clauses. SHA-256 is deterministic, so we hash the email
     * and store the hash in an indexed column for fast lookups.
     *
     * toLowerCase() ensures case-insensitive matching:
     *   "User@Example.COM" and "user@example.com" produce the same hash.
     *
     * @param value the plaintext value to hash
     * @return lowercase hex SHA-256 digest (64 characters), or null if input is null
     */
    public static String hashForLookup(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.toLowerCase().getBytes(StandardCharsets.UTF_8));
            // HexFormat.of().formatHex() converts bytes to hex string: [0xAB, 0xCD] → "abcd"
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new EncryptionException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Custom runtime exception for encryption/decryption failures.
     *
     * WHY EXTEND RuntimeException?
     * RuntimeException = unchecked exception (callers don't need try/catch).
     * Encryption failures are unrecoverable — wrapping them in a RuntimeException
     * propagates them up the call stack without cluttering every method with throws.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause); // Pass message and original exception to parent
        }
    }
}
