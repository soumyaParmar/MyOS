package com.myos.entity;

/**
 * WHAT IS AN ENUM?
 * An enum (short for "enumeration") is a special Java type that represents
 * a fixed set of constants. Think of it like a dropdown menu with predefined choices.
 *
 * Instead of using raw strings like "BEARER" throughout the code (error-prone),
 * we define them as enum values. The compiler checks that only valid values are used.
 *
 * WHY TOKENTYPE?
 * In OAuth2/JWT authentication, tokens have a "type" that tells the server how
 * to interpret them. "Bearer" means "whoever bears (carries) this token gets access."
 * The client sends: "Authorization: Bearer <token>" in the HTTP header.
 *
 * We could add more types later (e.g., REFRESH, API_KEY) without changing existing code.
 */
public enum TokenType {
    BEARER
}
