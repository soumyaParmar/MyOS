# Feature Plan: Initialize Frontend Vite React

## Source Checkbox

- Phase: Phase 1 — Foundation & Auth
- Checkbox: Frontend: Initialize Vite + React project (Tailwind CSS + Shadcn UI)
- Current status: `[x]`

## Goal

Set up the frontend foundation for MyOS using modern web technologies to provide a premium, high-performance user experience.

## Scope

- Initialize Vite + React (JavaScript) project in the `frontend/` folder.
- Configure Tailwind CSS for styling.
- Install essential libraries: `lucide-react`, `framer-motion`, `axios`, `lucide-react`.
- Set up the base project structure (components, pages, hooks, services).
- Create a basic "Under Construction" dashboard to verify the setup.

## Out Of Scope

- Authentication implementation (JWT/OAuth2).
- Backend API integration (except for a health check).
- Actual feature pages (Jobs, Social, etc.).

## Backend Plan

- Ensure CORS is configured in the backend to allow requests from the frontend (typically `localhost:5173`).

## Frontend Plan

- **Framework**: Vite + React.
- **Styling**: Tailwind CSS + PostCSS.
- **Components**: Basic shell using Tailwind.
- **Folder Structure**:
  ```text
  frontend/src/
    components/
    pages/
    hooks/
    services/
    assets/
    styles/
  ```

## Data And Migrations

- N/A

## API Contract

- `GET http://localhost:8080/health` (Connectivity check).

## Security And Privacy

- Initial frontend setup will be public.
- JWT storage strategy (HttpOnly cookies vs. LocalStorage) to be decided in the Auth task.

## Testing And Verification

- `npm run dev` starts the dev server.
- Dashboard renders correctly with Tailwind styles.
- Browser console shows no errors.

## Acceptance Criteria

- [ ] Vite project initialized successfully.
- [ ] Tailwind CSS configured and working.
- [ ] `npm install` completes without errors.
- [ ] Base directory structure created.
- [ ] App renders a "MyOS" landing/health page.

## Open Questions

- Should we use TypeScript? (Proposing JavaScript for now as per core rules, but can switch if requested).
- Which UI library? (Proposing Shadcn UI for a premium feel).
