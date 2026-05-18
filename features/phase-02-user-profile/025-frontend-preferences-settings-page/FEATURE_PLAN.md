# Feature Plan: Build Preferences Settings Page

This plan covers the implementation of the Preferences Settings page in the Next.js frontend, allowing users to view and update their system preferences.

## Source Checkbox
- [ ] Frontend: Build Preferences settings page (from `MyOS_TODO.md` - Phase 2)

## Goal and User Value
Provide a user-friendly interface for managing system-wide settings. This ensures users can easily configure how the AI agents behave (e.g., what jobs to search for, spending limits, and notification preferences) without interacting with raw APIs.

## Scope
- Create a new settings page (`/settings/preferences` or similar).
- Implement a form to display current preferences fetched from the backend.
- Allow users to update their preferences via the form.
- Use Shadcn UI components for a premium look and feel (Switch for notifications, Input for budget, Multi-select/Tags for job types).
- Add success/error toast notifications.
- Ensure the page is protected and only accessible to logged-in users.

## Out of Scope
- Backend implementation (already completed in task 024).
- Advanced profile management (covered in task 022).

## Frontend Changes

### Components
- `PreferencesForm`: A client component using `react-hook-form` and `zod` for validation.
- `JobTypeSelector`: A custom multi-select or tag input for job categories.

### Pages
- `app/(dashboard)/settings/preferences/page.tsx`: The main entry point for the preferences settings.

### API Integration
- `preferences.ts` (Service layer):
    - `getPreferences()`: Calls `GET /api/v1/preferences`.
    - `updatePreferences(data: PreferencesRequest)`: Calls `PUT /api/v1/preferences`.

## Design and UX
- **Layout**: Use a clean, card-based layout with clear sections for "Job Search", "Financials", and "Notifications".
- **Visuals**: Premium glassmorphism effects consistent with the Landing Page.
- **Feedback**: Immediate visual feedback (loading states, toast on save).

## Security
- Use the existing `AuthInterceptor` to include JWT tokens in requests.
- Redirect unauthenticated users to the login page.

## Testing and Verification Plan
- **Manual Testing**:
    - Verify that current preferences are correctly loaded on page open.
    - Verify that changing values and clicking "Save" updates the backend.
    - Verify that validation works (e.g., negative budget limit).
    - Verify that toast notifications appear correctly.

## Acceptance Criteria
- [ ] Page is accessible at `/settings/preferences`.
- [ ] Form correctly displays data from `GET /api/v1/preferences`.
- [ ] Submitting the form successfully calls `PUT /api/v1/preferences` and updates the UI.
- [ ] UI follows the project's premium design standards.
- [ ] Code is well-documented with educational comments.

## Open Questions
- Should we combine this with the general "Profile" settings or keep it as a separate tab/page? (Current plan assumes a dedicated page for clarity).
