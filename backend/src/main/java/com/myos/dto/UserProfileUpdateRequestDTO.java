package com.myos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for receiving user profile update requests from the client.
 * 
 * By defining a specific request DTO, we can enforce validation rules (like @NotNull or @Size)
 * later without cluttering the entity or response objects. It also prevents the client from
 * trying to update fields they shouldn't (like IDs or timestamps).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequestDTO {
    private String bio;
    private String skills;
    private String goals;
    private String resumeText;
}
