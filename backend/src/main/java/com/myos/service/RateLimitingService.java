package com.myos.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing rate-limiting "buckets".
 *
 * WHAT IS RATE LIMITING?
 * Rate limiting controls the number of requests a user can make in a given period.
 * It prevents brute-force attacks and server overload.
 *
 * WHAT IS BUCKET4J?
 * A Java library that implements the "Token Bucket" algorithm:
 *   1. Each user/IP gets a "bucket" with a certain number of "tokens".
 *   2. Every request "consumes" one token.
 *   3. If the bucket is empty, the request is rejected (429 Too Many Requests).
 *   4. Tokens "refill" at a steady rate over time.
 *
 * @Service — Marks this as a Spring-managed business logic bean.
 */
@Service
public class RateLimitingService {

    /**
     * Map to store buckets in memory.
     * Key: Client identifier (IP address or User ID)
     * Value: The Token Bucket associated with that identifier
     *
     * ConcurrentHashMap is used because multiple threads (requests) will access this
     * map simultaneously, and we need it to be thread-safe.
     */
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    /**
     * Resolves the rate-limit bucket for authentication endpoints (login, register).
     * These have strict limits to prevent brute-force attacks.
     *
     * Limit: 5 requests per minute.
     * Refill: 5 tokens added every 1 minute.
     *
     * @param key unique identifier for the client (usually the IP address)
     * @return the Bucket for this client
     */
    public Bucket resolveAuthBucket(String key) {
        return authBuckets.computeIfAbsent(key, k -> createAuthBucket());
    }

    /**
     * Resolves the rate-limit bucket for general API endpoints.
     * These have more relaxed limits for normal usage.
     *
     * Limit: 100 requests per minute.
     * Refill: 100 tokens added every 1 minute.
     *
     * @param key unique identifier for the client (IP address or User ID)
     * @return the Bucket for this client
     */
    public Bucket resolveGeneralBucket(String key) {
        return generalBuckets.computeIfAbsent(key, k -> createGeneralBucket());
    }

    /**
     * Creates a new bucket configuration for auth endpoints.
     *
     * Bandwidth.builder() — Defines the "capacity" and "refill rate".
     * capacity(5) — The bucket can hold at most 5 tokens.
     * refillGreedy(5, Duration.ofMinutes(1)) — Tokens refill gradually over the minute.
     */
    private Bucket createAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillGreedy(5, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    /**
     * Creates a new bucket configuration for general endpoints.
     *
     * capacity(100) — The bucket can hold at most 100 tokens.
     * refillGreedy(100, Duration.ofMinutes(1)) — 100 tokens refilled per minute.
     */
    private Bucket createGeneralBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(100)
                        .refillGreedy(100, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}
