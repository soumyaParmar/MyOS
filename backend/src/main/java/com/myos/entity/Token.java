package com.myos.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing a JWT token stored in the database.
 *
 * WHY STORE TOKENS IN THE DATABASE?
 * JWTs are normally stateless (the server doesn't track them). But we need
 * the ability to revoke tokens (e.g., on logout or password change). By storing
 * tokens in the DB, we can mark them as "revoked" or "expired" and reject them
 * even if they haven't technically expired yet.
 *
 * WHAT IS @Entity?
 * Marks this class as a JPA entity — meaning Hibernate will map it to a database table.
 * Each instance of Token represents one row in the "token" table.
 *
 * WHAT IS @Table?
 * Specifies the name of the database table this entity maps to.
 * Without it, JPA would use the class name ("Token") as the table name.
 */
@Entity
@Table(name = "token")
public class Token {

    /**
     * @Id — Marks this field as the primary key of the table.
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * Tells the database to auto-generate the ID using an auto-incrementing sequence.
     * IDENTITY means the database handles the numbering (1, 2, 3, ...).
     * Alternative strategies include UUID (random unique IDs) and SEQUENCE (DB sequences).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    /**
     * The actual JWT string (e.g., "eyJhbGciOiJIUzI1NiIsIn...").
     *
     * @Column(unique = true) — Adds a UNIQUE constraint in the database,
     * meaning no two rows can have the same token value. This prevents
     * duplicate tokens from being stored.
     */
    @Column(unique = true)
    public String token;

    /**
     * The type of token (currently only BEARER).
     *
     * @Enumerated(EnumType.STRING) — Tells JPA to store the enum value as a string
     * in the database ("BEARER") instead of a number (0). This is more readable
     * and safer when you add new enum values later.
     */
    @Enumerated(EnumType.STRING)
    public TokenType tokenType = TokenType.BEARER;

    /** Whether this token has been revoked (e.g., after logout). */
    public boolean revoked;

    /** Whether this token has expired (used for token rotation). */
    public boolean expired;

    /**
     * The user who owns this token.
     *
     * @ManyToOne — Defines a many-to-one relationship: many tokens can belong
     * to one user. In database terms, this creates a foreign key column.
     *
     * fetch = FetchType.LAZY — Don't load the User object from the database
     * until it's actually accessed. This improves performance by avoiding
     * unnecessary queries. (EAGER would load the user immediately with every token query.)
     *
     * @JoinColumn(name = "user_id") — Specifies the foreign key column name
     * in the "token" table that references the "users" table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;

    /**
     * No-argument constructor required by JPA.
     * JPA needs this to create entity instances via reflection when loading from the database.
     */
    public Token() {
    }

    /** All-arguments constructor for manual creation. */
    public Token(Integer id, String token, TokenType tokenType, boolean revoked, boolean expired, User user) {
        this.id = id;
        this.token = token;
        this.tokenType = tokenType;
        this.revoked = revoked;
        this.expired = expired;
        this.user = user;
    }

    // ======================== GETTERS AND SETTERS ========================
    // In Java, fields are typically private, and you provide public getter/setter
    // methods to access them. This is called "encapsulation" — one of the core
    // principles of object-oriented programming. It lets you control how fields
    // are read and modified (e.g., adding validation in a setter).

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // ======================== BUILDER PATTERN ========================

    /**
     * WHAT IS THE BUILDER PATTERN?
     * Instead of calling a constructor with many parameters (hard to read):
     *   new Token(null, "abc", TokenType.BEARER, false, false, user)
     *
     * You can use a builder (much more readable):
     *   Token.builder()
     *       .token("abc")
     *       .tokenType(TokenType.BEARER)
     *       .user(user)
     *       .build();
     *
     * Each method returns "this" (the builder itself), enabling method chaining.
     * The build() method at the end creates the actual Token object.
     *
     * NOTE: Libraries like Lombok can auto-generate builders with @Builder annotation,
     * but here we write it manually for learning purposes.
     */
    public static TokenBuilder builder() {
        return new TokenBuilder();
    }

    public static class TokenBuilder {
        private Integer id;
        private String token;
        private TokenType tokenType = TokenType.BEARER;
        private boolean revoked;
        private boolean expired;
        private User user;

        public TokenBuilder id(Integer id) {
            this.id = id;
            return this; // Return "this" to enable method chaining
        }

        public TokenBuilder token(String token) {
            this.token = token;
            return this;
        }

        public TokenBuilder tokenType(TokenType tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public TokenBuilder revoked(boolean revoked) {
            this.revoked = revoked;
            return this;
        }

        public TokenBuilder expired(boolean expired) {
            this.expired = expired;
            return this;
        }

        public TokenBuilder user(User user) {
            this.user = user;
            return this;
        }

        /** Creates the final Token object with all the values set on this builder. */
        public Token build() {
            return new Token(id, token, tokenType, revoked, expired, user);
        }
    }
}
