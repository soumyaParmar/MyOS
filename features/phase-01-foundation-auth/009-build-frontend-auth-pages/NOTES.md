# Implementation Notes - Frontend Auth Pages

## Dependencies to Install
- `react-hook-form`
- `zod`
- `@hookform/resolvers`

## Shadcn Components Needed
- `npx shadcn@latest add card input label alert`

## API Service Structure
```typescript
// src/services/auth.service.ts
const backendUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const triggerSocialLogin = (provider: 'google' | 'github') => {
  window.location.href = `${backendUrl}/oauth2/authorization/${provider}`;
};
```
