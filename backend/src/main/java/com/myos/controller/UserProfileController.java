package com.myos.controller;

import com.myos.dto.UserProfileResponseDTO;
import com.myos.dto.UserProfileUpdateRequestDTO;
import com.myos.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing REST APIs for User Profile operations.
 *
 * @RestController — Combines @Controller and @ResponseBody.
 * This tells Spring that every method returns data (JSON) directly, rather than a view name.
 * @RequestMapping — Base URL for all endpoints in this controller.
 */
@RestController
@RequestMapping("/api/v1/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Constructor injection for the UserProfileService.
     */
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * GET /api/v1/profile
     * Retrieves the profile of the currently authenticated user.
     *
     * @return 200 OK with the user profile DTO.
     */
    @GetMapping
    public ResponseEntity<UserProfileResponseDTO> getProfile() {
        String email = getAuthenticatedUserEmail();
        UserProfileResponseDTO profile = userProfileService.getProfileForCurrentUser(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/v1/profile
     * Updates the profile of the currently authenticated user.
     *
     * @RequestBody — Tells Spring to parse the incoming JSON body into the DTO object.
     * @return 200 OK with the updated user profile DTO.
     */
    @PutMapping
    public ResponseEntity<UserProfileResponseDTO> updateProfile(@RequestBody UserProfileUpdateRequestDTO request) {
        String email = getAuthenticatedUserEmail();
        UserProfileResponseDTO updatedProfile = userProfileService.updateProfileForCurrentUser(email, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Helper method to securely extract the current user's email.
     * 
     * WHY SecurityContextHolder?
     * This ensures users can ONLY interact with their own profile. If we accepted a userId
     * via the URL (like /api/v1/profile/{id}), a malicious user could change the ID and
     * read/edit someone else's profile. SecurityContextHolder provides the identity
     * validated by our JWT filter.
     */
    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // In our setup, the JWT subject (username) is the user's email.
    }
}
