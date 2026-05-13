# Task: Token Rotation (Backend)

- [ ] Create Flyway migration for `token` table
- [ ] Create `Token` entity and `TokenType` enum
- [ ] Create `TokenRepository`
- [ ] Update `JwtService` to handle refresh tokens
- [ ] Update `AuthenticationResponse` DTO to include refresh token
- [ ] Update `AuthService` with token management logic
    - [ ] `saveUserToken`
    - [ ] `revokeAllUserTokens`
    - [ ] `register` (update)
    - [ ] `login` (update)
    - [ ] `refreshToken`
- [ ] Update `AuthController` with `/refresh` endpoint
- [ ] Implement Logout logic
- [ ] Verify with tests
- [ ] Update `MyOS_TODO.md`
