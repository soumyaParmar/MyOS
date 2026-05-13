# Feature Plan: Frontend SSR/ISR Auth & Middleware

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: `[ ] Frontend: Leverage Next.js SSR/ISR for authenticated pages`
- Current status: `[ ]`

## Goal and User Value

Now that tokens are stored in HTTP-only cookies, we can leverage Next.js server-side capabilities to improve security and performance:
1. **Security**: Prevent "flash of unauthenticated content" (FOUC) by checking auth on the server before rendering the page.
2. **UX**: Faster initial loads for authenticated users by pre-fetching data on the server.
3. **Robustness**: Implement a centralized `middleware.ts` to protect all private routes (`/dashboard`, `/profile`, etc.).

## Scope

- Implement `src/middleware.ts` to intercept requests to protected routes.
- Create a server-side authentication utility to verify session/roles in Server Components.
- Update `src/app/dashboard/page.tsx` (and other protected pages) to use server-side session checks.
- Handle token expiration and redirect logic in the middleware.
- Ensure public routes (`/login`, `/signup`, `/`) are not blocked.

## Out of Scope

- Next.js Proxy configuration (this will be the next task: `015-frontend-proxy`).
- Advanced ISR (Incremental Static Regeneration) for user-specific data (sticking to SSR for now for security).

## Backend Changes

- None.

## Frontend Changes

### Middleware
- [NEW] `src/middleware.ts`: 
    - Checks for `access_token` and `refresh_token` cookies.
    - Redirects to `/login` if both are missing.
    - Handles "Protected" vs "Public" route matching.

### Server-Side Utilities
- [NEW] `src/lib/auth-server.ts`:
    - Helper function `getServerSession()` that reads cookies and optionally calls `/api/users/me` from the server to verify.

### Components & Pages
- [MODIFY] `src/app/dashboard/page.tsx`:
    - Convert to a Server Component (or wrap in a server check).
    - Use `getServerSession()` for initial auth.
- [MODIFY] `src/app/layout.tsx`:
    - Pass initial user data to `AuthProvider` if available on the server.

## Data Model or Migration Changes

- None.

## API Contracts

- None (using existing endpoints).

## Security and Privacy Considerations

- **Middleware Security**: The middleware runs on every request. It must be efficient and not leak sensitive info.
- **Token Verification on Server**: Since the middleware can't easily verify the JWT signature (it doesn't have the secret, unless we share it), it primarily checks for token presence. Full verification happens in Server Components or during API calls.
    - *Alternative*: Share the JWT secret with the frontend (via env var) so the middleware can actually verify the token. This is recommended for true SSR protection.

## Testing and Verification Plan

- **Manual Verification**:
    1. Log in.
    2. Try to access `/dashboard` directly via URL. It should load immediately without a loading spinner.
    3. Log out.
    4. Try to access `/dashboard` directly via URL. You should be redirected to `/login` *before* the dashboard renders.
    5. Test with expired `access_token` but valid `refresh_token`. The middleware should ideally allow the request through so the client-side interceptor can refresh, OR the server itself could try to refresh (more complex). 
    *Decision*: Let Middleware check for *any* valid-looking session cookie. If both are missing, redirect.

## Acceptance Criteria

- [ ] Private routes are inaccessible without session cookies.
- [ ] No "flash of unauthenticated content" when accessing protected pages directly.
- [ ] Middleware correctly ignores public assets and auth pages.
- [ ] `getServerSession` correctly identifies the user in Server Components.

## Open Questions

- Should we share the `JWT_SECRET` with the Next.js frontend?
    - *Answer*: Yes, this allows the middleware to verify the `access_token` signature locally without making a network call to the backend, which is much faster.
