# Feature Plan: Build Profile CRUD REST APIs

## Source Checkbox

- Phase: Phase 2 — User Profile & Preferences
- Checkbox: `[ ] Build profile CRUD REST APIs`
- Current status: `[ ]`

## Goal

Enable authenticated users to read and update their extended profile information (bio, skills, goals, resume text). This sets up the backend foundation needed for the AI agents to personalize insights and actions for the user.

## Scope

- Create a `UserProfileResponseDTO` and `UserProfileUpdateRequestDTO`.
- Create a `UserProfileService` to handle retrieving and saving the profile.
- Implement lazy profile creation (if a profile doesn't exist for a user when requested, create a blank one).
- Create a `UserProfileController` exposing GET and PUT endpoints for the authenticated user's profile.
- Write unit tests for the service and controller.
- Add educational comments to all new classes per project learning rules.

## Out Of Scope

- Frontend UI for profile dashboard (this is the next task).
- Adding new data fields to the `UserProfile` entity.
- Fetching profiles of other users (MyOS is a personal system; profiles are private).

## Backend Plan

1. **DTOs (`com.myos.dto`)**:
   - `UserProfileResponseDTO`: fields for `bio`, `skills`, `goals`, `resumeText`.
   - `UserProfileUpdateRequestDTO`: same fields, used to pass updates.

2. **Service (`com.myos.service.UserProfileService`)**:
   - `getProfileForCurrentUser(String email)`: Finds the user, then finds the `UserProfile`. If not found, initializes and saves a blank `UserProfile`. Returns the `UserProfileResponseDTO`.
   - `updateProfileForCurrentUser(String email, UserProfileUpdateRequestDTO request)`: Same lookup as above, updates the entity with request fields, saves, and returns the updated DTO.

3. **Controller (`com.myos.controller.UserProfileController`)**:
   - `GET /api/v1/profile`: Extracts the authenticated user's email from `SecurityContextHolder`, calls `getProfileForCurrentUser(email)`.
   - `PUT /api/v1/profile`: Extracts email, calls `updateProfileForCurrentUser(email, request)`.

## Frontend Plan

- None (deferred to the next task).

## Data And Migrations

- None. `UserProfile` entity and repository already exist from the previous step.

## API Contract

### `GET /api/v1/profile`
- **Auth required**: Yes (Bearer Token / Cookie)
- **Response (200 OK)**:
  ```json
  {
    "bio": "Software engineer with 5 years of experience",
    "skills": "Java, Spring Boot, React",
    "goals": "Become a senior backend engineer",
    "resumeText": "..."
  }
  ```

### `PUT /api/v1/profile`
- **Auth required**: Yes
- **Request Body**:
  ```json
  {
    "bio": "Updated bio",
    "skills": "Updated skills",
    "goals": "Updated goals",
    "resumeText": "Updated resume"
  }
  ```
- **Response (200 OK)**:
  Returns the updated profile object as above.

## Security And Privacy

- Endpoints must be protected by Spring Security JWT filter.
- The user ID used for DB lookup is derived exclusively from the authenticated `SecurityContext`. This guarantees users cannot modify other people's profiles by manipulating API parameters.

## Testing And Verification

- **Service Unit Tests (`UserProfileServiceTest`)**: Verify lazy creation works, verify update logic, mock `UserRepository` and `UserProfileRepository`.
- **Controller Unit Tests (`UserProfileControllerTest`)**: Use `@WebMvcTest` to verify endpoints are mapped correctly, verify security blocks unauthenticated requests, and mock the `UserProfileService`.

## Acceptance Criteria

- [ ] `GET /api/v1/profile` successfully returns the user's profile.
- [ ] `PUT /api/v1/profile` updates and saves the user's profile.
- [ ] Business logic prevents users from modifying other users' profiles.
- [ ] Educational comments are included in all new files.
- [ ] Unit tests pass successfully.

## Open Questions

- None for now. Ready for user approval.
