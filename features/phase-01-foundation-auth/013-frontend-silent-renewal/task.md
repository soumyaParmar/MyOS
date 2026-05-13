# Tasks: Frontend Silent Renewal & Interceptor

- [x] Configure Axios for cookies (`withCredentials: true`)
- [x] Implement Silent Refresh Interceptor in `src/lib/api.ts`
    - [x] Create request queue for multiple 401s
    - [x] Handle refresh token logic
    - [x] Retry original request on success
    - [x] Redirect to login on failure
- [x] Update `src/services/auth.service.ts` with `refreshToken` method
- [x] Update `src/lib/token.ts` to manage non-sensitive login flag
- [x] Update `src/providers/AuthProvider.tsx` for initial session check
- [x] Verify implementation with manual testing
