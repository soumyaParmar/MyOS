package com.myos.security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for EncryptionUtil.
 *
 * WHAT IS A UNIT TEST?
 * A test that verifies a single unit of code (one class/method) in isolation,
 * without starting the Spring context or connecting to a database.
 * Unit tests are fast — they run in milliseconds.
 *
 * UNIT TEST vs INTEGRATION TEST:
 * - Unit test: Tests one class in isolation (this file). No @SpringBootTest needed.
 * - Integration test: Tests multiple components together (UserRepositoryTests).
 *   Uses @DataJpaTest or @SpringBootTest to start the Spring context.
 */
class EncryptionUtilTest {

    /** A test key shared by all test methods in this class. */
    private static SecretKey testKey;

    /**
     * @BeforeAll — Runs ONCE before all test methods in this class.
     * Used for expensive setup that can be shared (like creating a key).
     *
     * Must be static because JUnit creates the test class instance separately
     * for each test method, but @BeforeAll runs before any instance is created.
     *
     * Compare with:
     * - @BeforeEach — Runs before EACH test method (see EncryptedStringConverterTest)
     * - @AfterAll   — Runs once after all tests
     * - @AfterEach  — Runs after each test
     */
    @BeforeAll
    static void setUp() {
        // Create a deterministic 32-byte test key (bytes 0 through 31)
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        testKey = EncryptionUtil.deriveKey(base64Key);
    }

    /** Verifies that encrypting and then decrypting returns the original value. */
    @Test
    void encryptDecryptRoundTrip() {
        String original = "hello@example.com";
        String encrypted = EncryptionUtil.encrypt(original, testKey);
        String decrypted = EncryptionUtil.decrypt(encrypted, testKey);

        assertThat(decrypted).isEqualTo(original);
    }

    /**
     * Verifies that AES-GCM produces different ciphertext each time
     * (because of the random IV), but both decrypt to the same plaintext.
     *
     * This is important for security — if the same plaintext always produced
     * the same ciphertext, an attacker could identify repeated values.
     */
    @Test
    void encryptProducesDifferentCiphertextEachTime() {
        String plaintext = "same-value";
        String encrypted1 = EncryptionUtil.encrypt(plaintext, testKey);
        String encrypted2 = EncryptionUtil.encrypt(plaintext, testKey);

        // Different IV each time → different ciphertext
        assertThat(encrypted1).isNotEqualTo(encrypted2);

        // But both decrypt to the same plaintext
        assertThat(EncryptionUtil.decrypt(encrypted1, testKey)).isEqualTo(plaintext);
        assertThat(EncryptionUtil.decrypt(encrypted2, testKey)).isEqualTo(plaintext);
    }

    /** Null input should return null (not throw an exception). */
    @Test
    void encryptNullReturnsNull() {
        assertThat(EncryptionUtil.encrypt(null, testKey)).isNull();
    }

    @Test
    void decryptNullReturnsNull() {
        assertThat(EncryptionUtil.decrypt(null, testKey)).isNull();
    }

    /**
     * Verifies that GCM detects tampered ciphertext.
     *
     * GCM includes an authentication tag — if even one byte of the ciphertext
     * is changed, decryption fails. This is the "integrity" guarantee.
     *
     * assertThatThrownBy() — AssertJ's way to test that an exception is thrown.
     * The lambda inside () -> ... is the code that should throw.
     * .isInstanceOf() checks the exception type.
     * .hasMessageContaining() checks the error message.
     */
    @Test
    void tamperedCiphertextThrowsException() {
        String encrypted = EncryptionUtil.encrypt("test-data", testKey);
        byte[] decoded = Base64.getDecoder().decode(encrypted);

        // Flip a byte in the ciphertext portion (after the 12-byte IV)
        decoded[15] ^= 0xFF; // XOR with 0xFF flips all bits in this byte
        String tampered = Base64.getEncoder().encodeToString(decoded);

        assertThatThrownBy(() -> EncryptionUtil.decrypt(tampered, testKey))
                .isInstanceOf(EncryptionUtil.EncryptionException.class)
                .hasMessageContaining("Failed to decrypt");
    }

    /** Verifies that decrypting with the wrong key fails. */
    @Test
    void decryptWithWrongKeyThrowsException() {
        String encrypted = EncryptionUtil.encrypt("test-data", testKey);

        // Create a different key
        byte[] wrongKeyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            wrongKeyBytes[i] = (byte) (i + 100); // Different bytes
        }
        SecretKey wrongKey = EncryptionUtil.deriveKey(
                Base64.getEncoder().encodeToString(wrongKeyBytes));

        assertThatThrownBy(() -> EncryptionUtil.decrypt(encrypted, wrongKey))
                .isInstanceOf(EncryptionUtil.EncryptionException.class);
    }

    /** Verifies that a key shorter than 256 bits (32 bytes) is rejected. */
    @Test
    void deriveKeyRejectsInvalidKeyLength() {
        byte[] shortKey = new byte[16]; // Only 128 bits, need 256
        String base64Short = Base64.getEncoder().encodeToString(shortKey);

        assertThatThrownBy(() -> EncryptionUtil.deriveKey(base64Short))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }

    /** Verifies that SHA-256 hashing is deterministic (same input → same output). */
    @Test
    void hashForLookupProducesDeterministicResult() {
        String email = "user@example.com";
        String hash1 = EncryptionUtil.hashForLookup(email);
        String hash2 = EncryptionUtil.hashForLookup(email);

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // SHA-256 hex digest = 64 characters
    }

    /** Verifies that email hashing is case-insensitive. */
    @Test
    void hashForLookupIsCaseInsensitive() {
        String hash1 = EncryptionUtil.hashForLookup("User@Example.COM");
        String hash2 = EncryptionUtil.hashForLookup("user@example.com");

        assertThat(hash1).isEqualTo(hash2); // Both produce the same hash
    }

    @Test
    void hashForLookupNullReturnsNull() {
        assertThat(EncryptionUtil.hashForLookup(null)).isNull();
    }

    /** Different emails should produce different hashes (collision resistance). */
    @Test
    void hashForLookupDifferentInputsProduceDifferentHashes() {
        String hash1 = EncryptionUtil.hashForLookup("user1@example.com");
        String hash2 = EncryptionUtil.hashForLookup("user2@example.com");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    /** Edge case: empty string should encrypt and decrypt successfully. */
    @Test
    void encryptDecryptEmptyString() {
        String encrypted = EncryptionUtil.encrypt("", testKey);
        String decrypted = EncryptionUtil.decrypt(encrypted, testKey);

        assertThat(decrypted).isEmpty();
    }

    /** Verifies that Unicode characters (non-ASCII) are handled correctly. */
    @Test
    void encryptDecryptUnicodeContent() {
        String original = "用户名 🔐 données personnelles";
        String encrypted = EncryptionUtil.encrypt(original, testKey);
        String decrypted = EncryptionUtil.decrypt(encrypted, testKey);

        assertThat(decrypted).isEqualTo(original);
    }
}
