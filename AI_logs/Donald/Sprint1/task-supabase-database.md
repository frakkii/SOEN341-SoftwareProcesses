Task: Connect the Spring Boot backend to the team's Supabase (PostgreSQL) database

Date: 2026-09-26

AI Tool: Claude Code (VS Code extension), model Claude Opus 5.5



Purpose of AI Use: AI was used to plan how to connect the backend to Supabase, adapt the registration and login code to the tables the team had already created, write database migrations, run the app against Supabase, and prepare the pull request.



Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:
- A plan to connect Spring Boot directly to Supabase's PostgreSQL database over JDBC (using the Session pooler connection, since the direct connection only works over IPv6), keeping the existing backend login instead of switching to Supabase Auth
- Configuration in `application.properties` that reads the database password from the `SUPABASE_DB_PASSWORD` environment variable so it is never committed
- An analysis of the existing `person_user` and `employer_user` tables, which found problems that would stop registration from working: required columns the sign-up form didn't collect, ids that were never generated, no unique constraint on email, and `phone` stored as an integer (too small for a 10-digit phone number)
- Flyway migrations: V1 records the existing tables (skipped on Supabase, where they already exist), V2 makes emails unique and changes `phone` to text, V3 lets the database generate user ids
- Code changes: `PersonUser` and `EmployerUser` entities with repositories, request classes for register and login, an updated `AuthController` (job seekers saved to `person_user`, recruiters to `employer_user`, one email per account across both tables, validation of every field against its column size)
- An expanded sign-up form in `index.html` (last name, phone, address, city, province/state, country, postal code, and company name for recruiters)
- Tests that run against an in-memory H2 database so they don't need the Supabase password, plus new tests for the new validation rules
- README instructions for setting the password and adding future migrations
- A pull request title and description for the team



Validation:
- Automated: the Maven test suite (`mvnw test`) passed with all 19 tests after each change
- Dry run: the V2 and V3 migrations were run on Supabase inside a transaction that was then rolled back, to confirm they worked on the real tables without changing anything
- Live run: the app was started against Supabase. The first attempt failed (a statement in V3 timed out waiting on a lock held by Flyway itself); the AI removed that statement and the migrations then applied successfully
- Smoke test with curl: the page loaded (200) and a login with an unknown email returned 401, confirming the app queries the database
- Manual review: I made the design decisions (see below) and reviewed the results before opening the pull request



Decision:
- Plan approved: connect Spring Boot directly to Supabase rather than using Supabase Auth
- Modified: the AI's first version created its own `users` table. I pointed out that the team already had `person_user` and `employer_user` tables, and the AI redesigned the code around them
- I chose to expand the sign-up form to collect all required fields (instead of making those columns optional), to manage schema changes with Flyway migrations in the repo, and to keep the existing rows in the tables
- Modified: the AI stored passwords as BCrypt hashes. I decided to store them as plain text to keep the school project simple, after the AI explained the trade-off
- Approved stopping an old copy of the app that was blocking port 8081
- Deferred: enabling row-level security on Flyway's history table (a one-line SQL statement to run in Supabase) and resetting the database password to a stronger one



Reflection: AI was useful for spotting problems in the existing database design that would have caused confusing errors later, such as the phone column being too small and ids not being generated. Its first plan assumed an empty database, so it was important to share what already existed in Supabase. Asking questions before implementing kept the important choices (form fields, migrations, password storage) with me.

Responsible Person: Donald Champ (40270957)



Appendix A: Prompt / Response history
(Prompts are paraphrased.)

Prompt 1: "Walk me through integrating our Supabase database with the project, and put together an implementation plan."

Response 1 (summary): The AI reviewed the backend and found users were stored in memory and lost on every restart. It recommended connecting Spring Boot directly to Supabase's PostgreSQL database and keeping the existing login code. The plan covered: getting the Session pooler connection details from the Supabase dashboard, adding the JPA, PostgreSQL and Flyway dependencies, reading the password from an environment variable, creating the table with a migration, turning users into database entities, running tests against an in-memory database, and documenting the setup. It also recommended enabling row-level security so Supabase's public REST API couldn't read password data.

Prompt 2: "Here are the Supabase connection details (host, port, database, user). Proceed with the implementation."

Response 2 (summary): The AI created the `feature/supabase-db` branch and implemented the plan with a new `users` table. All 15 existing tests passed. It couldn't test against Supabase because the password wasn't set on the machine.
Acceptance: Implementation accepted, later replaced (see Prompt 3).

Prompt 3: "Our Supabase already has a schema: here are the `person_user` and `employer_user` table definitions, along with the database credentials. Align the implementation with it."

Response 3 (summary): The AI inspected the database structure with read-only queries. It found two more tables (`job_postings`, `fav_postings`) and that row-level security was already on. It also listed the problems that would block registration (required fields not collected, no generated ids, no unique email, integer phone). It asked three questions: how to handle the extra required fields, how to apply the fixes, and what to do with the existing rows. It then rebuilt the code around the existing tables and added the V1 to V3 migrations and the expanded sign-up form. All 19 tests passed, and the migrations were dry-run on Supabase and rolled back.
Acceptance: I chose to expand the sign-up form, use Flyway migrations, and keep the existing rows.

Prompt 4: "Launch the web app so we can see it running against Supabase."

Response 4 (summary): The first start failed: one statement in V3 (enabling row-level security on Flyway's history table) timed out because Flyway holds a lock on that table. The AI removed the statement and restarted, and the migrations applied. The next start failed because an old copy of the app was using port 8081. After I approved stopping it, the app started at http://localhost:8081, and a page load and login check passed.
Acceptance: Approved stopping the old app. Enabling row-level security on the history table was left as a manual step.

Prompt 5: "Are the passwords being encrypted in the database?"

Response 5 (summary): The AI explained that passwords were hashed with BCrypt, not encrypted, and why hashing is the usual choice (it can't be reversed). It noted that the two existing rows might not be hashed.

Prompt 6: "Hashing adds more complexity than this school project needs. Store passwords as plain text instead."

Response 6 (summary): The AI explained the trade-off (anyone with database access can read the passwords) and then removed BCrypt. Passwords are now stored as typed and compared directly at login, with a 255-character limit to match the column. All 19 tests passed and the app was restarted.
Acceptance: Change accepted.

Prompt 7: "Open a pull request for this work."

Response 7 (summary): The AI committed the work and pushed the branch. The GitHub CLI wasn't installed, so it provided the pull request link along with a title and description written for the team.

Prompt 8: "Add my AI log for this task, with my prompts phrased at a higher level, and record my work in the member contribution file."

Response 8: This log file and the update to `documentation/Sprint1_MemberContribution`.
