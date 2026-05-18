package com.myos.repository;

import com.myos.entity.User;
import com.myos.entity.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserProfileRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserProfileRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    public void testSaveAndFindByUserId() {
        // Given — Create and save a user first
        User user = new User("John Doe", "john@example.com", "password", "ROLE_USER", "{}");
        userRepository.save(user);

        // Given — Create a profile for that user and manage both sides
        UserProfile profile = new UserProfile(user);
        profile.setBio("Passionate developer.");
        profile.setSkills("Java, Spring");
        profile.setGoals("Build MyOS");
        profile.setResumeText("Resume content...");
        user.setUserProfile(profile);
        
        // When — Save the user (it will cascade to the profile due to CascadeType.ALL)
        userRepository.save(user);

        // When — Look it up by user ID
        Optional<UserProfile> foundProfile = userProfileRepository.findByUserId(user.getId());

        // Then — Verify profile was saved and retrieved correctly
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(foundProfile.get().getBio()).isEqualTo("Passionate developer.");
        
        // Verify bidirectional link
        User foundUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(foundUser.getUserProfile()).isNotNull();
        assertThat(foundUser.getUserProfile().getBio()).isEqualTo("Passionate developer.");
    }
}
