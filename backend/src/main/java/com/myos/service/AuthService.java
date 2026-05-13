package com.myos.service;

import com.myos.security.EncryptionUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myos.dto.AuthenticationResponse;
import com.myos.dto.LoginRequest;
import com.myos.dto.RegisterRequest;
import com.myos.entity.User;
import com.myos.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service containing all authentication business logic:
 * registration, login, token refresh, and logout.
 *
 * This is the "brain" of the auth system — the controller delegates to this service,
 * and this service coordinates between UserRepository, JwtService, and TokenService.
 *
 * @Service — Marks this as a business logic bean in the service layer.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // BCrypt encoder (from SecurityConfig)
    private final JwtService jwtService;            // Creates and validates JWTs
    private final AuthenticationManager authenticationManager; // Spring's auth coordinator
    private final TokenService tokenService;        // Manages token persistence + revocation

    /**
     * Constructor injection with 5 dependencies.
     * Spring resolves all of these from its bean container automatically.
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /**
     * Registers a new user account.
     *
     * FLOW:
     * 1. Create a User entity with the hashed password
     * 2. Save to database (JPA auto-encrypts name, email, preferences via @Convert)
     * 3. Generate JWT access + refresh tokens
     * 4. Save the access token to the database (for revocation support)
     * 5. Set HTTP-only cookies on the response
     * 6. Return tokens in the response body (for non-cookie clients)
     *
     * passwordEncoder.encode() — Hashes the raw password with BCrypt.
     * The hash looks like: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
     * We NEVER store the raw password.
     */
    public AuthenticationResponse register(RegisterRequest request, HttpServletResponse response) {
        var user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()), // Hash the password
                "ROLE_USER",        // Default role for new users
                "{}"                // Default empty preferences JSON
        );
        var savedUser = userRepository.save(user);             // INSERT into database
        var jwtToken = jwtService.generateToken(user);         // Create access token
        var refreshToken = jwtService.generateRefreshToken(user); // Create refresh token
        tokenService.saveUserToken(savedUser, jwtToken);       // Persist token for revocation

        setCookies(response, jwtToken, refreshToken);          // Set HTTP-only cookies

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Authenticates an existing user with email + password.
     *
     * FLOW:
     * 1. AuthenticationManager.authenticate() — Triggers Spring Security's auth process:
     *    a. Calls UserDetailsServiceImpl.loadUserByUsername(email)
     *    b. Compares the submitted password with the stored BCrypt hash
     *    c. If invalid → throws BadCredentialsException (caught by Spring, returns 401)
     * 2. Look up the user by email hash (since email column is encrypted)
     * 3. Generate new tokens
     * 4. Revoke all old tokens (single active session)
     * 5. Save the new token + set cookies
     *
     * UsernamePasswordAuthenticationToken — An "authentication request" object.
     * It holds the credentials (email + password) that Spring Security will verify.
     */
    public AuthenticationResponse login(LoginRequest request, HttpServletResponse response) {
        // Step 1: Authenticate (throws exception if invalid credentials)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // Step 2: Find user by email hash (email column is encrypted, can't search directly)
        String emailHash = EncryptionUtil.hashForLookup(request.getEmail());
        var user = userRepository.findByEmailHash(emailHash)
                .orElseThrow(); // Should never fail if authenticate() succeeded

        // Step 3-5: Generate tokens, revoke old ones, set cookies
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        tokenService.revokeAllUserTokens(user);    // Invalidate old tokens (single session)
        tokenService.saveUserToken(user, jwtToken); // Persist new token

        setCookies(response, jwtToken, refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * This is called "silent renewal" — the frontend calls this endpoint when
     * the access token expires, without requiring the user to log in again.
     *
     * FLOW:
     * 1. Extract refresh token from Authorization header or cookie
     * 2. Extract the user email from the refresh token
     * 3. Find the user by email hash
     * 4. If the refresh token is still valid, generate a new access token
     * 5. Revoke old tokens and persist the new one
     * 6. Write the response as JSON using ObjectMapper
     *
     * ObjectMapper — Jackson's main class for JSON serialization/deserialization.
     * writeValue(outputStream, object) serializes the object to JSON and writes it
     * directly to the HTTP response output stream.
     */
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        // Try to get refresh token from header or cookie
        String refreshToken = null;
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
        } else if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return; // No refresh token provided
        }

        // Extract username and validate
        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            String emailHash = EncryptionUtil.hashForLookup(userEmail);
            var user = this.userRepository.findByEmailHash(emailHash)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                tokenService.revokeAllUserTokens(user);
                tokenService.saveUserToken(user, accessToken);

                setCookies(response, accessToken, refreshToken);

                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                // Write JSON directly to the response body
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    /**
     * Logs out a user by revoking their tokens and clearing cookies.
     *
     * FLOW:
     * 1. Extract the access token from header or cookie
     * 2. Find the user and revoke all their tokens in the database
     * 3. Clear the cookies by setting maxAge to 0 (tells the browser to delete them)
     *
     * maxAge(0) — A cookie with maxAge=0 is immediately deleted by the browser.
     * We set the value to "" (empty) and maxAge to 0 to clear both cookies.
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Extract JWT from header or cookie
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } else if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // Revoke all tokens for the user
        if (jwt != null) {
            var userEmail = jwtService.extractUsername(jwt);
            if (userEmail != null) {
                String emailHash = EncryptionUtil.hashForLookup(userEmail);
                var user = userRepository.findByEmailHash(emailHash).orElse(null);
                if (user != null) {
                    tokenService.revokeAllUserTokens(user);
                }
            }
        }

        // Clear cookies by setting maxAge to 0
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(0) // Delete immediately
                .sameSite("Lax")
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(0) // Delete immediately
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    /**
     * Helper method to set JWT tokens as HTTP-only cookies.
     *
     * WHAT ARE HTTP-ONLY COOKIES?
     * Cookies with httpOnly(true) CANNOT be accessed by JavaScript (document.cookie).
     * This prevents XSS (Cross-Site Scripting) attacks from stealing tokens.
     *
     * COOKIE PROPERTIES:
     * - httpOnly(true)  → JavaScript can't read the cookie (XSS protection)
     * - secure(false)   → Allow HTTP in development (should be true in production for HTTPS)
     * - path("/")       → Cookie is sent with every request to any path
     * - maxAge(seconds) → How long until the browser deletes the cookie
     * - sameSite("Lax") → Cookie is sent with same-site requests and top-level navigations
     *                      (prevents CSRF while allowing OAuth redirects)
     *
     * private — Only callable within this class.
     */
    private void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(3600) // 1 hour
                .sameSite("Lax")
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(7 * 24 * 3600) // 7 days
                .sameSite("Lax")
                .build();

        // Add Set-Cookie headers to the HTTP response
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
