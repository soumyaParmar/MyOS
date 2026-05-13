package com.myos.security;

import com.myos.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.myos.service.JwtService;

import java.io.IOException;

/**
 * Custom filter that intercepts every HTTP request and validates JWT tokens.
 *
 * WHAT IS A FILTER?
 * In Java web apps, filters sit between the client and the servlet (controller).
 * Every HTTP request passes through a chain of filters before reaching your controller.
 * Filters can inspect, modify, or reject requests.
 *
 * WHAT IS OncePerRequestFilter?
 * A Spring base class that guarantees this filter runs exactly ONCE per request.
 * (Some filters can run multiple times during request forwarding — this prevents that.)
 *
 * @Component — Registers this as a Spring bean so it can be injected into SecurityConfig
 * and added to the security filter chain.
 *
 * OVERALL FLOW:
 * HTTP Request → CORS Filter → CSRF Filter → JwtAuthenticationFilter → Controller
 *
 * This filter:
 * 1. Extracts JWT from the Authorization header or access_token cookie
 * 2. Validates the token (signature, expiration, not revoked)
 * 3. If valid, sets the authenticated user in Spring's SecurityContext
 * 4. If invalid/missing, lets the request continue without authentication
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    /** Constructor injection — Spring provides all three dependencies. */
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            TokenRepository tokenRepository
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * The core filter method — runs for every HTTP request.
     *
     * @Override — Implements the abstract method from OncePerRequestFilter.
     *
     * @NonNull — A documentation annotation indicating these parameters are never null.
     * It helps IDEs show warnings if you accidentally pass null.
     *
     * @param request    the incoming HTTP request (contains headers, cookies, etc.)
     * @param response   the outgoing HTTP response (we can modify headers, status, etc.)
     * @param filterChain the chain of remaining filters — call filterChain.doFilter()
     *                    to pass the request to the next filter in the chain.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ========== STEP 1: Extract JWT from the request ==========
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        final String userEmail;

        // Try to get JWT from Authorization header: "Bearer eyJhbGc..."
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Remove "Bearer " prefix (7 characters)
        }
        // If not in header, try to get from HTTP-only cookie named "access_token"
        else if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // ========== STEP 2: If no JWT found, skip authentication ==========
        if (jwt == null) {
            filterChain.doFilter(request, response); // Pass to next filter without auth
            return;
        }

        // ========== STEP 3: Extract the username (email) from the JWT ==========
        userEmail = jwtService.extractUsername(jwt);

        // ========== STEP 4: If user not already authenticated, validate and set context ==========
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load the full user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Check if the token exists in our DB and is not expired/revoked
            // This is the "stateful" check — even if the JWT signature is valid,
            // we reject it if it's been revoked (e.g., after logout)
            var isTokenValid = tokenRepository.findByToken(jwt)
                    .map(t -> !t.isExpired() && !t.isRevoked()) // Transform Token → boolean
                    .orElse(false); // If not found in DB, it's invalid

            // Validate: JWT signature + expiration + DB status
            if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                /**
                 * UsernamePasswordAuthenticationToken — Despite the name, this is a
                 * general-purpose authentication token. We create it with:
                 *   - principal: the UserDetails object (our User entity)
                 *   - credentials: null (we already verified the token, no password needed)
                 *   - authorities: the user's roles/permissions
                 *
                 * setDetails() — Attaches additional request details (IP address, session ID).
                 * WebAuthenticationDetailsSource extracts these from the HTTP request.
                 *
                 * SecurityContextHolder.getContext().setAuthentication() — Sets the
                 * authenticated user for this request. After this, any code can access
                 * the current user via @AuthenticationPrincipal or SecurityContextHolder.
                 */
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,                   // The authenticated user
                        null,                          // No credentials needed (already verified)
                        userDetails.getAuthorities()   // User's roles (ROLE_USER, ROLE_ADMIN, etc.)
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ========== STEP 5: Continue to the next filter / controller ==========
        filterChain.doFilter(request, response);
    }
}
