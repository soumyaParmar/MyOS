# Feature Plan: Implement Role-Based Access Control (RBAC)

## Source Checkbox
- [ ] Implement role-based access control (ADMIN, USER)

## Goal and User Value
Ensure that sensitive operations and data are protected based on the user's role. This provides a security layer where standard users cannot access administrative features or data belonging to others.

## Scope
- Enable Spring Security method-level security (`@EnableMethodSecurity`).
- Implement an example `AdminController` with endpoints restricted to `ROLE_ADMIN`.
- Update `SecurityConfig` to demonstrate URL-based access control.
- Ensure roles are correctly mapped from the `User` entity to Spring Security's `GrantedAuthority`.
- Implement a "Me" endpoint to return current user details including roles.

## Out of Scope
- Hierarchical roles (e.g., SUPER_ADMIN > ADMIN).
- Dynamic permission management (fine-grained permissions beyond roles).

## Backend Changes

### Configuration
- [MODIFY] `SecurityConfig.java`:
    - Add `@EnableMethodSecurity`.
    - (Optional) Add URL-based restrictions in `securityFilterChain`.

### Controllers
- [NEW] `AdminController.java`: Endpoints protected by `@PreAuthorize("hasRole('ADMIN')")`.
- [NEW] `UserController.java`: Endpoints for fetching current user profile (`/api/users/me`).

### Security
- [MODIFY] `User.java`: (Already has roles field and implements `getAuthorities`).

## Frontend Changes
- [MODIFY] `src/providers/AuthProvider.tsx`: Ensure user object in context includes roles.
- [MODIFY] `src/components/auth/ProtectedRoute.tsx`: Update to support optional `requiredRoles` prop.

## Data Model or Migration Changes
- None (already supported by `User` entity).

## API Contracts
- `GET /api/users/me`: Returns current user info + roles.
- `GET /api/admin/stats`: (Example) Returns system stats, restricted to ADMIN.

## Security and Privacy Considerations
- Always prefix roles with `ROLE_` when checking with `hasRole()` in Spring Security.
- Avoid exposing sensitive data in the public `User` DTO.

## Testing and Verification Plan
- Unit test for `AdminController` to verify 403 Forbidden for `ROLE_USER`.
- Manual verification using a JWT with `ROLE_USER` vs `ROLE_ADMIN`.
- Manual verification of the `/api/users/me` endpoint.

## Acceptance Criteria
- Endpoints marked with `@PreAuthorize("hasRole('ADMIN')")` are inaccessible to standard users.
- Standard users can access their own profile.
- Frontend correctly identifies the user's role and can conditionally render UI elements.

## Open Questions
- Should we use a separate `Role` entity instead of a comma-separated string in the future? (For now, string is fine for MVP).
