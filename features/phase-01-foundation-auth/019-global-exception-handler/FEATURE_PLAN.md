# Feature Plan: Global Exception Handler

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: Implement Global Exception Handler (@RestControllerAdvice)
- Current status: `[ ]`

## Goal

Standardize error responses across the entire application. Instead of raw Spring Boot error pages or inconsistent JSON, the API should return a consistent structure that helps the frontend handle errors gracefully.

## Scope

- Create a standard `ErrorResponse` DTO (timestamp, status, message, path, and optional list of validation errors).
- Implement a `GlobalExceptionHandler` class annotated with `@RestControllerAdvice`.
- Handle specific exceptions:
    - `BadCredentialsException` (401 Unauthorized)
    - `AccessDeniedException` (403 Forbidden)
    - `MethodArgumentNotValidException` (400 Bad Request - for `@Valid` failures)
    - `ConstraintViolationException` (400 Bad Request)
    - Generic `Exception` (500 Internal Server Error)
- Ensure all responses are in JSON format.

## Out Of Scope

- Custom business exceptions (we will add those as we build Phase 2+ features).
- Frontend error boundary implementation (part of a future frontend task).

## Backend Plan

### New Files
- `com.myos.dto.ErrorResponse`: DTO for structured error data.
- `com.myos.exception.GlobalExceptionHandler`: The central advice class.

### Modifications
- None strictly required, but we might clean up some controller code if they were manually catching exceptions.

## Data And Migrations

- None required.

## API Contract

All error responses will follow this structure:
```json
{
  "timestamp": "2024-05-11T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "validationErrors": {
    "email": "Invalid email format",
    "name": "Name cannot be blank"
  }
}
```

## Security And Privacy

- **Sanitize Error Messages**: Avoid leaking internal details (like class names, SQL queries, or stack traces) in the `message` field for production or generic `Exception` handlers.
- **Access Denied**: Ensure `403` responses don't reveal information about the existence of resources to unauthorized users.

## Testing And Verification

- Use `AuthServiceTest` or create a new `GlobalExceptionHandlerTest` to verify that throwing an exception results in the expected JSON response.
- Manual verification using Postman/Curl:
    - Send invalid login credentials (verify 401).
    - Send registration with empty fields (verify 400 with validation details).
    - Access an admin endpoint as a user (verify 403).

## Acceptance Criteria

- [ ] `ErrorResponse` DTO created.
- [ ] `@RestControllerAdvice` implemented.
- [ ] Validation errors (400) include field-specific messages.
- [ ] Generic exceptions return a clean 500 error without leaking stack traces.
- [ ] Educational comments explaining `@RestControllerAdvice` and `@ExceptionHandler`.

## Open Questions

- Do we want to include a "trace" field only in a `dev` profile? (Recommendation: Keep it simple for now, no trace in the response body).
