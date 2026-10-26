**Task ID/Title:** Task 2: Profile page front-end (Sprint 2)

**Purpose of AI Use:** Code generation and UI/UX guidance for a LinkedIn-inspired profile page in the app's colour scheme.

**Chat Link or Prompt/Response:** https://claude.ai/share/b4dcafff-0e8f-4749-b310-78aff6a28190. Key prompt: build a modern, colourful profile page with a wide cover banner users can upload, an overlapping circular avatar with an edit button, and sections for details, experience and bio.

**AI-Suggested Content:** Claude generated `profile.html` and `js/profile.js`, opened from the avatar in the navigation bar. It has cover and photo upload, an "Edit profile" dialog (headline, location, About), sample Experience, Education and Skills sections, and a read-only CV card that only reads the existing `GET /api/resume`. Profile data is saved in the browser only, not on the server, but only for now.

**Validation:** Claude tested uploads, the edit dialog, saved values across pages and responsive layout in a headless browser, using a simulated resume response. Opened the profile, uploaded photos, edited and saved the intro.

**Decision:** Accepted with limits. Used as a template; saving the profile on the server has still to be done later and the sample sections are placeholders. Merged to `main` in pull request #76.

**Reflection:** The "create and edit profile" requirement still needs backend storage.

**Person:** Mario Habib