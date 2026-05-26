package com.myos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @Service tells Spring Boot that this class is a service bean housing business utilities.
 * It is managed as a Singleton in the Spring Application Context, allowing it to be injected
 * into other components (like AiModelConfigService) using Dependency Injection.
 *
 * This class provides symmetric AES-GCM 256-bit encryption/decryption
 * to securely store sensitive cloud API credentials (OpenAI/Anthropic) at rest in PostgreSQL.
 */
@Service
public class EncryptionService {

    // AES/GCM/NoPadding transforms the cipher using:
    // - AES block cipher
    // - Galois/Counter Mode (GCM) for authenticated encryption, generating checksum tags to prevent tampering
    // - NoPadding since GCM acts as a stream cipher and doesn't require byte padding
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    
    // GCM authentication tag size in bits (128-bits = 16-bytes). Proves authenticity during decryption.
    private static final int GCM_TAG_LENGTH = 128;
    
    // Recommended unique Initialization Vector (IV) length in bytes (12-bytes = 96-bits).
    private static final int IV_LENGTH = 12;

    // SecretKeySpec is a JCA class that acts as a type-safe symmetric key container.
    private final SecretKeySpec keySpec;

    /**
     * Constructor Injection: Spring Boot automatically injects the symmetric encryption key.
     * We pull the key from system environment properties via {@code @Value}.
     * If not set, it falls back to a safe default Base64-encoded 256-bit key.
     *
     * @param base64Key Base64-encoded 32-byte (256-bit) symmetric key.
     */
    public EncryptionService(@Value("${app.security.encryption-key:FNE0Pp6yrzuWKEWgkPWx6pj4A8ibUeaCy/3KPFsHfz0=}") String base64Key) {
        // Decode the text-based Base64 key into raw binary bytes
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        // Pack raw bytes into an AES secret key spec container
        this.keySpec = new SecretKeySpec(decodedKey, "AES");
    }

    /**
     * Encrypts plaintext using authenticated AES-GCM 256-bit encryption.
     * Generates a unique, random IV (nonce) for every operation.
     * Prepends the 12-byte IV to the ciphertext, and encodes the combined array in Base64.
     *
     * @param plaintext Cleartext API key (e.g. "sk-proj-...")
     * @return Base64-encoded string containing both IV and ciphertext, safe to store in PostgreSQL.
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) return null;
        try {
            // Allocate a clean 12-byte buffer for the IV
            byte[] iv = new byte[IV_LENGTH];
            // SecureRandom creates cryptographically secure, mathematically unpredictable IV nonces.
            // This prevents replay attacks and ensures the same key produces different ciphertexts.
            new SecureRandom().nextBytes(iv);

            // Get a Cipher instance from Java's Cryptography Architecture
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // Compile tag length and IV into GCM specifications
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            // Initialize the engine in ENCRYPT_MODE
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            // Convert String to portable UTF-8 raw bytes, and perform GCM encryption
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Create a combined array to store both the IV (first 12 bytes) and the ciphertext + GCM tag
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            // Encode binary combined array in Base64 text string
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while encrypting sensitive key", e);
        }
    }

    /**
     * Decrypts Base64 combined ciphertext.
     * Decodes the string, separates the 12-byte IV from the ciphertext, and decrypts the remainder.
     * GCM automatically verifies the 16-byte authentication tag to guarantee integrity.
     *
     * @param encryptedText Base64-encoded string (IV + Ciphertext + GCM Tag)
     * @return Decrypted cleartext API key
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) return null;
        try {
            // Decode the database string back to raw bytes
            byte[] combined = Base64.getDecoder().decode(encryptedText);

            // Extract the 12-byte IV from the front of the array
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            // Extract the actual ciphertext + GCM tag from the remainder of the array
            int cipherTextLen = combined.length - IV_LENGTH;
            byte[] encryptedBytes = new byte[cipherTextLen];
            System.arraycopy(combined, iv.length, encryptedBytes, 0, cipherTextLen);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            // Initialize the engine in DECRYPT_MODE
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            // Decrypt ciphertext. Throws AEADBadTagException if the database row was modified or tampered with.
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            // Decode bytes back to Java string
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while decrypting sensitive key", e);
        }
    }
}
