package com.myos.controller;

import com.myos.dto.UserDto;
import com.myos.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user-related endpoints (e.g., getting current user's profile).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * GET /api/users/me — Returns the currently authenticated user's profile.
     *
     * @AuthenticationPrincipal — A Spring Security annotation that automatically
     * extracts the authenticated user from the SecurityContext and injects it
     * as a method parameter.
     *
     * HOW IT WORKS:
     * 1. JwtAuthenticationFilter extracts the JWT from the request.
     * 2. It loads the User via UserDetailsService and stores it in SecurityContext.
     * 3. @AuthenticationPrincipal pulls that User object out and passes it here.
     *
     * This avoids manually doing:
     *   Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     *   User user = (User) auth.getPrincipal();
     *
     * WHY CONVERT TO UserDto?
     * The User entity has sensitive fields (password, emailHash, etc.).
     * We convert to UserDto to expose only safe fields (id, name, email, roles).
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build(); // 401 Unauthorized
        }
        // Map the entity to a DTO — only expose safe fields
        UserDto userDto = new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );
        return ResponseEntity.ok(userDto);
    }
}
