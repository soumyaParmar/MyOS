# Feature Plan: Add OAuth2 SSO Login (Google + GitHub)

## Source Checkbox
- [ ] Add OAuth2 SSO login (Google + GitHub)

## Goal and User Value
Allow users to securely log in to MyOS using their existing Google or GitHub accounts. This improves user experience by eliminating the need to create and remember another password, while providing high-assurance authentication.

## Scope
- Backend: Configure Spring Security for OAuth2 Login.
- Backend: Implement custom `OAuth2UserService` to map provider details to the local `User` entity.
- Backend: Update `User` entity to support OAuth2 providers.
- Backend: Handle successful OAuth2 authentication by issuing a local JWT.
- Backend: Securely manage OAuth2 credentials via environment variables.

## Out of Scope
- Frontend implementation (this will be handled in the next checkbox).
- Linking multiple OAuth2 providers to the same account (initially, we'll map by email).

## Backend Changes

### Dependencies
- Add `spring-boot-starter-oauth2-client` to `pom.xml`.

### Data Model
- [MODIFY] `User` entity:
    - Add `String provider` (e.g., "google", "github").
    - Add `String providerId` (unique ID from the provider).
    - Make `password` nullable or handle its absence for OAuth2 users.
- [NEW] Flyway migration `V6__add_oauth2_fields_to_users.sql` to add these columns.

### Security Configuration
- [MODIFY] `SecurityConfig.java`:
    - Enable `.oauth2Login()`.
    - Configure custom `oauth2UserService`.
    - Configure `successHandler` to redirect to a frontend URL with a JWT (or set it in a cookie).

### Services
- [NEW] `CustomOAuth2UserService`: Loads or creates a user based on the OAuth2 attribute map.
- [NEW] `OAuth2AuthenticationSuccessHandler`: Generates a JWT for the authenticated user and handles the final redirect.

## Frontend Changes
- None (Scope is Backend only for this task).

## Data Model or Migration Changes
- `users` table:
    - `provider` VARCHAR(50)
    - `provider_id` VARCHAR(255)
    - `password` becomes nullable.

## API Contracts
- `/oauth2/authorization/google` (standard Spring Security endpoint).
- `/oauth2/authorization/github` (standard Spring Security endpoint).
- Callback URL: `/login/oauth2/code/{provider}`.

## Security and Privacy Considerations
- OAuth2 secrets must be stored in `.env` and never committed.
- Ensure that if a user logs in via OAuth2, their email is verified by the provider (Google/GitHub do this).
- Map users by email to avoid duplicate accounts if they switch between local and OAuth2 (with caution).

## Testing and Verification Plan
- Unit tests for `CustomOAuth2UserService`.
- Manual verification using a browser to trigger the OAuth2 flow (will require real/test client IDs).

## Acceptance Criteria
- User can successfully authenticate via Google/GitHub.
- User is automatically registered in the database on first login.
- System issues a valid JWT after successful OAuth2 login.
- No secrets are committed to the repository.

## Open Questions
- Do we want to support linking an existing local account to an OAuth2 provider later?
- Should the JWT be returned as a query param or a cookie? (Usually query param for redirect, then frontend stores it).
