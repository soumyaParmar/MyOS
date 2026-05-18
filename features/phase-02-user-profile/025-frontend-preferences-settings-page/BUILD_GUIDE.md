# Build Guide: Frontend Preferences Settings Page

Welcome to the frontend implementation of the Preferences system! Now that we have the backend API ready, we'll build a premium settings page to manage these configurations.

## What we are building
A dedicated settings page in the Next.js dashboard where users can manage:
1. **Job Search Preferences**: Keywords/Categories for the Job Agent.
2. **Financial Limits**: Monthly budget limits for the Finance Agent.
3. **Notifications**: Toggle email and push notifications.

---

## Task 1: Create the Preferences Service
We need a way to communicate with our new backend endpoints.

### Instructions
1. Create (or update) a service file at `frontend/src/services/preferences.service.ts`.
2. Implement `getPreferences` and `updatePreferences` methods.
3. Use your existing Axios instance (which should already handle tokens).

### Significance
Decoupling API calls into a service layer makes your components cleaner and easier to test. It also centralizes error handling and data transformation.

---

## Task 2: Build the Preferences Form
We'll use `react-hook-form` and `zod` for a robust, type-safe form.

### Instructions
1. Use Shadcn UI components: `Card`, `Form`, `Input`, `Switch`, `Button`.
2. Define a Zod schema that matches your backend DTO.
3. Handle the loading state while fetching initial data.
4. Add a "Save Changes" button that triggers the update API.

### Educational Concept: Zod & React Hook Form
Using **Zod** for schema validation ensures that the data entered by the user is valid before it even reaches your backend. **React Hook Form** manages the form state efficiently, preventing unnecessary re-renders.

---

## Task 3: Create the Settings Page
Now, assemble everything into a beautiful page.

### Instructions
1. Create `app/(dashboard)/settings/preferences/page.tsx`.
2. Wrap the form in a premium card layout.
3. Add a clear header and description.
4. Integrate toast notifications for success/error feedback.

---

## Deep Dive: Glassmorphism & UI Consistency
To maintain the "Premium" look of MyOS, remember to:
- Use `backdrop-blur` and semi-transparent backgrounds for cards.
- Use a consistent color palette (primary colors for actions).
- Add subtle hover effects to interactive elements.

---

## Verification Checklist
- [ ] Does the page load current settings correctly on mount?
- [ ] Does the "Save" button successfully update the database?
- [ ] Are toast notifications visible on success?
- [ ] Is the form responsive and accessible?

---

Happy coding! You're one step closer to a fully functional AI Operating System!
