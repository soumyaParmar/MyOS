package com.myos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO (Data Transfer Object) for authentication responses.
 *
 * WHAT IS A DTO?
 * A DTO is a simple object used to transfer data between layers (e.g., from the
 * service layer to the controller, then to the client as JSON). DTOs are different
 * from entities — entities map to database tables, while DTOs shape the API response.
 *
 * WHY NOT RETURN THE USER ENTITY DIRECTLY?
 * 1. Security: The User entity has sensitive fields (password hash, emailHash) that
 *    should never be exposed in API responses.
 * 2. Decoupling: If you change the database schema, the API response stays the same.
 * 3. Control: You choose exactly what fields the client sees.
 */
public class AuthenticationResponse {

    /**
     * @JsonProperty("access_token")
     * Tells Jackson (the JSON library Spring uses) to serialize this field as
     * "access_token" in JSON instead of "accessToken". This follows the OAuth2
     * convention of using snake_case for token responses.
     *
     * Java uses camelCase (accessToken), but APIs often use snake_case (access_token).
     * @JsonProperty bridges this difference.
     */
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    /** No-arg constructor needed by Jackson for JSON deserialization (JSON → Java object). */
    public AuthenticationResponse() {
    }

    public AuthenticationResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters and Setters — Jackson uses these to read/write field values during serialization.
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Static factory method that returns a new Builder instance.
     * See Token.java for a detailed explanation of the Builder pattern.
     */
    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    /** Builder pattern for readable object construction. */
    public static class AuthenticationResponseBuilder {
        private String accessToken;
        private String refreshToken;

        public AuthenticationResponseBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this; // Enables method chaining: builder().accessToken("x").refreshToken("y").build()
        }

        public AuthenticationResponseBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(accessToken, refreshToken);
        }
    }
}
