Task: Connect the frontend login page to the Spring Boot backend, and clean up the repository

Date: 2026-09-25

AI Tool: Claude Code (VS Code extension), models Claude Sonnet 5 and Claude Opus 5.5



Purpose of AI Use: AI was used to explore the codebase, explain why the frontend and backend were not connected, plan and implement the connection using Spring Boot, launch a demo, and review the codebase for issues and redundancies.



Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:
- An explanation that the repo had two unrelated servers (a plain Java `MainServer.java` that only served `login.html`, and a Spring Boot app with `/api/auth/register` and `/api/auth/login`) and that the page's JavaScript never called the API
- A plan to have Spring Boot serve both the page and the API from one app
- Code changes: a `role` field (Job Seeker / Recruiter) added to `User.java` and `AuthController.java`; the login page rewritten to call the API with `fetch`, with a Sign In / Register toggle and inline error messages; `MainServer.java` deleted; README setup instructions
- A Maven build setting in `pom.xml` so frontend files stay in `src/webapp/` and are copied into Spring Boot's static resources at build time
- A list of 14 issues and redundancies in the codebase
- A cleanup: removed a broken nested repo link, committed build output (`out/`), IntelliJ settings (`.idea/`) and empty placeholder files; added a root `.gitignore`



Validation:
- Automated: the Maven test suite (`mvnw test`) passed after the changes
- API checks with curl: register (200), duplicate email (409), invalid role (400), correct login (returns name and role), wrong password (401)
- Demo: the app was launched at http://localhost:8081 (port 8080 is used by a local Apache server) and opened in the browser for review
- Manual review: I reviewed the plan before any code was written and approved it



Decision:
- Plan approved as written
- Modified: I asked for all frontend files to stay in the `src/webapp/` folder instead of the Spring Boot `static/` folder; the AI changed the build to copy them from `src/webapp/`
- Of the 14 flagged issues, I accepted fixes for items 1-5 (repo clutter and redundancy). Items 6-14 (thread safety, input validation, request classes, Spring Security defaults, placeholder names, real sessions, demo text, README database claim, missing tests) are kept for later, mostly alongside the MySQL work



Reflection: AI was useful for quickly understanding an unfamiliar codebase and for spotting problems that were easy to miss, such as the broken nested repo link and the port conflict with Apache. Reviewing the plan before implementation made it easy to steer the result (keeping frontend files in `src/webapp/`).

Responsible Person: Donald Champ (40270957)



Appendix A: Prompt / Response history

Prompt 1: "Explore the code base and explain the steps I must take to connect the frontend and backend"

Response 1 (summary): The AI read the project files and found:
- `src/MainServer.java` was a basic Java HTTP server on port 8080 that only returned `login.html`
- `src/backend/` was a Spring Boot app with `/api/auth/register` and `/api/auth/login` (BCrypt passwords, users stored in memory), also on port 8080
- `login.html` never called the backend; it only showed and hid parts of the page, and it asked for a username while the backend expects an email
- MySQL was listed in the README, but no database was actually set up
Suggested steps: have Spring Boot serve the page, rewrite the page's JavaScript to call the API, only add CORS if the frontend and backend run on different ports, optionally add MySQL, then test end to end.

Prompt 2: "Create a plan to best connect the front and backend, we want to use Springboot. I will then approve and we can go from there"

Response 2 (summary): The AI wrote a plan: (1) move the page into Spring Boot and delete `MainServer.java`; (2) add a `role` to the backend user; (3) rewrite the page to call `/api/auth/login` and `/api/auth/register`, with a Sign In / Register toggle and inline errors; (4) update the README. MySQL and real login sessions were left out of scope. It also included a verification checklist.
Acceptance: Plan approved. The AI then implemented it, ran the tests (passed), found that port 8080 was taken by a local Apache (`httpd`) server, and verified every API case with curl on port 8081.

Prompt 3: "Whats the status?"

Response 3 (summary): The work was done and tested at the API level but not committed and not yet clicked through in a browser; port 8080 was blocked by Apache.

Prompt 4: "First, try and keep all frontend files in the webapp folder. Second, let's launch and demo"

Response 4 (summary): The page was moved back to `src/webapp/` (renamed `index.html` so it loads at `/`), and `pom.xml` was changed to copy `src/webapp/` into the app at build time. The app was launched at http://localhost:8081 and opened in the browser, with a demo account (demo@careerconnect.com / demo123) and demo steps provided.
Acceptance: Modification requested (frontend location) and applied.

Prompt 5: "Can you identify any issues or redundancies in the codebase and flag them to me"

Response 5 (summary): 14 issues were flagged:
1. `SOEN341-SoftwareProcesses-main` is a broken nested repo link that clones as an empty folder
2. Compiled files for the old server are committed in `out/`
3. IntelliJ settings (`.idea/`) are committed
4. No root `.gitignore`
5. Empty and leftover placeholder files (`.ai/mcp/mcp.json`, `src/.gitkeep`)
6. User storage (`HashMap`) isn't safe when two requests arrive at once
7. Empty fields pass validation; no email format or password length checks
8. `User` is used both for incoming requests and for stored records (including the password hash)
9. Spring Security's default login is unused but still prints a generated password on start
10. Placeholder names (`com.example.demo`, `DemoApplication`, empty pom fields)
11. The server doesn't remember who is logged in; refreshing logs you out
12. Hardcoded Sprint 1 demo text on the page
13. README says MySQL, but there is no database
14. Only one test (the app starts); register and login have no tests

Prompt 6: "fix 1-5"

Response 6 (summary): Git stopped tracking the nested repo link and the empty folder was deleted; `out/` was deleted; `.idea/` was untracked (the files were kept on disk); a root `.gitignore` was added (build output, IDE folders, OS files); `.ai/mcp/mcp.json` and `src/.gitkeep` were deleted.
Acceptance: Items 1-5 accepted and applied. Items 6-14 deferred.

Prompt 7: "Could you write to Donald file in AI_logs, my promping history answers and my acceptance"

Response 7: This log file.
