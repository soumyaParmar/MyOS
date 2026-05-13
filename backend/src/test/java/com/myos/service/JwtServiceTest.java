package com.myos.service;

import com.myos.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 * 
 * EDUCATIONAL COMMENTS:
 * - We are not using @SpringBootTest because we want to test ONLY JwtService in isolation.
 * - @BeforeEach: Runs before every @Test method. Perfect for setting up fresh data.
 * - ReflectionTestUtils: A Spring utility that lets us inject values into private fields
 *   (like @Value fields) without needing a full Spring context.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    // A valid Base64 encoded 256-bit key for testing
    private final String testSecret = "4qVq6f8S7e9A1c2D3f4G5h6J7k8M9n0P1r2S3t4V5w6X7y8Z9a0B1c2D3f4G5h6J";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Injecting @Value fields manually since we aren't using @SpringBootTest
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days

        testUser = new User("Test User", "test@example.com", "password", "ROLE_USER", "{}");
    }

    @Test
    void shouldGenerateValidAccessToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        // Act
        String token = jwtService.generateRefreshToken(testUser);

        // Assert
        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    void shouldIdentifyInvalidTokenForDifferentUser() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        User differentUser = new User();
        differentUser.setEmail("other@example.com");

        // Act & Assert
        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void shouldExtractExpirationDate() {
        // Act
        String token = jwtService.generateToken(testUser);
        
        // Assert
        assertNotNull(jwtService.extractClaim(token, io.jsonwebtoken.Claims::getExpiration));
    }
}
