# Feature Plan: Frontend JWT Storage and API Interceptor

## Source Checkbox
- [ ] Frontend: Implement JWT storage and interceptor for API calls

## Goal and User Value
Ensure that users stay logged in across page refreshes and that all their requests to the backend are properly authenticated. This is critical for accessing protected resources and providing a seamless session experience.

## Scope
- Implement a utility for persistent JWT storage (using `localStorage` for now).
- Configure Axios interceptors to automatically attach the JWT to all requests to the backend API.
- Implement an `AuthProvider` using React Context to manage global authentication state (user info, loading state).
- Implement a `useAuth` hook for components to access authentication status.
- Handle 401 (Unauthorized) responses by automatically clearing the token and redirecting to the login page.
- Implement basic protected route logic (redirecting unauthenticated users from private pages).

## Out of Scope
- Server-side JWT validation in Next.js Middleware (will be handled if needed for SSR later).
- Refresh token rotation logic.
- Multi-session management.

## Backend Changes
- None.

## Frontend Changes

### State Management
- [NEW] `src/providers/AuthProvider.tsx`: Context provider to track user state and token.
- [NEW] `src/hooks/useAuth.ts`: Custom hook to interact with the Auth context.

### API Integration
- [MODIFY] `src/lib/api.ts`: Add request and response interceptors.
    - Request interceptor: Attach `Authorization` header if token exists.
    - Response interceptor: Handle 401 errors globally.

### Utilities
- [NEW] `src/lib/token.ts`: Utility for managing the JWT in `localStorage`.

### Components
- [NEW] `src/components/auth/ProtectedRoute.tsx`: A wrapper component to protect routes.

## Data Model or Migration Changes
- None.

## API Contracts
- Request Header: `Authorization: Bearer <JWT>`

## Security and Privacy Considerations
- Store the JWT securely. While `localStorage` is vulnerable to XSS, it is a standard starting point for many apps.
- Ensure the token is cleared on logout or 401 errors.
- Do not log the JWT in console logs or error reports.

## Testing and Verification Plan
- Manual testing: Log in and verify that subsequent API calls (e.g., to `/api/users/me`) include the token.
- Manual testing: Verify that refreshing the page keeps the user logged in.
- Manual testing: Verify that manual deletion of the token from `localStorage` triggers a logout state.

## Acceptance Criteria
- JWT is persisted across browser refreshes.
- All API requests to the backend automatically include the `Authorization` header if a token is present.
- Global `auth` state is available throughout the application.
- 401 Unauthorized errors from the backend trigger a redirect to `/login`.

## Open Questions
- Should we use cookies instead of `localStorage` for better SSR support and CSRF protection? (For now, `localStorage` is simpler for a client-side Vite-like feel in Next.js).
