package com.myos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity representing user-specific settings and preferences.
 * 
 * LEARNING NOTE: Why a separate entity?
 * While we could store preferences as a JSON blob inside the User table, 
 * a separate entity provides:
 * 1. Type Safety: We can use Java types (Boolean, Double, etc.) instead of parsing JSON.
 * 2. Querying: It's easier to find users who have "Email Notifications" enabled via standard SQL/HQL.
 * 3. Scalability: We can add hundreds of settings without bloating the main 'users' table.
 */
@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
public class UserPreferences {

    /**
     * @Id — Marks this field as the primary key.
     * @GeneratedValue — Automatically generates a unique UUID for every new record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * @OneToOne — Defines a 1:1 relationship (one user has one set of preferences).
     * @JoinColumn — Creates a 'user_id' foreign key column in the 'user_preferences' table.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Stores preferred job categories (e.g., "Remote, Software Engineer, Java").
     */
    @Column(name = "job_types")
    private String jobTypes;

    /**
     * User's self-imposed monthly budget limit for the Finance Agent.
     */
    @Column(name = "monthly_budget_limit")
    private Double monthlyBudgetLimit;

    /**
     * Toggle for email-based alerts and digests.
     */
    @Column(name = "email_notifications_enabled")
    private Boolean emailNotificationsEnabled = true;

    /**
     * Toggle for browser/mobile push notifications.
     */
    @Column(name = "push_notifications_enabled")
    private Boolean pushNotificationsEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /**
     * Convenience constructor.
     * @param user The user this preference set belongs to.
     */
    public UserPreferences(User user) {
        this.user = user;
    }
}
