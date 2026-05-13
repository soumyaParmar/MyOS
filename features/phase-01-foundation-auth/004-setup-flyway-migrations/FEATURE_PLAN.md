# Feature Plan: 004-setup-flyway-migrations

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: `- [ ] Set up Flyway for database migrations`
- Current status: `[ ]`

## Goal

Integrate Flyway into the Spring Boot backend to manage database schema versioning. This ensures that the database schema is consistent across all environments and allows for automated, reproducible migrations.

## Scope

- Configure Flyway in `application.yml`.
- Create the migration directory structure in the backend.
- Add an initial "baseline" migration script (even if empty or just a test table) to verify setup.
- Ensure the application starts successfully with Flyway enabled.

## Out Of Scope

- Defining actual domain entities (Users, Profile, etc.) — these will be handled in subsequent features.
- Multi-database migration support.

## Backend Plan

- Verify `flyway-core` and `flyway-database-postgresql` dependencies in `pom.xml` (already present).
- Update `src/main/resources/application.yml` to include Flyway configurations if needed (though Spring Boot defaults are often sufficient).
- Create `src/main/resources/db/migration` directory.
- Create `V1__init_schema.sql` to initialize the database.

## Frontend Plan

- N/A (Backend-only infrastructure task).

## Data And Migrations

- Initial migration: `V1__init_schema.sql`.
- Migration location: `classpath:db/migration`.

## API Contract

- N/A.

## Security And Privacy

- Database credentials should remain in `.env` and be injected via `${}` in `application.yml`. Flyway will use the same datasource.

## Testing And Verification

- Run the application and check logs for Flyway's initialization and migration execution.
- Verify the `flyway_schema_history` table is created in PostgreSQL.

## Acceptance Criteria

- [ ] `src/main/resources/db/migration` directory exists.
- [ ] At least one migration script exists.
- [ ] Application starts without Flyway errors.
- [ ] `flyway_schema_history` table is present in the database after startup.

## Open Questions

- Should we include the `User` table in the initial migration, or keep it strictly for infrastructure setup? (Recommendation: Keep it for infrastructure, next feature will add `User`).
