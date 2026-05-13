package com.myos.dto;

/**
 * DTO for login requests. Carries the user's email and password from the
 * HTTP request body into the service layer.
 *
 * When a client sends POST /api/auth/login with JSON:
 *   { "email": "user@example.com", "password": "secret123" }
 *
 * Spring (via Jackson) automatically converts that JSON into this LoginRequest object.
 * This is called "deserialization" (JSON → Java object).
 *
 * The @RequestBody annotation in the controller triggers this conversion.
 */
public class LoginRequest {

    /** The user's email address, used as the login username. */
    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;

    @jakarta.validation.constraints.NotBlank(message = "Password is required")
    private String password;

    /** No-arg constructor required by Jackson for deserialization. */
    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters — Jackson calls these during JSON deserialization/serialization.
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
