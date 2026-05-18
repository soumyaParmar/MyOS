package com.myos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myos.dto.UserProfileResponseDTO;
import com.myos.dto.UserProfileUpdateRequestDTO;
import com.myos.service.JwtService;
import com.myos.service.TokenService;
import com.myos.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.myos.repository.TokenRepository;
import com.myos.service.RateLimitingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserProfileController.
 *
 * EDUCATIONAL COMMENTS:
 * - @WebMvcTest: Loads ONLY the web layer (controllers, filters) without loading the full DB or services.
 * - @MockBean: Adds a mock to the Spring ApplicationContext. Replaces the real UserProfileService.
 * - @WithMockUser: Simulates an authenticated user in the SecurityContext for the duration of the test.
 */
@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for isolated controller testing
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;
    
    // We mock JwtService and TokenService because SecurityConfig might try to use them
    @MockBean
    private JwtService jwtService;
    
    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenRepository tokenRepository;

    @MockBean
    private RateLimitingService rateLimitingService;

    private UserProfileResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = UserProfileResponseDTO.builder()
                .bio("Test Bio")
                .skills("Java")
                .goals("Learn")
                .resumeText("Resume")
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGetUserProfile() throws Exception {
        // Arrange
        when(userProfileService.getProfileForCurrentUser("test@example.com")).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Test Bio"))
                .andExpect(jsonPath("$.skills").value("Java"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUpdateUserProfile() throws Exception {
        // Arrange
        UserProfileUpdateRequestDTO request = new UserProfileUpdateRequestDTO(
                "Updated Bio", "Updated Skills", "Updated Goals", "Updated Resume"
        );
        
        UserProfileResponseDTO updatedResponse = UserProfileResponseDTO.builder()
                .bio("Updated Bio")
                .skills("Updated Skills")
                .goals("Updated Goals")
                .resumeText("Updated Resume")
                .build();

        when(userProfileService.updateProfileForCurrentUser(eq("test@example.com"), any(UserProfileUpdateRequestDTO.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Updated Bio"))
                .andExpect(jsonPath("$.skills").value("Updated Skills"));
    }
}
