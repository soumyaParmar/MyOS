package com.myos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A simple health check endpoint to verify the API is running.
 *
 * WHAT IS @RestController?
 * A combination of two annotations:
 *   1. @Controller — Marks this class as a Spring MVC controller (handles HTTP requests).
 *   2. @ResponseBody — Tells Spring to serialize the return value directly as JSON
 *                       (instead of treating it as a view/template name).
 *
 * So @RestController = @Controller + @ResponseBody. It's the standard annotation
 * for REST API controllers that return JSON.
 *
 * WHAT IS A CONTROLLER?
 * In the controller → service → repository architecture, controllers are the
 * entry point for HTTP requests. They:
 *   1. Receive the HTTP request
 *   2. Extract parameters (path variables, request body, headers)
 *   3. Delegate to a service for business logic
 *   4. Return the response
 * Controllers should NOT contain business logic — that goes in services.
 */
@RestController
public class HealthController {

    /**
     * GET /health — Returns {"status": "UP"} to confirm the server is alive.
     *
     * @GetMapping("/health") — Maps HTTP GET requests to this method.
     * When someone visits http://localhost:8080/health, this method runs.
     *
     * Map.of() — Creates an immutable map (Java 9+). The return value is
     * automatically serialized to JSON by Jackson:
     *   {"status": "UP"}
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
