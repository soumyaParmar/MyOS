# Feature Plan: Build Frontend Auth Pages (Login/Signup)

## Source Checkbox
- [ ] Frontend: Build Login/Signup pages (Google/GitHub integration)

## Goal and User Value
Provide a user-friendly interface for users to register and log in to MyOS. Supporting social logins (Google/GitHub) reduces friction and improves security perception.

## Scope
- Create `/login` page with local email/password form and social login buttons.
- Create `/signup` page with local registration form.
- Integrate with backend authentication endpoints:
    - `POST /api/auth/register`
    - `POST /api/auth/login`
- Handle OAuth2 redirect to backend endpoints:
    - `/oauth2/authorization/google`
    - `/oauth2/authorization/github`
- Handle validation and error messaging for auth forms.

## Out of Scope
- Password reset flow (forgot password).
- Email verification flow.
- JWT storage logic (this is the *next* checkbox).

## Backend Changes
- None (API already exists or was implemented in previous steps).

## Frontend Changes

### Components (Shadcn UI)
- [NEW] `Input`: Basic text/password input.
- [NEW] `Label`: Accessible form labels.
- [NEW] `Card`: Container for auth forms.
- [NEW] `Alert`: Displaying error messages.
- [NEW] `Icons`: Google and GitHub brand icons.

### Pages
- [NEW] `/src/app/login/page.tsx`: The login screen.
- [NEW] `/src/app/signup/page.tsx`: The registration screen.

### Logic
- Form handling using `react-hook-form` and `zod` for validation.
- Service layer for API calls to the backend.

## Data Model or Migration Changes
- None.

## API Contracts
- Authentication Service:
    - `register(name, email, password)`
    - `login(email, password)`
- OAuth2 trigger: `window.location.href = backendUrl + "/oauth2/authorization/{provider}"`

## Security and Privacy Considerations
- Ensure password inputs use `type="password"`.
- Use HTTPS for all communications.
- Validate inputs on both client and server.

## Testing and Verification Plan
- Manual testing of form validation (empty fields, invalid email).
- Manual testing of navigation between login and signup.
- Verifying social login buttons redirect to the correct backend endpoint.

## Acceptance Criteria
- User can see and interact with login/signup forms.
- Form validation displays errors for invalid input.
- Social login buttons are visible and functional.
- Layout is responsive and follows the project's design aesthetic.

## Open Questions
- Do we have a specific design system or theme (Dark/Light) to prioritize?
- Should we use a specific icon library (e.g., `react-icons` or `lucide-react`)?
