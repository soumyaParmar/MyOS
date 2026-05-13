# Feature Plan: Frontend Silent Renewal & Interceptor

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: `[ ] Frontend: Implement Silent Renewal & Interceptor for token refresh`
- Current status: `[ ]`

## Goal and User Value

Now that the backend supports HTTP-only cookies and token rotation, the frontend needs to be updated to:
1. Stop relying on `localStorage` for sensitive tokens (XSS prevention).
2. Automatically refresh the access token when it expires without forcing the user to log in again.
3. Handle API requests seamlessly by intercepting 401 errors and retrying them after a successful refresh.

## Scope

- Configure Axios to send `withCredentials: true` to support HTTP-only cookies.
- Update `src/lib/api.ts` to implement a robust response interceptor for 401 handling.
- Implement a queue system for requests that arrive while a token is being refreshed.
- Update `src/lib/token.ts` to stop using `localStorage` (or reduce its use to non-sensitive "isLogged" flag).
- Ensure the frontend can still access public routes without triggering refresh logic.
- Update `AuthService` on frontend to handle the refresh endpoint.

## Out of Scope

- Next.js Middleware for SSR-level auth (this will be a separate task).
- Social login (OAuth2) silent renewal (already handled by backend cookies, but needs testing).

## Backend Changes

- None (already implemented in Phase 1 - 012).

## Frontend Changes

### API Integration
- [MODIFY] `src/lib/api.ts`: 
    - Set `withCredentials: true`.
    - Add a complex response interceptor to handle 401s, call refresh, and retry original requests.
- [MODIFY] `src/services/auth.service.ts`:
    - Add `refreshToken()` method that calls `/api/v1/auth/refresh`.

### Utilities
- [MODIFY] `src/lib/token.ts`:
    - Clean up `localStorage` logic. 
    - Maybe keep a `user_logged_in` boolean in `localStorage` just for UI state (e.g. showing "Login" vs "Profile" before the first API call).

### Hooks & Providers
- [MODIFY] `src/providers/AuthProvider.tsx`:
    - Handle silent renewal on app load (initial check).

## Data Model or Migration Changes

- None.

## API Contracts

### Refresh Token Endpoint (Existing)
`POST /api/v1/auth/refresh`
- Request: Credentials (cookies)
- Response: `AuthenticationResponse` (JSON + Set-Cookie)

## Security and Privacy Considerations

- **HTTP-only Cookies**: Access and Refresh tokens are now stored in cookies, which are inaccessible to JavaScript, protecting against XSS.
- **withCredentials**: Axios must be configured to send cookies with cross-origin requests (since backend is on 8080 and frontend on 3000).
- **Infinite Refresh Loops**: Ensure the interceptor doesn't get stuck in a loop if the refresh token itself is invalid.

## Testing and Verification Plan

- **Manual Verification**:
    1. Log in.
    2. Wait for access token to expire (or manually tamper with it if possible, though it's HTTP-only). 
    3. *Trick*: Change backend access token TTL to 1 minute for testing.
    4. Perform an API action and verify (via Network tab) that:
        - The first request fails with 401.
        - A call to `/api/v1/auth/refresh` is made automatically.
        - The original request is retried and succeeds.
    5. Verify that logout clears the cookies.

## Acceptance Criteria

- [ ] Users stay logged in as long as the refresh token is valid.
- [ ] 401 errors trigger a background refresh instead of immediate redirect to login.
- [ ] Multiple simultaneous 401s are queued and resolved after a single refresh call.
- [ ] Cookies are correctly sent with all API requests.

## Open Questions

- Since we can't read the HTTP-only cookie in JS, how does the frontend know if it's "logged in" on page load without making an API call? 
    - *Proposed Solution*: Backend can set a non-http-only cookie (e.g. `logged_in=true`) or we keep a flag in `localStorage`. Alternatively, just call `/api/v1/users/me` on load.
