# Tasks: Frontend SSR Auth & Middleware

- [x] Add `jose` dependency for JWT verification in Middleware
- [x] Add `JWT_SECRET` to frontend `.env.local`
- [x] Implement `src/middleware.ts`
    - [x] Match protected routes (`/dashboard`, etc.)
    - [x] Verify `access_token` cookie
    - [x] Redirect to `/login` if auth is missing
- [x] Implement `src/lib/auth-server.ts`
    - [x] Create `getServerSession()` helper
    - [x] Decode user data from JWT
- [x] Update `src/app/dashboard/page.tsx` to use server-side auth
- [x] Update `src/app/layout.tsx` to pass initial session to `AuthProvider`
- [x] Verify implementation with manual testing
