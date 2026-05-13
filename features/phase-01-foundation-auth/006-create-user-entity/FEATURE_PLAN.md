# Feature Plan: Create User Entity

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: `Create User entity (id, name, email, roles, preferences)`
- Current status: `[ ]`

## Goal

Define the core `User` model for MyOS. This entity will serve as the primary identity for authentication, authorization (RBAC), and basic system-level preferences.

## Scope

- Define the `User` JPA entity with standard fields.
- Implement the `UserRepository` interface for database access.
- Create a Flyway migration script to initialize the `users` table in PostgreSQL.
- Ensure the `email` field is unique and indexed.

## Out Of Scope

- Authentication logic (JWT/SSO) - This will be handled in subsequent tasks.
- Advanced User Profile (skills, bio) - This belongs to Phase 2.
- UI for user management.

## Backend Plan

- **Package**: `com.myos.entity` (or `com.myos.model`)
- **Class**: `User`
  - `@Id` `@GeneratedValue(strategy = GenerationType.UUID)` `private UUID id;`
  - `private String name;`
  - `private String email;` (Unique)
  - `private Set<String> roles;` (Using `@ElementCollection` for now or a simple string join)
  - `private String preferences;` (JSONB string for basic settings like theme, etc.)
- **Repository**: `UserRepository` extending `JpaRepository<User, UUID>`.

## Frontend Plan

- None for this specific task (Data model only).

## Data And Migrations

- **File**: `backend/src/main/resources/db/migration/V2__Create_users_table.sql`
- **Schema**:
  ```sql
  CREATE TABLE users (
      id UUID PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      email VARCHAR(255) NOT NULL UNIQUE,
      roles TEXT, -- Comma separated or separate table
      preferences JSONB,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
  );
  CREATE INDEX idx_users_email ON users(email);
  ```

## API Contract

- None (Internal entity creation).

## Security And Privacy

- Email uniqueness is critical for identity.
- Data at rest encryption (AES-256) is mentioned in TODO (Line 18), but we'll focus on the schema first.
- The `preferences` field should not store sensitive credentials (use `.env` for those).

## Testing And Verification

- **Unit Tests**: `UserRepositoryTests` using `@DataJpaTest`.
- **Manual Verification**:
  - Run the application and verify Flyway migration success.
  - Verify table structure in PostgreSQL.

## Acceptance Criteria

- [ ] `User` entity class exists with all specified fields.
- [ ] `UserRepository` exists.
- [ ] Database migration `V2` creates the `users` table successfully.
- [ ] Unit tests confirm CRUD operations on `User` entity work as expected.

## Open Questions

- Should we use a separate `roles` table or `@ElementCollection` for roles? (Initial plan: `@ElementCollection` for simplicity in a personal OS).
- Should `preferences` be a structured POJO mapped to JSONB or just a String/Map?
