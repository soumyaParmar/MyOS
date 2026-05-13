package com.myos.service;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitingService.
 * 
 * EDUCATIONAL COMMENTS:
 * - We are testing the "Token Bucket" logic.
 * - tryConsume(n): Attempts to take n tokens from the bucket. Returns true if successful.
 * - getAvailableTokens(): Returns the current number of tokens in the bucket.
 */
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    void shouldCreateAuthBucketWithCorrectCapacity() {
        // Arrange
        String ip = "192.168.1.1";

        // Act
        Bucket bucket = rateLimitingService.resolveAuthBucket(ip);

        // Assert
        assertNotNull(bucket);
        assertEquals(5, bucket.getAvailableTokens());
    }

    @Test
    void shouldCreateGeneralBucketWithCorrectCapacity() {
        // Arrange
        String ip = "192.168.1.1";

        // Act
        Bucket bucket = rateLimitingService.resolveGeneralBucket(ip);

        // Assert
        assertNotNull(bucket);
        assertEquals(100, bucket.getAvailableTokens());
    }

    @Test
    void shouldProvideDifferentBucketsForDifferentKeys() {
        // Arrange
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";

        // Act
        Bucket bucket1 = rateLimitingService.resolveAuthBucket(ip1);
        Bucket bucket2 = rateLimitingService.resolveAuthBucket(ip2);

        // Assert
        assertNotSame(bucket1, bucket2);
    }

    @Test
    void shouldProvideSameBucketForSameKey() {
        // Arrange
        String ip = "192.168.1.1";

        // Act
        Bucket bucket1 = rateLimitingService.resolveAuthBucket(ip);
        Bucket bucket2 = rateLimitingService.resolveAuthBucket(ip);

        // Assert
        assertSame(bucket1, bucket2);
    }

    @Test
    void shouldConsumeTokensAndDenyWhenEmpty() {
        // Arrange
        String ip = "127.0.0.1";
        Bucket bucket = rateLimitingService.resolveAuthBucket(ip);

        // Act & Assert
        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume(1), "Should consume token " + (i + 1));
        }
        
        assertFalse(bucket.tryConsume(1), "Should be empty and deny the 6th request");
    }
}
