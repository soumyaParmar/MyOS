# Feature Plan: Implement Spring Security with JWT Authentication

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: `Implement Spring Security with JWT authentication`
- Current status: `[ ]`

## Goal

Secure the MyOS API using JSON Web Tokens (JWT). This feature will provide a robust authentication mechanism where users can register, login, and receive a signed token to access protected resources. It's the foundation for personalized user experiences and data privacy in MyOS.

## Scope

- Add JWT dependencies to the backend.
- Update `User` entity to support password-based authentication.
- Create database migration for the new `password` field.
- Implement JWT generation, validation, and filtering logic.
- Create REST endpoints for User Registration and Login.
- Configure Spring Security to enforce JWT authentication for protected routes.
- Securely handle password hashing using BCrypt.

## Out Of Scope

- OAuth2 SSO login (Google/GitHub) — *This is the next checkbox.*
- Frontend integration (Login/Signup pages) — *This is a separate checkbox.*
- Password reset functionality.
- Multi-factor authentication (MFA).

## Backend Plan

### 1. Dependencies
Add `jjwt-api`, `jjwt-impl`, and `jjwt-jackson` to `pom.xml`.

### 2. User Entity Update
Add `private String password;` to `com.myos.entity.User`.
Update constructors and getters/setters.

### 3. Security Infrastructure
- **`UserDetailsServiceImpl`**: Implement `UserDetailsService` to load `User` from the database.
- **`JwtService`**: Handle JWT signing, extraction of claims, and validation.
- **`JwtAuthenticationFilter`**: Filter that extracts JWT from `Authorization` header and sets the security context.
- **`SecurityConfig`**: 
    - Configure `AuthenticationManager`, `AuthenticationProvider`, and `PasswordEncoder` (BCrypt).
    - Set up the filter chain to use `JwtAuthenticationFilter`.
    - Permit all access to `/api/auth/**` and require authentication for everything else.

### 4. Auth Controllers & DTOs
- `RegisterRequest`, `LoginRequest`, `AuthenticationResponse` DTOs.
- `AuthController`:
    - `POST /api/auth/register`: Create new user, hash password, return token.
    - `POST /api/auth/login`: Validate credentials, return token.

## Frontend Plan

- *No changes in this step (Focusing on Backend Auth first as per TODO order).*

## Data And Migrations

- New Flyway migration: `V2__Add_password_to_users.sql`
- Add `password` column to `users` table (`VARCHAR(255)`).

## API Contract

### Authentication API

| Endpoint | Method | Request Body | Response Body | Status |
|---|---|---|---|---|
| `/api/auth/register` | `POST` | `name`, `email`, `password` | `token` | `201 Created` |
| `/api/auth/login` | `POST` | `email`, `password` | `token` | `200 OK` |

## Security And Privacy

- **Password Hashing**: Use `BCryptPasswordEncoder` with a strength of 10.
- **Secret Management**: JWT secret key will be stored in `.env` and loaded via `application.yml`.
- **Token Expiration**: Initial expiration set to 24 hours.
- **No Sensitive Info in JWT**: Only `sub` (email) and `id` will be included in the claims.

## Testing And Verification

### Automated Tests
- `AuthIntegrationTest`: Verify registration, login, and access to protected routes with a valid/invalid token.
- `JwtServiceTest`: Unit test for token generation and parsing.
- `UserRepositoryTest`: Verify finding user by email.

### Manual Verification
- Use Postman/curl to:
    1. Register a new user.
    2. Login with valid credentials to get a token.
    3. Access a protected endpoint (e.g., a dummy test endpoint) with and without the `Authorization: Bearer <token>` header.

## Acceptance Criteria

- [ ] Users can successfully register and login.
- [ ] Passwords are stored in hashed format in the database.
- [ ] Valid JWTs grant access to protected endpoints.
- [ ] Invalid or missing JWTs result in a `403 Forbidden` or `401 Unauthorized`.
- [ ] JWT secret is externalized to environment variables.

## Open Questions

- Should we include roles in the JWT claims now or wait until RBAC is implemented in a later step?
- Do we want to implement Refresh Tokens immediately or stick to short-lived access tokens for Phase 1?
