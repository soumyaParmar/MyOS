package com.myos.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EncryptedStringConverter.
 *
 * These tests verify that the JPA AttributeConverter correctly encrypts
 * values before writing to DB and decrypts after reading from DB.
 *
 * Since this is a unit test (no @SpringBootTest), we manually create the
 * converter and provide a test key instead of relying on Spring DI.
 */
class EncryptedStringConverterTest {

    /** The converter instance under test. */
    private EncryptedStringConverter converter;

    /**
     * @BeforeEach — Runs before EACH individual test method.
     * Unlike @BeforeAll (which runs once), this creates a fresh converter
     * for every test to avoid state leaking between tests.
     */
    @BeforeEach
    void setUp() {
        // Create a deterministic test key (32 bytes)
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        SecretKey testKey = EncryptionUtil.deriveKey(
                Base64.getEncoder().encodeToString(keyBytes));

        // Use the Spring-style constructor to set the static key
        // This simulates what Spring does at application startup
        converter = new EncryptedStringConverter(testKey);
    }

    /** Verifies that convertToDatabaseColumn() returns encrypted (non-plaintext) data. */
    @Test
    void convertToDatabaseColumnReturnsEncryptedString() {
        String plaintext = "test@example.com";
        String dbValue = converter.convertToDatabaseColumn(plaintext);

        assertThat(dbValue).isNotNull();
        assertThat(dbValue).isNotEqualTo(plaintext);      // Should be encrypted, not plaintext
        // The output should be valid Base64 (since we encode ciphertext as Base64)
        assertThat(Base64.getDecoder().decode(dbValue)).isNotEmpty();
    }

    /** Verifies the full round-trip: encrypt → decrypt returns original value. */
    @Test
    void convertToEntityAttributeReturnsOriginalPlaintext() {
        String original = "test@example.com";
        String dbValue = converter.convertToDatabaseColumn(original);      // Encrypt
        String restored = converter.convertToEntityAttribute(dbValue);     // Decrypt

        assertThat(restored).isEqualTo(original); // Should get back the original
    }

    /** Null inputs should pass through as null (no encryption/decryption needed). */
    @Test
    void convertToDatabaseColumnNullReturnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttributeNullReturnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    /**
     * Tests that the no-arg constructor (used by JPA) works correctly.
     *
     * JPA creates EncryptedStringConverter via new EncryptedStringConverter()
     * (no-arg constructor). The key is already set in the static field by the
     * Spring-managed instance (created in setUp()). This test verifies
     * that the static holder pattern works.
     */
    @Test
    void noArgConstructorUsesStaticKey() {
        // The Spring constructor (in setUp) already set the static key
        EncryptedStringConverter jpaConverter = new EncryptedStringConverter(); // No-arg (JPA-style)
        String encrypted = jpaConverter.convertToDatabaseColumn("test-value");
        String decrypted = jpaConverter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo("test-value");
    }

    /** Verifies that JSON strings (like user preferences) survive the encrypt/decrypt cycle. */
    @Test
    void roundTripWithJsonPreferences() {
        String json = "{\"theme\":\"dark\",\"language\":\"en\",\"notifications\":true}";
        String encrypted = converter.convertToDatabaseColumn(json);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(json);
    }
}
