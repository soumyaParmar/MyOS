package com.myos.repository;

import com.myos.entity.User;
import com.myos.entity.UserPreferences;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserPreferencesRepository.
 * 
 * LEARNING NOTE:
 * @DataJpaTest — Focuses only on JPA components. It disables full auto-configuration 
 * and instead applies only configuration relevant to JPA tests.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserPreferencesRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Test
    public void testSaveAndFindByUserId() {
        // Given — Create and save a user
        User user = new User("Jane Doe", "jane@example.com", "password", "ROLE_USER", "{}");
        userRepository.save(user);

        // Given — Create preferences for that user
        UserPreferences prefs = new UserPreferences(user);
        prefs.setJobTypes("Remote, Java Developer");
        prefs.setMonthlyBudgetLimit(1500.0);
        prefs.setEmailNotificationsEnabled(true);
        prefs.setPushNotificationsEnabled(false);
        
        // Link bidirectional
        user.setUserPreferences(prefs);
        
        // When — Save the user (cascades to preferences)
        userRepository.save(user);

        // When — Retrieve by User ID
        Optional<UserPreferences> foundPrefs = userPreferencesRepository.findByUserId(user.getId());

        // Then — Verify data integrity
        assertThat(foundPrefs).isPresent();
        assertThat(foundPrefs.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(foundPrefs.get().getJobTypes()).contains("Java Developer");
        assertThat(foundPrefs.get().getMonthlyBudgetLimit()).isEqualTo(1500.0);
        assertThat(foundPrefs.get().getPushNotificationsEnabled()).isFalse();

        // Verify bidirectional relationship from User side
        User foundUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(foundUser.getUserPreferences()).isNotNull();
        assertThat(foundUser.getUserPreferences().getMonthlyBudgetLimit()).isEqualTo(1500.0);
    }
}
