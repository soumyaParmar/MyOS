package com.myos.entity;

import com.myos.security.EncryptedStringConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity representing extended user information.
 * 
 * LOMBOK ANNOTATIONS:
 * @Getter / @Setter — Automatically generates all getX() and setX() methods at compile time.
 * @NoArgsConstructor — Generates the mandatory empty constructor required by JPA.
 * 
 * These help keep our entity classes clean and focused on the data structure 
 * rather than repetitive "boilerplate" code.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    /**
     * @Id — Marks this field as the primary key.
     * @GeneratedValue(strategy = GenerationType.UUID) — Auto-generates a unique UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * @OneToOne — Defines a 1:1 relationship between User and UserProfile.
     * 
     * @JoinColumn(name = "user_id", nullable = false)
     * Maps the 'user_id' column in the 'user_profiles' table to this field.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * @Convert(converter = EncryptedStringConverter.class)
     * Transparently encrypts/decrypts this field.
     */
    @Column(columnDefinition = "text")
    @Convert(converter = EncryptedStringConverter.class)
    private String bio;

    @Column(columnDefinition = "text")
    @Convert(converter = EncryptedStringConverter.class)
    private String skills;

    @Column(columnDefinition = "text")
    @Convert(converter = EncryptedStringConverter.class)
    private String goals;

    /**
     * Stored as TEXT in DB to handle large resumes.
     */
    @Column(name = "resume_text", columnDefinition = "text")
    @Convert(converter = EncryptedStringConverter.class)
    private String resumeText;

    /**
     * @CreationTimestamp — Automatically set by Hibernate on INSERT.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /**
     * @UpdateTimestamp — Automatically set by Hibernate on UPDATE.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /** Convenience constructor. */
    public UserProfile(User user) {
        this.user = user;
    }
}
