# Feature Plan: Dockerize Backend

## Source Checkbox

- Phase: 14 (Moved forward)
- Checkbox: Write Dockerfile for the Spring Boot app / Write docker-compose.yml
- Current status: `[x]`

## Goal

Containerize the Spring Boot backend and integrate it into the existing Docker Compose environment alongside the PostgreSQL database. This allows for a consistent development and deployment environment.

## Scope

- Create a multi-stage `Dockerfile` for the Java backend.
- Update `docker-compose.yml` to include the `api` service.
- Configure network communication between the backend and database.
- Ensure environment variables are correctly passed to the container.

## Out Of Scope

- Dockerizing the frontend (handled in a separate task).
- Production-grade orchestration (K8s, etc.).

## Backend Plan

- Create `backend/Dockerfile` using Eclipse Temurin JDK 21.
- Optimize the build stage to use Maven Wrapper.
- Set up `api` service in `docker-compose.yml`.

## Frontend Plan

- Ensure `FRONTEND_URL` in backend matches the local frontend dev server (usually `http://localhost:3000`).

## Data And Migrations

- The backend will continue to use Flyway for migrations on startup.
- Database service remains as defined in previous steps but will be part of a shared network.

## API Contract

- No changes to existing APIs.
- Port 8080 will be exposed to the host.

## Security And Privacy

- Secrets will continue to be managed via `.env` and environment variables.
- Docker network will isolate database traffic from the host (except for the mapped port 5438).

## Testing And Verification

- Run `docker compose up --build` and verify the `myos-api` container starts and connects to `myos-db`.
- Check logs for "Started MyOsApplication" and successful Flyway migrations.

## Acceptance Criteria

- [ ] `backend/Dockerfile` exists and builds correctly.
- [ ] `backend/docker-compose.yml` contains both `db` and `api` services.
- [ ] Backend can successfully connect to the database within the Docker network.
- [ ] API is accessible at `http://localhost:8080` from the host.

## Open Questions

- Should we use a specific profile for Docker (e.g., `spring.profiles.active=docker`)?
- Do we want to use `docker-compose.override.yml` for local dev specificities?
