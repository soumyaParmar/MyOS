package com.myos.service;

import com.myos.dto.UserProfileResponseDTO;
import com.myos.dto.UserProfileUpdateRequestDTO;
import com.myos.entity.User;
import com.myos.entity.UserProfile;
import com.myos.repository.UserProfileRepository;
import com.myos.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Service handling business logic for User Profiles.
 *
 * @Service — Marks this class as a Spring-managed service bean.
 * This is where we put our core business rules and orchestrate database calls.
 */
@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    /**
     * Constructor injection for our dependencies.
     * Spring automatically provides the repository instances at runtime.
     */
    public UserProfileService(UserProfileRepository userProfileRepository, UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the profile for the currently authenticated user.
     * If a profile doesn't exist yet, it creates a blank one (Lazy Creation).
     *
     * @param email The email of the authenticated user.
     * @return UserProfileResponseDTO containing the profile data.
     */
    public UserProfileResponseDTO getProfileForCurrentUser(String email) {
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);
        
        return mapToResponseDTO(profile);
    }

    /**
     * Updates the profile for the currently authenticated user.
     *
     * @param email The email of the authenticated user.
     * @param request The new data to apply to the profile.
     * @return UserProfileResponseDTO containing the updated profile data.
     */
    public UserProfileResponseDTO updateProfileForCurrentUser(String email, UserProfileUpdateRequestDTO request) {
        User user = getUserByEmail(email);
        UserProfile profile = getOrCreateProfile(user);

        // Update the fields
        profile.setBio(request.getBio());
        profile.setSkills(request.getSkills());
        profile.setGoals(request.getGoals());
        profile.setResumeText(request.getResumeText());

        // Save back to the database
        UserProfile savedProfile = userProfileRepository.save(profile);

        return mapToResponseDTO(savedProfile);
    }

    /**
     * Helper method to lookup the user by their email.
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for the given email."));
    }

    /**
     * Helper method to fetch an existing profile or create a new blank one.
     * This ensures the application never crashes due to a missing profile.
     */
    private UserProfile getOrCreateProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // Lazy creation: Create a new profile linked to this user
                    UserProfile newProfile = new UserProfile(user);
                    return userProfileRepository.save(newProfile);
                });
    }

    /**
     * Maps the JPA entity to a clean Data Transfer Object (DTO) to send to the client.
     */
    private UserProfileResponseDTO mapToResponseDTO(UserProfile profile) {
        return UserProfileResponseDTO.builder()
                .bio(profile.getBio())
                .skills(profile.getSkills())
                .goals(profile.getGoals())
                .resumeText(profile.getResumeText())
                .build();
    }
}
