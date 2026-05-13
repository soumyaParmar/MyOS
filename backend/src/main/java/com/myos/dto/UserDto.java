package com.myos.dto;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for returning user profile information to the client.
 *
 * WHY A SEPARATE DTO FOR USER?
 * The User entity implements UserDetails and has sensitive fields like password,
 * emailHash, provider info, and internal timestamps. This DTO exposes only the
 * fields the client needs to display a user profile:
 *   - id, name, email, roles
 *
 * This is the "response shaping" principle — never return raw entity objects
 * from your API. Always map to a DTO first.
 */
public class UserDto {

    private UUID id;
    private String name;
    private String email;
    private String roles;

    /** No-arg constructor for Jackson deserialization. */
    public UserDto() {
    }

    public UserDto(UUID id, String name, String email, String roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
