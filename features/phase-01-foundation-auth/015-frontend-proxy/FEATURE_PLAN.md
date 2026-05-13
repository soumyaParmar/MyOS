# Feature Plan: Frontend API Proxy Configuration

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: `[ ] Frontend: Configure Next.js Proxy for secure API communication`
- Current status: `[ ]`

## Goal and User Value

Currently, the frontend communicates with the backend via `http://localhost:8080` directly. This requires:
1. Hardcoding the backend URL in frontend code or environment variables.
2. Managing CORS (Cross-Origin Resource Sharing) on the backend.
3. Complexities with cookies if the domains/ports don't match exactly.

By configuring a **Next.js Rewrite (Proxy)**, we can make the frontend think the API is on its own domain (e.g., `http://localhost:3000/api/...`). Next.js will then transparently proxy these requests to the backend.

## Scope

- Update `next.config.ts` to include rewrite rules for `/api/:path*`.
- Update `src/lib/api.ts` to use a relative base URL (`/api`) instead of an absolute one.
- Update `src/services/auth.service.ts` to remove direct backend URL references for social login (or update them to use the proxy).
- Verify that cookies still work correctly across the proxy.

## Out of Scope

- Setting up a production-grade reverse proxy like Nginx (this is purely for local development and Next.js deployment environments).

## Backend Changes

- [OPTIONAL] Update `SecurityConfig.java` to restrict CORS further if needed (since requests will now come from the same "origin" as the frontend server, though they still technically have different origins during proxying if not configured as a full reverse proxy). *Decision*: Keep current CORS for now to ensure compatibility.

## Frontend Changes

### Configuration
- [MODIFY] `next.config.ts`: Add `rewrites` function.

### API Integration
- [MODIFY] `src/lib/api.ts`: 
    - Change `baseURL` to `/api` (or just remove absolute part).
- [MODIFY] `src/services/auth.service.ts`:
    - Update `triggerSocialLogin` to use the proxy or a consistent URL.

## Data Model or Migration Changes

- None.

## API Contracts

- No changes to endpoint paths.

## Security and Privacy Considerations

- **CORS Simplification**: Proxying helps avoid "Preflight" OPTIONS requests in some scenarios and makes cookie handling more predictable.
- **SSO Redirects**: Note that OAuth2 redirection still happens at the browser level, so the backend `redirect-uri` must still point back to the frontend domain correctly.

## Testing and Verification Plan

- **Manual Verification**:
    1. Start backend and frontend.
    2. Log in.
    3. Check the **Network** tab in the browser.
    4. Verify that API calls are now going to `http://localhost:3000/api/...` instead of `8080`.
    5. Verify that the app still functions correctly (auth, dashboard, etc.).

## Acceptance Criteria

- [ ] All frontend API calls are routed through the Next.js proxy.
- [ ] No direct references to port 8080 in the frontend application code (except possibly in `next.config.ts`).
- [ ] Authentication and cookies work seamlessly across the proxy.

## Open Questions

- Should we also proxy the OAuth2 login URLs?
    - *Answer*: Yes, we can proxy `/oauth2/**` to keep everything under one domain.
