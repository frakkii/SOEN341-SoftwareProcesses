**Task ID/Title:** Frontend UI overhaul: navigation bar, Tools menu fix, home feed and resume page restyle (Sprint 2)

**Purpose of AI Use:** Code generation, debugging and UI/UX guidance to modernise the post-login front end while keeping the colour scheme, login page and backend logic unchanged.

**Chat Link or Prompt/Response:** https://claude.ai/share/b4dcafff-0e8f-4749-b310-78aff6a28190. Key prompts included: a request to act as an expert frontend engineer and overhaul the job-search UI (keep the colours, do not change the backend or login page, use the full screen); a fix for the three Tools items that were invisible until hovered; a modern feed with a "Create a Post" box; and a profile avatar in the top right of the navigation bar.

**AI-Suggested Content:** Claude found that the Tools items were white text on a white panel because of a CSS conflict. It created a separate `css/app.css` (so the shared login stylesheet stays untouched), `js/shell.js` (shared navigation bar with profile avatar), `js/feed.js` (job and post cards, composer, Like, Comment, Share, Save, Apply, filters), a rewritten `home.html`, and a restyled `resume.html` that keeps the original upload script. The feed content is sample data and is not saved.

**Validation:** Claude tested in a headless browser at widths from 390 to 2560 px and with scripted interaction tests, and confirmed the login page, `auth.js` and the backend were unchanged. Ran `run.cmd`, checked the Tools menu, feed and resume upload at `localhost:8081`.

**Decision:** Modified before use. A teammate's pull request #74 changed `home.html` and `resume.html` at the same time, so I kept my redesign and re-applied their change in the new JavaScript: "My Applications" and the Applications tile now open `applications.html`.

**Reflection:** Used AI for finding the menu bug quickly and getting a layout, but the AI did not know about my teammates parallel changes, so the merge had to be handled by the team and me. The feed is only a front-end shell and still needs real backend data.

**Person:** Mario Habib