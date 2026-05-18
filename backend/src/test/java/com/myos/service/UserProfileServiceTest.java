package com.myos.service;

import com.myos.dto.UserProfileResponseDTO;
import com.myos.dto.UserProfileUpdateRequestDTO;
import com.myos.entity.User;
import com.myos.entity.UserProfile;
import com.myos.repository.UserProfileRepository;
import com.myos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserProfileService.
 *
 * EDUCATIONAL COMMENTS:
 * - @ExtendWith(MockitoExtension.class): Initializes Mockito mocks and injects them.
 * - @Mock: Creates a dummy version of a dependency.
 * - @InjectMocks: Injects the mocks into the service being tested.
 */
@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com", "password", "ROLE_USER", "{}");
        testUser.setId(UUID.randomUUID());

        testProfile = new UserProfile(testUser);
        testProfile.setBio("Original Bio");
        testProfile.setSkills("Java");
        testProfile.setGoals("Learn Spring");
        testProfile.setResumeText("Resume text");
    }

    @Test
    void shouldGetExistingProfileForCurrentUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));

        // Act
        UserProfileResponseDTO response = userProfileService.getProfileForCurrentUser("test@example.com");

        // Assert
        assertNotNull(response);
        assertEquals("Original Bio", response.getBio());
        assertEquals("Java", response.getSkills());
        
        // Verify lazy creation was not triggered
        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    void shouldLazyCreateProfileIfItDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty()); // Profile missing!
        
        UserProfile newProfile = new UserProfile(testUser);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(newProfile);

        // Act
        UserProfileResponseDTO response = userProfileService.getProfileForCurrentUser("test@example.com");

        // Assert
        assertNotNull(response);
        assertNull(response.getBio()); // Newly created profile has null fields
        verify(userProfileRepository).save(any(UserProfile.class)); // Verify lazy creation happened
    }

    @Test
    void shouldUpdateProfileForCurrentUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));
        
        UserProfileUpdateRequestDTO request = new UserProfileUpdateRequestDTO(
                "Updated Bio", "Go, Python", "Learn AI", "New Resume"
        );

        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileResponseDTO response = userProfileService.updateProfileForCurrentUser("test@example.com", request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Bio", response.getBio());
        assertEquals("Go, Python", response.getSkills());
        assertEquals("Learn AI", response.getGoals());
        assertEquals("New Resume", response.getResumeText());

        verify(userProfileRepository).save(testProfile);
    }
}
