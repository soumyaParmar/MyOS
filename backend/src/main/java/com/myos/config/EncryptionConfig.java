package com.myos.config;

import com.myos.security.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

/**
 * Spring configuration class for AES-256-GCM encryption.
 *
 * WHAT IS @Configuration?
 * Marks this class as a source of bean definitions. Spring processes it at startup
 * and registers any methods annotated with @Bean as beans in the application context.
 *
 * Think of it as a "factory" that creates and configures objects Spring will manage.
 * Unlike @Component (which registers the class itself as a bean), @Configuration
 * contains @Bean methods that create OTHER objects as beans.
 */
@Configuration
public class EncryptionConfig {

    /**
     * @Value("${app.encryption.key}")
     * Injects a value from application.yml (or environment variables) into this field.
     *
     * HOW IT WORKS:
     * 1. Spring looks for "app.encryption.key" in application.yml
     * 2. application.yml has: key: ${ENCRYPTION_KEY}
     * 3. ${ENCRYPTION_KEY} resolves to the ENCRYPTION_KEY environment variable from .env
     * 4. The final value is a Base64-encoded 32-byte AES key
     *
     * This chain (code → yml → env var → .env file) keeps secrets out of source code.
     */
    @Value("${app.encryption.key}")
    private String base64Key;

    /**
     * Creates and returns a SecretKey bean for AES-256 encryption.
     *
     * WHAT IS @Bean?
     * A method annotated with @Bean tells Spring: "Call this method and register
     * the returned object as a bean." Other classes can then inject this SecretKey
     * using constructor injection or @Autowired.
     *
     * Spring calls this method ONCE at startup, and the same SecretKey instance
     * is reused everywhere (singleton scope by default).
     *
     * @return a validated AES-256 SecretKey derived from the Base64-encoded config value
     */
    @Bean
    public SecretKey encryptionKey() {
        return EncryptionUtil.deriveKey(base64Key);
    }
}
