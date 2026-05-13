package com.myos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myos.dto.LoginRequest;
import com.myos.dto.RegisterRequest;
import com.myos.entity.User;
import com.myos.repository.TokenRepository;
import com.myos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterAndLoginWithCookies() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("Auth User", "auth@example.com", "password123");

        // 1. Register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("access_token", true))
                .andExpect(cookie().httpOnly("refresh_token", true));

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("auth@example.com", "password123");
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        var accessCookie = loginResult.getResponse().getCookie("access_token");
        var refreshCookie = loginResult.getResponse().getCookie("refresh_token");

        // 3. Access Protected Route using Cookie
        mockMvc.perform(get("/api/test/protected")
                        .cookie(accessCookie))
                .andExpect(status().isNotFound()); // NotFound means authenticated but route missing

        // 4. Refresh Token using Cookie
        mockMvc.perform(post("/api/auth/refresh-token")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"));

        // 5. Logout
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(accessCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0));

        // 6. Access after Logout
        mockMvc.perform(get("/api/test/protected")
                        .cookie(accessCookie))
                .andExpect(status().isUnauthorized());
    }
}
