package com.myos.security;

import com.myos.entity.User;
import com.myos.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import com.myos.service.JwtService;
import com.myos.service.TokenService;

import java.io.IOException;
import java.util.Map;

/**
 * Handles what happens AFTER a successful OAuth2 login.
 *
 * THE FLOW:
 * 1. User logs in via Google/GitHub (handled by Spring Security + CustomOAuth2UserService)
 * 2. On success, Spring calls onAuthenticationSuccess() in this class
 * 3. We generate JWT access + refresh tokens
 * 4. Set tokens as HTTP-only cookies
 * 5. Redirect the user to the frontend application
 *
 * WHAT IS SimpleUrlAuthenticationSuccessHandler?
 * A Spring class that handles post-login redirects. We extend it to add our
 * JWT token generation and cookie-setting logic before redirecting.
 *
 * @Component — Registers as a Spring bean for injection into SecurityConfig.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    /**
     * @Value — Injects the frontend redirect URL from application.yml.
     * After OAuth2 login, we redirect the user to this URL (e.g., http://localhost:3000/dashboard).
     */
    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    /** Constructor injection — Spring provides all three service beans. */
    public OAuth2AuthenticationSuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            TokenService tokenService
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    /**
     * Called by Spring Security after a successful OAuth2 authentication.
     *
     * @Override — Overrides the parent class method to add our custom logic.
     *
     * @param request        the HTTP request
     * @param response       the HTTP response (we set cookies and redirect URL)
     * @param authentication contains the authenticated user's info from the OAuth2 provider
     *
     * @throws IOException      if there's an I/O error during redirect
     * @throws ServletException if the user can't be found in the database
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Guard: if the response is already committed (headers sent), don't try to redirect
        if (response.isCommitted()) {
            return;
        }

        // Cast to OAuth2AuthenticationToken to access OAuth2-specific data
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        // Get the provider name (e.g., "google", "github")
        String registrationId = token.getAuthorizedClientRegistrationId();
        // Get the user's profile attributes from the OAuth2 provider
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        // Extract email from provider-specific attributes
        String email = extractEmail(registrationId, attributes);

        // Find the user by email hash (email column is encrypted)
        String emailHash = EncryptionUtil.hashForLookup(email);
        User user = userRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new ServletException("User not found after OAuth2 authentication"));

        // Generate JWT tokens for the authenticated user
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Revoke old tokens and save the new one (single session enforcement)
        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, accessToken);

        // Set tokens as HTTP-only cookies (secure, not accessible by JavaScript)
        setCookies(response, accessToken, refreshToken);

        // Build the redirect URL (e.g., http://localhost:3000/dashboard)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .build().toUriString();

        // Redirect the browser to the frontend
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Sets JWT tokens as HTTP-only cookies.
     * See AuthService.setCookies() for detailed explanation of cookie properties.
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

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    /**
     * Extracts email from OAuth2 provider attributes.
     * Same logic as CustomOAuth2UserService.extractEmail() — different providers
     * use different attribute names for the user's email.
     */
    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("email");
        } else if ("github".equals(registrationId)) {
            String email = (String) attributes.get("email");
            if (email == null) {
                String login = (String) attributes.get("login");
                email = login + "@github.com";
            }
            return email;
        }
        return null;
    }
}
