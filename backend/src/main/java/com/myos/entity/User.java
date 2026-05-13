package com.myos.entity;

import com.myos.security.EncryptedStringConverter;
import com.myos.security.EncryptionUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA Entity representing a user in the system.
 * 
 * LOMBOK ANNOTATIONS:
 * @Getter / @Setter — Generates all getters and setters automatically.
 * @NoArgsConstructor — Generates the mandatory empty constructor for JPA.
 *
 * This significantly reduces the size of the class while keeping the data structure clear.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(name = "email_hash", length = 64)
    private String emailHash;

    @Column
    private String password;

    @Column
    private String provider;

    @Column(name = "provider_id")
    @Convert(converter = EncryptedStringConverter.class)
    private String providerId;

    @Column
    private String roles;

    @Column(columnDefinition = "text")
    @Convert(converter = EncryptedStringConverter.class)
    private String preferences;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /** Bidirectional One-to-One mapping. */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    /** Convenience constructor for creating users programmatically. */
    public User(String name, String email, String password, String roles, String preferences) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.preferences = preferences;
    }

    @PrePersist
    @PreUpdate
    private void computeEmailHash() {
        if (email != null) {
            this.emailHash = EncryptionUtil.hashForLookup(email);
        }
    }

    // ======================== UserDetails INTERFACE METHODS ========================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return java.util.List.of();
        }
        return Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
