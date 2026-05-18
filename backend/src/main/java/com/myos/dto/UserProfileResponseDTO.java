package com.myos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning user profile information to the client.
 * 
 * WHY A SEPARATE DTO?
 * We do not want to expose our raw database entity (UserProfile) to the client.
 * Using a DTO allows us to format the data securely and decoupled from the DB schema.
 * 
 * LOMBOK ANNOTATIONS:
 * @Data — Generates getters, setters, toString, equals, and hashCode.
 * @Builder — Provides a convenient builder pattern for creating instances.
 * @NoArgsConstructor & @AllArgsConstructor — Generates empty and full constructors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private String bio;
    private String skills;
    private String goals;
    private String resumeText;
}
