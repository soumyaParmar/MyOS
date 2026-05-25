package com.myos.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import com.myos.security.JwtAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.myos.service.CustomOAuth2UserService;
import com.myos.security.OAuth2AuthenticationSuccessHandler;
import com.myos.security.RateLimitFilter;

/**
 * Central security configuration for the application.
 *
 * WHAT IS @EnableWebSecurity?
 * Enables Spring Security's web security support. Without this, Spring Security
 * won't intercept HTTP requests or enforce authentication/authorization rules.
 *
 * WHAT IS @EnableMethodSecurity?
 * Enables method-level security annotations
 * like @PreAuthorize("hasRole('ADMIN')").
 * Without this, those annotations are ignored.
 *
 * SECURITY FILTER CHAIN:
 * Every HTTP request passes through a chain of security filters before reaching
 * your controller. Think of it as airport security — each filter checks one
 * thing:
 * 1. CORS filter → checks if the request origin is allowed
 * 2. CSRF filter → checks for cross-site request forgery (disabled for APIs)
 * 3. Our JWT filter → extracts and validates the JWT token
 * 4. Authorization filter → checks if the user has permission for the endpoint
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * The frontend URL for CORS configuration.
     * 
     * @Value injects from application.yml: app.frontend-url =
     *        ${FRONTEND_URL:http://localhost:3000}
     */
    @Value("${app.frontend-url}")
    private String frontendUrl;

    /** Our custom JWT filter that validates tokens on every request. */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Spring Security's UserDetailsService — loads user data during authentication.
     */
    private final UserDetailsService userDetailsService;

    /** Custom service that handles OAuth2 user data from Google/GitHub. */
    private final CustomOAuth2UserService oauth2UserService;

    /** Custom handler that runs after successful OAuth2 login. */
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    /** Filter that enforces rate limiting on all requests. */
    private final RateLimitFilter rateLimitFilter;

    /**
     * Constructor injection — Spring injects all five dependencies automatically.
     * All these beans are created by Spring because their classes are annotated
     * with @Component, @Service, or similar stereotypes.
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            CustomOAuth2UserService oauth2UserService,
            OAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
            RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.oauth2UserService = oauth2UserService;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.rateLimitFilter = rateLimitFilter;
    }

    /**
     * Configures the HTTP security filter chain.
     *
     * This is THE most important security configuration method. It defines:
     * - Which endpoints are public vs. protected
     * - How sessions are managed
     * - How errors are handled
     * - Which authentication mechanisms are used (JWT + OAuth2)
     *
     * @Bean — Registers the returned SecurityFilterChain as a Spring bean.
     *
     * @param http the HttpSecurity builder (uses the fluent builder pattern)
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /**
                 * CSRF (Cross-Site Request Forgery) protection — DISABLED.
                 *
                 * WHY DISABLE CSRF?
                 * CSRF protection is for server-rendered apps that use cookies for sessions.
                 * Our API uses JWT tokens (stateless), so CSRF attacks aren't a concern.
                 * If we kept CSRF enabled, every POST/PUT/DELETE request would need a CSRF
                 * token.
                 *
                 * AbstractHttpConfigurer::disable — A method reference (shorthand for csrf ->
                 * csrf.disable()).
                 */
                .csrf(AbstractHttpConfigurer::disable)

                /**
                 * CORS (Cross-Origin Resource Sharing) configuration.
                 *
                 * WHY DO WE NEED CORS?
                 * Browsers block requests from one origin (http://localhost:3000) to another
                 * (http://localhost:8080) by default. CORS headers tell the browser:
                 * "It's OK, I trust requests from this frontend."
                 *
                 * Lambda configuration: cors -> cors.configurationSource(request -> { ... })
                 * This creates an inline CORS config for every request.
                 */
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(java.util.List.of("*")); // Only our frontend - use frontendUrl
                    corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfiguration.setAllowedHeaders(java.util.List.of("*")); // Allow all headers
                    corsConfiguration.setAllowCredentials(false); // Allow cookies to be sent cross-origin use true when
                                                                  // cors is enabled
                    return corsConfiguration;
                }))

                /**
                 * URL-based authorization rules.
                 *
                 * requestMatchers("/path").permitAll() — Anyone can access (no auth needed).
                 * anyRequest().authenticated() — Everything else requires authentication.
                 *
                 * IMPORTANT: Order matters! More specific rules come first.
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll() // Health check — public
                        .requestMatchers("/api/auth/**").permitAll() // Auth endpoints — public (can't require login to
                                                                     // login!)
                        .anyRequest().authenticated() // Everything else — must be logged in
                )

                /**
                 * Session management — STATELESS.
                 *
                 * Spring Security normally creates HTTP sessions (server-side state).
                 * With JWTs, we don't need sessions — the token itself carries all auth info.
                 * STATELESS means Spring won't create or use HTTP sessions at all.
                 */
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /**
                 * Custom error responses for authentication/authorization failures.
                 *
                 * Instead of Spring's default HTML error pages, we return JSON:
                 * - 403 Forbidden: User is authenticated but doesn't have permission
                 * - 401 Unauthorized: User is not authenticated (no valid token)
                 *
                 * These use lambda expressions — short anonymous functions:
                 * (request, response, exception) -> { ... }
                 */
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access Denied\"}");
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.setContentType("application/json");
                            response.getWriter()
                                    .write("{\"error\": \"Unauthorized\", \"message\": \"Authentication Required\"}");
                        }))

                /**
                 * Sets the authentication provider (how passwords are verified).
                 * See authenticationProvider() method below.
                 */
                .authenticationProvider(authenticationProvider())

                /**
                 * Adds our rate limiting filter at the very beginning of the chain.
                 * This ensures we block abusive requests before even trying to parse JWTs.
                 */
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                /**
                 * Adds our JWT filter BEFORE Spring's default username/password filter.
                 *
                 * This means every request hits our JWT filter first:
                 * 1. JwtAuthenticationFilter checks for a JWT in cookies or Authorization
                 * header
                 * 2. If valid, it sets the user in SecurityContext
                 * 3. The request continues through the filter chain
                 */
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                /**
                 * OAuth2 login configuration (Google + GitHub).
                 *
                 * userInfoEndpoint — Configures how user data is fetched from the OAuth2
                 * provider.
                 * We use our CustomOAuth2UserService to create/update users in our DB.
                 *
                 * successHandler — After successful OAuth2 login, our custom handler generates
                 * JWT tokens and redirects to the frontend.
                 */
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                        .successHandler(oauth2SuccessHandler));

        return http.build();
    }

    /**
     * Configures the authentication provider — the component that verifies
     * credentials.
     *
     * DaoAuthenticationProvider:
     * - "Dao" = Data Access Object (it loads users from a database)
     * - Uses our UserDetailsServiceImpl to load the User entity by email
     * - Uses BCryptPasswordEncoder to verify the password hash
     *
     * When a user logs in:
     * 1. DaoAuthenticationProvider calls
     * userDetailsService.loadUserByUsername(email)
     * 2. It gets the User entity (which has the BCrypt password hash)
     * 3. It uses passwordEncoder.matches(rawPassword, hashedPassword) to verify
     * 4. If matched → authentication succeeds. If not → throws
     * BadCredentialsException.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a bean.
     *
     * The AuthenticationManager is Spring Security's main interface for
     * authentication.
     * We need it as a bean so we can inject it into AuthService to call
     * authenticationManager.authenticate() during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Creates a BCryptPasswordEncoder bean.
     *
     * WHAT IS BCRYPT?
     * BCrypt is a password hashing algorithm designed to be intentionally slow.
     * This makes brute-force attacks impractical. It also automatically handles
     * "salting" — adding random data before hashing so identical passwords produce
     * different hashes.
     *
     * Example: "password123" →
     * "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
