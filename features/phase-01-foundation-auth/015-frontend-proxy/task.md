# Tasks: Frontend API Proxy Configuration

- [x] Configure `rewrites` in `next.config.ts`
    - [x] Proxy `/api/:path*` to `http://localhost:8080/api/:path*`
    - [x] Proxy `/oauth2/:path*` to `http://localhost:8080/oauth2/:path*`
- [x] Update `src/lib/api.ts`
    - [x] Set `baseURL` to `/api`
    - [x] Update interceptor to use relative refresh URL
- [x] Update `src/services/auth.service.ts`
    - [x] Use proxy for social login triggers
- [x] Verify implementation with manual testing
