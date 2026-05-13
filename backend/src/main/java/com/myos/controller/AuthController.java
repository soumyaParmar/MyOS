package com.myos.controller;

import com.myos.dto.AuthenticationResponse;
import com.myos.dto.LoginRequest;
import com.myos.dto.RegisterRequest;
import com.myos.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints (register, login, refresh, logout).
 *
 * @RestController — Makes this a REST API controller (returns JSON, not HTML).
 *
 * @RequestMapping("/api/auth") — Sets the base URL path for all endpoints in this controller.
 * All methods will be prefixed with "/api/auth", so:
 *   @PostMapping("/register") → POST /api/auth/register
 *   @PostMapping("/login")    → POST /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * WHAT IS DEPENDENCY INJECTION (DI)?
     * Instead of creating AuthService ourselves (new AuthService(...)), we declare
     * it as a constructor parameter and let Spring create and inject it for us.
     *
     * Spring sees: "AuthController needs an AuthService" → looks in its bean container
     * → finds the AuthService bean (because it's annotated with @Service) → injects it.
     *
     * WHY "final"?
     * The "final" keyword means this field can only be assigned once (in the constructor).
     * This ensures the dependency can't be accidentally changed later — a best practice
     * called "constructor injection with immutable fields."
     */
    private final AuthService authService;

    /**
     * Constructor injection — Spring calls this constructor and passes the AuthService bean.
     * This is the recommended way to inject dependencies (over @Autowired field injection)
     * because:
     *   1. Dependencies are explicit and visible
     *   2. The object is always in a valid state (dependencies are set at construction time)
     *   3. Easier to test (you can pass mock dependencies in unit tests)
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/register — Creates a new user account.
     *
     * @PostMapping — Maps HTTP POST requests to this method.
     * POST is used for creating resources (as opposed to GET for reading).
     *
     * @RequestBody — Tells Spring to read the HTTP request body (JSON) and
     * deserialize it into a RegisterRequest object using Jackson.
     * Example request body: {"name": "John", "email": "john@example.com", "password": "secret"}
     *
     * ResponseEntity<T> — A wrapper that lets you control the HTTP response:
     *   - Status code (200 OK, 201 Created, 400 Bad Request, etc.)
     *   - Response headers
     *   - Response body (the T generic type)
     * ResponseEntity.ok(body) is shorthand for ResponseEntity.status(200).body(body).
     *
     * HttpServletResponse — The raw servlet response object. We need it here to set
     * HTTP-only cookies for the access and refresh tokens.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @jakarta.validation.Valid @RequestBody RegisterRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    /**
     * POST /api/auth/login — Authenticates an existing user.
     * Returns JWT tokens in both the response body AND HTTP-only cookies.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @jakarta.validation.Valid @RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    /**
     * POST /api/auth/refresh-token — Exchanges a valid refresh token for a new access token.
     * This is called "silent renewal" — the frontend calls this when the access token
     * expires, without requiring the user to log in again.
     *
     * HttpServletRequest — The raw servlet request. We read the refresh token from
     * either the Authorization header or the HTTP-only cookie.
     */
    @PostMapping("/refresh-token")
    public void refreshToken(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) throws java.io.IOException {
        authService.refreshToken(request, response);
    }

    /**
     * POST /api/auth/logout — Invalidates the user's tokens and clears cookies.
     *
     * ResponseEntity<Void> — The response has no body (just a 200 status code).
     * ResponseEntity.ok().build() creates an empty 200 OK response.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }
}
