# Implementation Notes - Frontend JWT Storage

## Token Utility
```typescript
const TOKEN_KEY = 'myos_token';
export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token);
export const removeToken = () => localStorage.removeItem(TOKEN_KEY);
```

## Axios Interceptor
```typescript
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

## Auth Provider
Use `useEffect` in the `AuthProvider` to check for a token on mount and fetch user profile if available.
