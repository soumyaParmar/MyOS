# Feature Plan: Setup PostgreSQL Configuration

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: Set up PostgreSQL + configure `application.yml`
- Current status: `[ ]`

## Goal

Establish a working database connection to a PostgreSQL instance. This will allow the application to persist data for users, jobs, and other entities in subsequent tasks.

## Scope

- Define the PostgreSQL connection settings in `backend/src/main/resources/application.yml`.
- Set up a `docker-compose.yml` file in the root directory to easily spin up a PostgreSQL instance.
- Configure Hibernate/JPA settings for production readiness (naming strategy, SQL logging).
- Verify connectivity by starting the app with the DB instance running.

## Out Of Scope

- Creating actual entities (User, etc.).
- Flyway migration scripts (next task).
- Production environment secrets management (will use environment variables with defaults).

## Backend Plan

- Update `backend/src/main/resources/application.yml`:
  - Configure `spring.datasource` with environment variable support:
    - `url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5438/myos}`
    - `username: ${SPRING_DATASOURCE_USERNAME:postgres}`
    - `password: ${SPRING_DATASOURCE_PASSWORD:password}`
  - Configure `spring.jpa`:
    - `hibernate.ddl-auto: validate` (Recommended for projects with migrations, but using `update` initially for development ease until Flyway is active).
    - `show-sql: true`
    - `properties.hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect`

- Create `docker-compose.yml` in root:
  - Service `db`:
    - Image: `postgres:16-alpine`
    - Environment: `POSTGRES_DB=myos`, `POSTGRES_PASSWORD=password`
    - Ports: `5438:5438`

## Frontend Plan

- N/A

## Data And Migrations

- None in this step. Connectivity check only.

## API Contract

- N/A (No new endpoints).

## Security And Privacy

- Database credentials should not be hardcoded in plain text in a real production environment.
- Using environment variables in `application.yml` as a best practice.

## Testing And Verification

- Run `docker compose up -d db` to start the database.
- Start the Spring Boot application.
- Verify the logs show a successful connection to the PostgreSQL database.

## Acceptance Criteria

- [ ] `docker-compose.yml` is present and correctly configures PostgreSQL.
- [ ] `application.yml` is updated with correct datasource settings.
- [ ] Application starts successfully and connects to the database.

## Open Questions

- Should we use a specific PostgreSQL version? (Proposing 16-alpine for modern features and small footprint).
- Should we use a separate `application-dev.yml`? (Proposing keeping it simple in `application.yml` for now with environment variable overrides).
