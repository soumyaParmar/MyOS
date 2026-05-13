package com.myos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for admin-only endpoints.
 * These endpoints require the user to have the ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * GET /api/admin/stats — Returns basic system stats (admin only).
     *
     * @PreAuthorize("hasRole('ADMIN')")
     * This is a method-level security annotation. Before this method executes,
     * Spring Security checks if the authenticated user has the role "ROLE_ADMIN".
     *
     * HOW IT WORKS:
     * 1. Spring Security reads the user's authorities from the SecurityContext
     *    (set by JwtAuthenticationFilter during request processing).
     * 2. hasRole('ADMIN') checks for "ROLE_ADMIN" (Spring adds the "ROLE_" prefix automatically).
     * 3. If the user doesn't have this role, Spring returns 403 Forbidden.
     *
     * This annotation requires @EnableMethodSecurity on the SecurityConfig class.
     *
     * Map.of("key1", value1, "key2", value2) — Creates an immutable map.
     * Jackson serializes it to: {"status": "UP", "systemTime": 1234567890, "message": "..."}
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "systemTime", System.currentTimeMillis(),
                "message", "Admin access granted"
        ));
    }
}
