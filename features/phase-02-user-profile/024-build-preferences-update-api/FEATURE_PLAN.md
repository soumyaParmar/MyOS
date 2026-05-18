# Feature Plan: Build Preferences Update API

This plan covers the implementation of the REST API for updating user preferences in the MyOS backend.

## Source Checkbox
- [ ] Build preferences update API (from `MyOS_TODO.md` - Phase 2)

## Goal and User Value
Allow users to manage their personal settings, such as job search categories, budget limits, and notification preferences. These settings will drive the behavior of various AI agents (Job Agent, Finance Agent, etc.) in later phases.

## Scope
- Create a REST endpoint to retrieve current user preferences.
- Create a REST endpoint to update user preferences.
- Implement business logic to ensure preferences are correctly linked to the authenticated user.
- Add validation for preference fields (e.g., budget limits cannot be negative).

## Out of Scope
- Frontend implementation (this will be handled in the next task).
- Integration with AI agents (this will be done in Phase 3+).

## Backend Changes

### DTOs
- `UserPreferencesRequest`: For receiving update data.
- `UserPreferencesResponse`: For returning preference data.

### Services
- `UserPreferencesService`:
    - `getPreferencesByUserId(UUID userId)`
    - `updatePreferences(UUID userId, UserPreferencesRequest request)`

### Controllers
- `UserPreferencesController`:
    - `GET /api/preferences`: Retrieve preferences for the authenticated user.
    - `PUT /api/preferences`: Update preferences for the authenticated user.

## Data Model Changes
No changes to the `UserPreferences` entity or schema, as it was already created in the previous task.

## API Contracts

### GET `/api/preferences`
**Response (200 OK):**
```json
{
  "jobTypes": "Remote, Java, Spring Boot",
  "monthlyBudgetLimit": 5000.0,
  "emailNotificationsEnabled": true,
  "pushNotificationsEnabled": true
}
```

### PUT `/api/preferences`
**Request Body:**
```json
{
  "jobTypes": "Remote, Java, Spring Boot",
  "monthlyBudgetLimit": 5500.0,
  "emailNotificationsEnabled": true,
  "pushNotificationsEnabled": false
}
```
**Response (200 OK):**
```json
{
  "jobTypes": "Remote, Java, Spring Boot",
  "monthlyBudgetLimit": 5500.0,
  "emailNotificationsEnabled": true,
  "pushNotificationsEnabled": false
}
```

## Security and Privacy
- Only authenticated users can access their own preferences.
- The `userId` will be extracted from the security context (JWT), not from the request body/path, to prevent IDOR (Insecure Direct Object Reference) attacks.

## Testing and Verification Plan
- Unit tests for `UserPreferencesService`.
- Integration tests for `UserPreferencesController` using `MockMvc`.
- Verify that updating preferences for one user does not affect another user.

## Acceptance Criteria
- [ ] `GET /api/preferences` returns 200 OK with current user settings.
- [ ] `PUT /api/preferences` updates settings in the database and returns 200 OK.
- [ ] All Java files include educational comments.
- [ ] Tests pass successfully.

## Open Questions
None.
