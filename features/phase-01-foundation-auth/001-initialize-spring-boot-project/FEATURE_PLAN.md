# Feature Plan: Initialize Spring Boot Project

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: Initialize Spring Boot project (Maven/Gradle)
- Current status: `[x]`

## Goal

The goal is to initialize the core Spring Boot application that will serve as the foundation for the MyOS ecosystem. This involves setting up the project structure, build tool configuration, and essential dependencies for upcoming features like Auth and PostgreSQL integration.

## Scope

- Initialize a Spring Boot 3.3.x project using Java 21.
- Configure Maven as the build tool.
- Set up the base package structure: `com.myos`.
- Include initial dependencies for web, security, data, and migrations.
- Create a basic `application.yml` file.
- Add a simple health check or "Hello World" endpoint to verify initialization.

## Out Of Scope

- PostgreSQL connection setup (this is the next checkbox).
- Flyway migration scripts (checkbox after next).
- Full security implementation.
- User entity creation.

## Backend Plan

- Use Spring Initializr (or equivalent CLI/Command) to bootstrap the project.
- Project Metadata:
  - Group: `com.myos`
  - Artifact: `myos-api`
  - Name: `MyOS API`
  - Description: `Personal AI Operating System Backend`
  - Java: `21`
  - Packaging: `Jar`
- Dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-security`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `org.postgresql:postgresql` (Runtime)
  - `org.flywaydb:flyway-core`
  - `org.flywaydb:flyway-database-postgresql`
  - `org.projectlombok:lombok` (Optional/Recommended)
  - `spring-boot-starter-test`
  - `spring-security-test`

## Frontend Plan

- N/A for this backend initialization task.

## Data And Migrations

- None at this stage. Flyway will be configured but no migrations will be added yet.

## API Contract

- `GET /health` -> Returns `{"status": "UP"}` (Standard Actuator or custom simple controller).

## Security And Privacy

- Security will be initially disabled or configured with a permit-all for the health check to allow verification.
- Proper JWT security will be implemented in a subsequent feature task.

## Testing And Verification

- Verify `mvn clean install` runs successfully.
- Start the application and verify it listens on port 8080.
- `curl http://localhost:8080/health` (or whatever endpoint is created) returns success.

## Acceptance Criteria

- [ ] Maven project structure is correctly initialized.
- [ ] `pom.xml` contains all required dependencies.
- [ ] Application starts without errors.
- [ ] Health check endpoint returns 200 OK.
- [ ] Project follows the `src/main/java/com/myos` package structure.

## Open Questions

- Should we use Maven or Gradle? (Proposing Maven for stability and standard usage).
- Should we include Spring Boot Actuator immediately for health checks, or build a custom one? (Proposing custom simple one first to keep it lean).
