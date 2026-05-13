# Feature Plan: Write Auth Unit Tests

## Source Checkbox
- [ ] Write auth unit tests (JUnit 5 + Mockito)

## Goal and User Value
**Goal:** Ensure the reliability and security of the authentication system by writing comprehensive unit tests for core auth services and controllers.
**User Value:** Prevents regressions in the authentication logic (the most sensitive part of the app). It gives the developer confidence that password hashing, token generation, and role management are working exactly as intended.

## Scope
- Unit testing for `AuthService` using Mockito to mock dependencies (`UserRepository`, `PasswordEncoder`, `JwtService`, etc.).
- Unit testing for `JwtService` to verify token generation, extraction, and validation logic.
- Unit testing for `RateLimitingService` to ensure bucket logic works correctly.
- Use **JUnit 5** and **Mockito**.

## Out of Scope
- Integration tests (hitting a real database or starting the full Spring context) — though we might add a few `MockMvc` tests for the controller.
- Testing OAuth2 flows (which involve external providers and are harder to unit test without complex mocking).
- Frontend testing.

## Backend Changes

### New Files (Test Classes)
- `AuthServiceTest.java`: Tests `register()`, `login()`, `refreshToken()`, and `logout()`.
- `JwtServiceTest.java`: Tests `generateToken()`, `extractUsername()`, `isTokenValid()`.
- `RateLimitingServiceTest.java`: Tests bucket resolution and consumption.

### Modifications
- None to production code (unless bugs are found during testing).

## Testing and Verification Plan
- Run all tests using `./mvnw test`.
- Ensure all tests pass.
- Verify that educational comments are added to the test files as well (explaining `@Mock`, `@InjectMocks`, `@Test`, etc.).

## Acceptance Criteria
- [ ] `AuthService` is covered by unit tests for all major methods.
- [ ] `JwtService` is covered by unit tests for token lifecycle.
- [ ] `RateLimitingService` is covered by unit tests.
- [ ] All tests pass.
- [ ] Educational comments are added to test classes.

## Open Questions
- Should we use AssertJ for better assertions? (Recommendation: Yes, it's standard with `spring-boot-starter-test`).
- Do we need to test `RateLimitFilter`? (Recommendation: Yes, using `MockHttpServletRequest` and `MockHttpServletResponse`).
