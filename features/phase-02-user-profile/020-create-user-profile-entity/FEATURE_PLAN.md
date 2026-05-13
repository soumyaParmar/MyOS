# Feature Plan: Create UserProfile Entity

## Source Checkbox

- Phase: Phase 2 — User Profile & Preferences
- Checkbox: `[ ] Create UserProfile entity (skills, goals, bio, resume text)`
- Current status: `[ ]`

## Goal

Establish a central data structure to store detailed user information (skills, goals, bio, resume text) that the AI Brain will use to personalize recommendations and actions. This profile will serve as the "identity" of the user within the AI agents' ecosystem.

## Scope

- Create a JPA entity `UserProfile` with fields for skills, goals, bio, and resume text.
- Establish a One-to-One relationship with the `User` entity.
- Implement encryption at rest for sensitive fields (bio, resume text, goals).
- Create a Flyway migration to add the `user_profiles` table.
- Provide educational comments explaining JPA relationships and encryption application.

## Out Of Scope

- Frontend UI for profile editing (this is a separate checkbox: `Build User Profile dashboard and edit forms`).
- REST APIs for profile CRUD (this is a separate checkbox: `Build profile CRUD REST APIs`).

## Backend Plan

### 1. Entity Definition
Create `com.myos.entity.UserProfile`:
- `UUID id` (Primary Key)
- `User user` (@OneToOne relationship with `User`)
- `String bio` (Encrypted)
- `String skills` (Comma-separated or JSON list, Encrypted)
- `String goals` (Encrypted)
- `String resumeText` (Encrypted, stored as TEXT)
- `OffsetDateTime createdAt`
- `OffsetDateTime updatedAt`

### 2. Relationship Update
Update `com.myos.entity.User` to include a reference to `UserProfile` for bidirectional mapping (optional but helpful).

### 3. Repository
Create `com.myos.repository.UserProfileRepository`.

## Frontend Plan

- None (Backend only task).

## Data And Migrations

### Flyway Migration (`V11__create_user_profiles_table.sql`)
```sql
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    bio TEXT,
    skills TEXT,
    goals TEXT,
    resume_text TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## API Contract

- None (Entity only).

## Security And Privacy

- **Encryption at Rest**: Fields like `bio`, `goals`, and `resumeText` contain highly personal information. We will use the existing `EncryptedStringConverter` to ensure this data is encrypted before hitting the database.
- **Data Isolation**: Each profile is strictly tied to a single user via `user_id`.

## Testing And Verification

### Automated Tests
- Create a unit test `UserProfileRepositoryTest` to verify saving and retrieving a profile.
- Verify that sensitive fields are correctly encrypted/decrypted via the converter in an integration test context.

### Manual Verification
- Inspect the database table structure after migration.

## Acceptance Criteria

- [ ] `UserProfile` entity created with correct annotations and fields.
- [ ] One-to-One relationship with `User` established.
- [ ] Sensitive fields use `@Convert(converter = EncryptedStringConverter.class)`.
- [ ] Flyway migration script successfully creates the table.
- [ ] `UserProfileRepository` created.
- [ ] Educational comments included in all new files.

## Open Questions

- Should `skills` be a separate table or a simple comma-separated string? For now, we'll stick to a string/JSON for simplicity, matching the `roles` and `preferences` pattern in `User`.
