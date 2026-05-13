# MyOS Backend

The backend of MyOS is built with Spring Boot 3.3.x and Java 21. It provides the core AI orchestration, data management, and integration services.

## Prerequisites

- **Java 21**: Ensure you have JDK 21 installed.
- **Maven**: (Recommended) Or use the provided Maven wrapper.
- **Docker**: Required for running the PostgreSQL database and other services.

## Getting Started

### 1. Start the Infrastructure
The backend requires PostgreSQL. You can start it using Docker Compose from the project root (once the task is complete) or manually.

```bash
docker compose up -d
```

### 2. Run the Application
From the `backend` directory, use the following command:

```bash
mvn spring-boot:run
```

If you don't have Maven installed globally, use the wrapper (to be added):
```bash
./mvnw spring-boot:run
```

### 3. Verify
You can check if the application is running by visiting:
`http://localhost:8080/health`

## Project Structure

- `src/main/java/com/myos`: Main source code.
- `src/main/resources`: Configuration and migration files.
- `pom.xml`: Maven build configuration.
