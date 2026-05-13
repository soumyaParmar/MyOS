package com.myos.service;

import com.myos.security.EncryptionUtil;

import com.myos.entity.User;
import com.myos.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Custom service for processing OAuth2 user data from providers (Google, GitHub).
 *
 * WHAT IS OAuth2?
 * OAuth2 is a protocol that lets users log in using their existing accounts
 * from other services (Google, GitHub, etc.) instead of creating a new password.
 *
 * THE OAuth2 LOGIN FLOW:
 * 1. User clicks "Login with Google" → Redirected to Google's login page
 * 2. User logs in with Google → Google redirects back to our app with an auth code
 * 3. Spring exchanges the auth code for an access token (server-to-server)
 * 4. Spring calls THIS service's loadUser() to fetch the user's profile from Google
 * 5. We create/update a User entity in our database
 * 6. OAuth2AuthenticationSuccessHandler generates JWTs and redirects to frontend
 *
 * WHAT DOES THIS CLASS DO?
 * It extends DefaultOAuth2UserService (which handles the HTTP call to the provider).
 * After getting the user's profile (email, name, etc.), it either:
 *   - Creates a new User in our database (first-time login)
 *   - Updates the existing User (returning user)
 *
 * @Service — Registers as a Spring bean so SecurityConfig can reference it.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /** Constructor injection — Spring provides the UserRepository bean. */
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Called by Spring Security after successfully obtaining an OAuth2 access token.
     *
     * @Override — Overrides the parent's loadUser() to add our custom user processing.
     *
     * super.loadUser(userRequest) — Calls the parent class method to make the HTTP
     * request to the OAuth2 provider's userinfo endpoint and get the user's profile.
     *
     * @param userRequest contains the OAuth2 access token and client registration info
     * @return the OAuth2User with profile attributes (email, name, etc.)
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Call parent to fetch user info from the OAuth2 provider
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // Get the provider name (e.g., "google", "github")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        return processOAuth2User(registrationId, oAuth2User);
    }

    /**
     * Creates or updates a User in our database based on OAuth2 profile data.
     *
     * @param registrationId the OAuth2 provider name ("google" or "github")
     * @param oAuth2User     the user profile returned by the provider
     * @return the same OAuth2User (passed through for Spring Security)
     */
    private OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        // Extract user attributes from the OAuth2 provider response
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(registrationId, attributes);
        String name = (String) attributes.get("name");
        String providerId = extractProviderId(registrationId, attributes);

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Look up user by email hash (email is encrypted, so we search by hash)
        String emailHash = EncryptionUtil.hashForLookup(email);
        Optional<User> userOptional = userRepository.findByEmailHash(emailHash);
        User user;

        if (userOptional.isPresent()) {
            // User already exists — update provider info if missing
            // (e.g., user signed up with password first, now linking Google)
            user = userOptional.get();
            if (user.getProvider() == null) {
                user.setProvider(registrationId);
                user.setProviderId(providerId);
                userRepository.save(user); // UPDATE existing row
            }
        } else {
            // New user — create an account from OAuth2 data
            user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : email.split("@")[0]); // Fallback: use email prefix as name
            user.setProvider(registrationId);    // "google" or "github"
            user.setProviderId(providerId);      // Provider's unique ID for this user
            user.setRoles("ROLE_USER");           // Default role
            userRepository.save(user); // INSERT new row (no password — OAuth2 users don't need one)
        }

        return oAuth2User; // Return to Spring Security for further processing
    }

    /**
     * Extracts the email address from OAuth2 provider attributes.
     * Each provider has a different attribute name for the email.
     *
     * Google: attributes.get("email")
     * GitHub: attributes.get("email") — may be null if the email is private,
     *         so we fall back to username@github.com
     *
     * TYPE CASTING: (String) attributes.get("email")
     * The attributes map is Map<String, Object>. Since Object could be anything,
     * we cast it to String because we know the email is a string.
     */
    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("email");
        } else if ("github".equals(registrationId)) {
            // GitHub might return null email if not public, providing fallback
            String email = (String) attributes.get("email");
            if (email == null) {
                String login = (String) attributes.get("login");
                email = login + "@github.com";
            }
            return email;
        }
        return null;
    }

    /**
     * Extracts the provider's unique user ID from OAuth2 attributes.
     *
     * Google uses "sub" (subject) — a unique string like "118234523452345234523"
     * GitHub uses "id" — a numeric ID like 12345678
     *
     * String.valueOf() — Converts any object to its string representation.
     * We use it for GitHub's numeric ID: String.valueOf(12345678) → "12345678"
     */
    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return (String) attributes.get("sub");
        } else if ("github".equals(registrationId)) {
            return String.valueOf(attributes.get("id"));
        }
        return null;
    }
}
