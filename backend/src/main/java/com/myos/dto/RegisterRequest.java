package com.myos.dto;

/**
 * DTO for registration requests. Carries the new user's name, email, and password.
 *
 * When a client sends POST /api/auth/register with JSON:
 *   { "name": "John Doe", "email": "john@example.com", "password": "secret123" }
 *
 * Jackson converts it into this RegisterRequest object.
 * The service layer then uses these values to create a new User entity.
 */
public class RegisterRequest {

    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    private String name;

    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;

    @jakarta.validation.constraints.NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    /** No-arg constructor required by Jackson for JSON deserialization. */
    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
