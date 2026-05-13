package com.myos.repository;

import com.myos.entity.User;
import com.myos.security.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository.
 *
 * WHAT IS A TEST CLASS?
 * Test classes verify that your code works correctly. JUnit 5 is the testing framework
 * used by Spring Boot. Each method annotated with @Test is a test case.
 *
 * WHAT IS @DataJpaTest?
 * A Spring Boot test annotation that:
 * 1. Starts a minimal Spring context with only JPA-related beans (entities, repositories)
 * 2. Configures an in-memory database by default (H2)
 * 3. Makes each test transactional (rolls back after each test to keep the DB clean)
 * 4. Disables full auto-configuration (faster than @SpringBootTest)
 *
 * @AutoConfigureTestDatabase(replace = Replace.NONE)
 * Tells Spring NOT to replace the configured database with an in-memory one.
 * We want to test against our actual PostgreSQL database (configured in application-test.yml).
 *
 * @ActiveProfiles("test")
 * Activates the "test" Spring profile, which loads application-test.yml
 * for test-specific configuration (e.g., a separate test database).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTests {

    /**
     * @Autowired — Field injection (Spring injects the bean directly into the field).
     *
     * In test classes, @Autowired field injection is common and acceptable.
     * In production code, constructor injection is preferred (see AuthService.java).
     *
     * Spring creates a real UserRepository proxy backed by the test database.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Tests saving a user and finding them by email hash.
     *
     * TEST STRUCTURE (Given-When-Then / Arrange-Act-Assert):
     * - Given: Set up test data (create a user)
     * - When: Perform the action being tested (save + find)
     * - Then: Verify the result (assertions)
     *
     * @Test — Marks this method as a JUnit test case.
     * JUnit discovers and runs all methods with this annotation.
     */
    @Test
    public void testSaveAndFindByEmailHash() {
        // Given — Create a test user with all required fields
        User user = new User("Test User", "test@example.com", "password", "ROLE_USER", "{\"theme\": \"dark\"}");

        // When — Save the user to the database, then look them up by email hash
        userRepository.save(user);
        // Hash the email (SHA-256) for the lookup — we can't search by encrypted email
        String emailHash = EncryptionUtil.hashForLookup("test@example.com");
        Optional<User> foundUser = userRepository.findByEmailHash(emailHash);

        // Then — Verify the user was found and has correct data
        // assertThat() is from AssertJ — a fluent assertion library
        assertThat(foundUser).isPresent();                                      // User was found
        assertThat(foundUser.get().getName()).isEqualTo("Test User");           // Name matches (decrypted by @Convert)
        assertThat(foundUser.get().getPreferences()).contains("dark");         // Preferences decrypted correctly
    }

    /**
     * Tests the existsByEmailHash() repository method.
     */
    @Test
    public void testExistsByEmailHash() {
        // Given — Save a user
        User user = new User("Another User", "another@example.com", "password", "ROLE_USER", null);
        userRepository.save(user);

        // When — Check existence for both existing and non-existing emails
        String existsHash = EncryptionUtil.hashForLookup("another@example.com");
        String notExistsHash = EncryptionUtil.hashForLookup("nonexistent@example.com");
        boolean exists = userRepository.existsByEmailHash(existsHash);
        boolean notExists = userRepository.existsByEmailHash(notExistsHash);

        // Then — Verify results
        assertThat(exists).isTrue();       // This email exists in the DB
        assertThat(notExists).isFalse();   // This email does NOT exist
    }
}
