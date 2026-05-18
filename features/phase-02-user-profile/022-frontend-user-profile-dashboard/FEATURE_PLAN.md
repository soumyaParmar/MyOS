# Feature Plan: Frontend User Profile Dashboard and Edit Forms

## Source Checkbox

- Phase: 2 — User Profile & Preferences
- Checkbox: `Frontend: Build User Profile dashboard and edit forms`
- Current status: `[ ]`

## Goal

Provide a user interface for the authenticated user to view and edit their MyOS profile. The profile data (`bio`, `skills`, `goals`, `resumeText`) is critical context for the various AI agents (Job Agent, Learning Agent, etc.) that will be built in future phases. This feature will give users full control over the personal context they provide to the AI.

## Scope

- Create a `Profile` page in the Next.js frontend (`app/(authenticated)/profile/page.tsx` or similar).
- Implement a view-mode dashboard displaying the user's current profile information.
- Implement an edit-mode form using `react-hook-form` and `zod` for validation.
- Integrate with the backend `GET /api/v1/profile` and `PUT /api/v1/profile` endpoints.
- Add loading states and error handling using toast notifications (e.g., Shadcn UI Toaster).
- Ensure the UI is responsive and adheres to the established Shadcn UI design system.

## Out Of Scope

- Updating the core `User` entity details (like email or password). This is strictly for `UserProfile`.
- Real-time collaborative editing.
- AI generation of the profile (that will come in a later phase).

## Backend Plan

- None required. The backend REST APIs (`GET /api/v1/profile` and `PUT /api/v1/profile`) were built and tested in the previous steps. 

## Frontend Plan

- **API Integration:** Create functions in an api service (e.g., `src/lib/api/profile.ts`) to fetch and update the profile data using the configured Next.js proxy/Axios interceptors.
- **State Management:** Use standard React hooks (`useState`, `useEffect`) or a data fetching library like React Query (if already set up) to manage the profile state.
- **UI Components:** 
  - Use Shadcn UI components: `Card`, `Form`, `Input`, `Textarea`, `Button`, `Skeleton` (for loading state).
  - The dashboard will likely have a "View" and "Edit" toggle, or separate components for reading vs. editing.
- **Form Validation:** Use `zod` to create a schema matching `UserProfileUpdateRequestDTO` (with appropriate max length rules if necessary).

## Data And Migrations

- None required for this step.

## API Contract

- **GET `/api/v1/profile`** -> Returns `UserProfileResponseDTO`
- **PUT `/api/v1/profile`** -> Accepts `UserProfileUpdateRequestDTO`

## Security And Privacy

- The page must be protected, requiring an authenticated session. (Next.js middleware or HOC should already handle this for the `(authenticated)` route group).
- Only the currently authenticated user's profile is accessible/editable, enforced by the backend's `SecurityContextHolder`.

## Testing And Verification

- **Manual Verification:**
  1. Log in to the application.
  2. Navigate to the Profile page.
  3. Verify that empty/null fields are handled gracefully (e.g., "No bio provided").
  4. Click "Edit" and modify the fields.
  5. Save the profile and verify a success toast appears.
  6. Refresh the page and confirm the updated data persists.

## Acceptance Criteria

- [ ] A protected Profile page is accessible from the main navigation/dashboard.
- [ ] The page correctly fetches and displays the user's current profile.
- [ ] An edit form allows updating `bio`, `skills`, `goals`, and `resumeText`.
- [ ] Form submission successfully calls the PUT endpoint and updates the UI.
- [ ] Loading and error states are handled gracefully with appropriate UX feedback.

## Open Questions

- Should we use a single page with inline editing, or a separate "Settings / Profile" layout with a dedicated edit tab? (Recommendation: Inline editing within a Card component for a smoother UX).
