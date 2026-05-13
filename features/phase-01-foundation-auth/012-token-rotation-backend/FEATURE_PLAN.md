# Feature Plan: Token Rotation (Backend)

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: `[ ] Implement Access Token & Refresh Token rotation (Backend)`
- Current status: `[ ]`

## Goal

Enhance security by implementing a dual-token system (Access + Refresh). Short-lived access tokens minimize the window of opportunity for stolen tokens, while long-lived refresh tokens allow for seamless user sessions. Refresh token rotation ensures that each time a new access token is requested, a new refresh token is also issued, and the old one is invalidated.

## Scope

- Create a `Token` entity to store refresh tokens in PostgreSQL.
- Update `JwtService` to handle both access and refresh tokens.
- Update `AuthService` to generate and save tokens.
- Implement a `/api/v1/auth/refresh` endpoint to handle token rotation.
- Implement a logout mechanism to revoke tokens.
- Add database migrations for the new `Token` table.

## Out Of Scope

- Frontend implementation of silent renewal (this will be a separate task).
- HTTP-only cookie migration (this will be a separate task).

## Backend Plan

1. **Entity**: Create `Token` entity with fields: `id`, `token`, `tokenType` (BEARER), `revoked`, `expired`, `user`.
2. **Repository**: Create `TokenRepository` with methods to find valid tokens by user and find token by string.
3. **JwtService**:
    - Add `generateRefreshToken(UserDetails)` method.
    - Update `generateToken` to accept extra claims (already exists but might need tweak).
4. **AuthService**:
    - Update `register` and `login` to generate both tokens.
    - Implement `saveUserToken` to persist the refresh token.
    - Implement `revokeAllUserTokens` to invalidate previous tokens for a user.
    - Implement `refreshToken(request, response)` logic.
5. **SecurityConfig**: Ensure `/api/v1/auth/refresh` is accessible or handled correctly.

## Frontend Plan

- None for this backend-only task.

## Data And Migrations

- New table `token`:
    - `id`: SERIAL PRIMARY KEY
    - `token`: VARCHAR(512) UNIQUE NOT NULL
    - `token_type`: VARCHAR(50) DEFAULT 'BEARER'
    - `revoked`: BOOLEAN DEFAULT FALSE
    - `expired`: BOOLEAN DEFAULT FALSE
    - `user_id`: INTEGER REFERENCES users(id)

## API Contract

### Authentication Response Update
```json
{
  "access_token": "...",
  "refresh_token": "..."
}
```

### Refresh Token Endpoint
`POST /api/v1/auth/refresh`
- Header: `Authorization: Bearer <REFRESH_TOKEN>`
- Response: `AuthenticationResponse` (new access + refresh token)

## Security And Privacy

- Refresh tokens are stored in the database to allow revocation (e.g., on password change or logout).
- Refresh token rotation: Every refresh request generates a NEW refresh token and invalidates the OLD one.
- If a leaked refresh token is reused, the system can detect it (optional: invalidate all tokens for that user if reuse is detected).

## Testing And Verification

- **Unit Tests**:
    - `JwtServiceTest`: Test generation of access and refresh tokens.
    - `AuthServiceTest`: Test login/register returning both tokens and refresh logic.
- **Manual Verification**:
    - Login and verify two tokens are returned.
    - Use the refresh token to get a new set of tokens.
    - Verify the old refresh token is marked as expired/revoked.

## Acceptance Criteria

- [ ] Successful login/registration returns both `access_token` and `refresh_token`.
- [ ] `POST /api/v1/auth/refresh` correctly issues new tokens.
- [ ] Old refresh tokens are invalidated after use.
- [ ] User can be logged out (tokens revoked).
- [ ] Database stores the tokens correctly.

## Open Questions

- Should we implement "Refresh Token Reuse Detection"? (If an old refresh token is used, should we revoke all tokens for that user as a safety measure?) -> Let's keep it simple for now: revoke the one used and issue new ones.
