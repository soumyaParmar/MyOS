package com.myos.service;

import com.myos.dto.AuthenticationResponse;
import com.myos.dto.LoginRequest;
import com.myos.dto.RegisterRequest;
import com.myos.entity.User;
import com.myos.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * 
 * EDUCATIONAL COMMENTS:
 * - @ExtendWith(MockitoExtension.class): Initializes Mockito mocks and injects them.
 * - @Mock: Creates a "dummy" version of a dependency. We can program its behavior.
 * - @InjectMocks: Creates the actual instance of the service and injects the @Mocks into it.
 * - MockHttpServletRequest/Response: Provided by Spring Test to simulate HTTP objects without a server.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com", "encodedPassword", "ROLE_USER", "{}");
        // Manually set ID if needed (though UUID might be generated or mocked)
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("Test User", "test@example.com", "password");
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        // Act
        AuthenticationResponse authResponse = authService.register(registerRequest, response);

        // Assert
        assertNotNull(authResponse);
        assertEquals("accessToken", authResponse.getAccessToken());
        assertEquals("refreshToken", authResponse.getRefreshToken());
        
        // Verify Set-Cookie headers
        assertTrue(response.getHeaderNames().contains(HttpHeaders.SET_COOKIE));
        assertEquals(2, response.getHeaders(HttpHeaders.SET_COOKIE).size());
        
        verify(userRepository).save(any(User.class));
        verify(tokenService).saveUserToken(eq(testUser), eq("accessToken"));
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken");

        // Act
        AuthenticationResponse authResponse = authService.login(loginRequest, response);

        // Assert
        assertNotNull(authResponse);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).revokeAllUserTokens(testUser);
        verify(tokenService).saveUserToken(testUser, "accessToken");
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws IOException {
        // Arrange
        request.setCookies(new Cookie("refresh_token", "validRefreshToken"));
        when(jwtService.extractUsername("validRefreshToken")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid("validRefreshToken", testUser)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("newAccessToken");

        // Act
        authService.refreshToken(request, response);

        // Assert
        verify(tokenService).revokeAllUserTokens(testUser);
        verify(tokenService).saveUserToken(testUser, "newAccessToken");
        assertTrue(response.getContentAsString().contains("newAccessToken"));
    }

    @Test
    void shouldLogoutSuccessfully() {
        // Arrange
        request.setCookies(new Cookie("access_token", "validAccessToken"));
        when(jwtService.extractUsername("validAccessToken")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        authService.logout(request, response);

        // Assert
        verify(tokenService).revokeAllUserTokens(testUser);
        // Verify cookies cleared (maxAge=0)
        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookie);
        assertTrue(setCookie.contains("Max-Age=0"));
    }
}
