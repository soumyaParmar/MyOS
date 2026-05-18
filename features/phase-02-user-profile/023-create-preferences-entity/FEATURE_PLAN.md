# Feature Plan: Create Preferences Entity

## Source Checkbox

- Phase: 2
- Checkbox: `- [ ] Create Preferences entity (job types, budget limits, notification settings)`
- Current status: `[ ]`

## Goal

The goal is to transition from the generic `jsonb` preferences field in the `User` entity to a dedicated `UserPreferences` entity. This provides:
1. **Type Safety**: Proper Java types for each preference setting.
2. **Granularity**: Ability to update specific settings without parsing/re-writing a JSON blob.
3. **Extensibility**: Easily add new preference categories (AI settings, theme, etc.) as separate columns.
4. **Relationship**: A structured 1:1 relationship with the `User`.

## Scope

- Create a new JPA entity `UserPreferences`.
- Define fields for:
    - `jobTypes`: List or comma-separated string of preferred job roles.
    - `budgetLimits`: JSON or specific columns for monthly limits.
    - `notificationSettings`: JSON or specific boolean flags (Email, Push, Agent-specific).
    - `theme`: UI preference (Light/Dark).
- Set up a One-to-One relationship with `User`.
- Create a Flyway migration to add the `user_preferences` table.

## Out Of Scope

- Building the REST APIs (this is the next checkbox).
- Building the Frontend settings page (this is two checkboxes away).
- Migrating existing data from the `User.preferences` field (if any exists, though currently it's likely empty or placeholder).

## Backend Plan

- **Entity**: `com.myos.entity.UserPreferences`
    - `@Id` (UUID)
    - `@OneToOne` with `User`
    - `String jobTypes` (comma-separated for simplicity in this learning phase, or a List with `@ElementCollection`)
    - `Double monthlyBudgetLimit`
    - `Boolean emailNotificationsEnabled`
    - `Boolean pushNotificationsEnabled`
- **Repository**: `com.myos.repository.UserPreferencesRepository`

## Frontend Plan

- None for this specific checkbox (entity only).

## Data And Migrations

- **New Table**: `user_preferences`
- **Flyway Migration**: `V20260513_023__create_user_preferences_table.sql`
    - Columns: `id`, `user_id`, `job_types`, `monthly_budget_limit`, `email_notifications_enabled`, `push_notifications_enabled`, `created_at`, `updated_at`.
    - Foreign key constraint to `users(id)`.

## API Contract

- None (Entity only).

## Security And Privacy

- Preferences are personal data. Access must be restricted to the owner or admins.
- The relationship ensures that preferences are tied to a specific `user_id`.

## Testing And Verification

- **Unit Test**: Create a test in `UserPreferencesRepositoryTest` to verify saving and retrieving preferences.
- **Verification**: Verify the table is created correctly in the database via Flyway.

## Acceptance Criteria

- [ ] `UserPreferences` entity created with requested fields.
- [ ] 1:1 relationship with `User` established.
- [ ] Flyway migration script created and tested.
- [ ] Repository interface created.

## Open Questions

- Should we use `@ElementCollection` for `jobTypes` or keep it simple with a comma-separated string? (Recommendation: Keep it simple for now, or use a JSONB column if we want to stay modern).
- Should we keep the `preferences` column in the `User` table? (Recommendation: Deprecate/Remove it once this is fully implemented).
