package com.myos.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * JPA {@link AttributeConverter} that transparently encrypts entity fields on write
 * and decrypts on read using AES-256-GCM.
 *
 * WHAT IS AN AttributeConverter?
 * JPA's way of transforming field values between Java and the database:
 *   - convertToDatabaseColumn(): Java → DB (encrypt before saving)
 *   - convertToEntityAttribute(): DB → Java (decrypt after loading)
 *
 * This is called "transparent encryption" because the rest of the application
 * works with plaintext — the encryption/decryption happens automatically.
 *
 * USAGE: Add this annotation to any entity field:
 *   @Convert(converter = EncryptedStringConverter.class)
 *   private String email;
 *
 * THE DI PROBLEM:
 * JPA creates AttributeConverters using "new EncryptedStringConverter()" — it doesn't
 * use Spring's dependency injection. So we can't inject the SecretKey bean directly.
 *
 * THE SOLUTION (Static Holder Pattern):
 * 1. We also make this class a @Component, so Spring creates one instance WITH the key
 * 2. The Spring-managed constructor stores the key in a STATIC field
 * 3. When JPA creates its own instances (via no-arg constructor), they read the static key
 * This bridges Spring DI and JPA's converter instantiation.
 *
 * @Component — Makes this a Spring-managed bean (so the SecretKey can be injected).
 * @Converter — Marks this as a JPA converter (so it can be used with @Convert).
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    /**
     * Static field holding the encryption key.
     * "static" means this field belongs to the CLASS, not to any instance.
     * All instances share the same key — set once by the Spring-managed instance.
     */
    private static SecretKey secretKey;

    /**
     * Constructor called by Spring (with dependency injection).
     *
     * @param encryptionKey the AES-256 SecretKey bean from EncryptionConfig
     *
     * This sets the static field so that JPA-created instances (via the no-arg constructor)
     * can also access the key. This runs once at application startup.
     */
    public EncryptedStringConverter(SecretKey encryptionKey) {
        EncryptedStringConverter.secretKey = encryptionKey;
    }

    /**
     * No-arg constructor required by JPA.
     *
     * JPA creates converter instances via reflection using this constructor.
     * The static secretKey will already be set by the Spring-managed instance above.
     * If Spring hasn't initialized yet, secretKey will be null (shouldn't happen
     * in normal operation since Spring initializes before JPA processes entities).
     */
    public EncryptedStringConverter() {
        // JPA instantiation — key is available via the static holder
    }

    /**
     * Called by JPA BEFORE saving to database.
     * Converts plaintext → AES-256-GCM ciphertext (Base64-encoded).
     *
     * Example: "john@example.com" → "dGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVk..."
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return EncryptionUtil.encrypt(attribute, secretKey);
    }

    /**
     * Called by JPA AFTER loading from database.
     * Converts AES-256-GCM ciphertext → plaintext.
     *
     * Example: "dGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVk..." → "john@example.com"
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EncryptionUtil.decrypt(dbData, secretKey);
    }
}
