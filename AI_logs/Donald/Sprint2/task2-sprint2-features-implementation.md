Task: Finish the Sprint 2 features (applications, recruiter review, profile, job search), fix the migration history, and prepare demo data

Date: 2026-10-26

AI Tool: Claude Code (VS Code extension), model Claude Opus 5.5



Purpose of AI Use: I used Claude to implement the plan from task 1 on the `sprint2-features` branch, bring in Hiru's job search backend, recreate the `V7` migration nobody had committed, add demo data for our presentation, and run the app locally so I could test it.



Chat Link or Prompt/Response: see Appendix A

AI-Suggested Content:
- Backend:
  - Recruiters can see who applied to each posting, move applications forward (Applied → Under Review → Interview → Offered/Rejected, where the last two are final), download an applicant's CV, and close or reopen a posting. Another recruiter's postings return 404
  - The recruiter's postings list shows location, status and real applicant counts. New postings take a location, category and job type, and need a future deadline
  - Closed postings stop accepting applications, and applications now show the job title and company
  - `GET`/`PUT /api/profile` saves headline, About, skills and location
- Hiru's job search (`GET /api/jobs`, `GET /api/jobs/{id}`), merged so her commit keeps her name, then updated to our current schema
- `V7__add_resume_versions.sql` recreated from the live Supabase table (resume gets its own id and a "current" flag), and the resume code updated so uploading keeps old versions
- Migrations `V8` (profile fields), `V9` (generated post ids) and `V10` (Hiru's search columns, renumbered from her `V7`)
- Frontend:
  - New job search and job detail pages with an Apply button
  - The home feed shows real postings, and My Applications has a progress bar for each application
  - The profile saves to the database
  - The recruiter postings table uses real data, and a new applicants page lets recruiters change statuses
- Removed three unused files, and added 28 backend tests
- Demo data scripts (`demo-data/`): 4 demo companies and 12 postings, kept outside the migrations so they never run automatically



Validation:
- Tests: Claude ran the suite after every step: 47, then 66, then 74 tests, all passing at the end
- JavaScript syntax check: this caught a bug Claude had introduced in `shell.js` (a broken regular expression) that would have stopped every job seeker page from loading. It was fixed and rechecked
- End-to-end run: Claude started the app on a local database and went through the whole flow over HTTP: post a job, save a profile, upload a CV, apply, the recruiter moves the application to Interview and downloads the CV, and the job seeker sees "Interview". Every page loaded
- Job search on the demo data: 11 open jobs, "java" found 3, Toronto + Finance found 1, and the closed job returned 404
- `V7`: checked against the real `resume` table on Supabase, ran cleanly on a fresh local database, and Flyway's validate on Supabase reported only the expected checksum difference for `V7`
- Demo data: ran the seed on Supabase twice, with the same 4 companies and 12 postings both times, and our 2 existing postings untouched
- My own testing in the browser on the local app: in progress



Decision:
- Recruiter, profile, application and frontend work: Accepted
- Running the app: Altered. I asked for it to run against Supabase, but Claude ran it on a local database, because starting this branch on Supabase would apply unreviewed migrations to everyone's database
- Demo jobs: Accepted, as a separate removable script instead of a migration
- Hiru's endpoints: Accepted. I decided to port them once she confirmed she wasn't merging
- `V7`: Accepted. Hiru confirmed she wasn't committing it, so I decided we would recreate it
- `flyway repair` on Supabase: not run yet. It updates the stored `V7` checksum on the shared database, so I'm running it myself before the PR is merged
- Claude's first version of `shell.js`: Rejected by the syntax check and fixed
- Deferred to Sprint 3: editing postings, the recruiter home page's stats, choosing between saved resume versions, and storing profile photos on the server



Reflection: Checking every step mattered more than the code itself. A syntax check caught a bug that would have broken most pages, and the end-to-end run showed the whole flow working before I tried it in the browser. Merging Hiru's branch instead of copying her code kept her authorship visible. The trickiest part wasn't code at all: a migration had been run on the shared database without being committed, and it had to be rebuilt from the live table.

Responsible Person: Donald Champ (40270957)



Appendix A: Prompt / Response history
(Prompts are paraphrased.)

Prompt 1: "Proceed with option (a)."

Response 1 (summary): Claude created `sprint2-features`, merged in Dani's fix, and added the recruiter endpoints, the posting status check, job titles on applications, the profile API and `V8`/`V9`, with 19 new tests.
Validation: Accepted. 66 tests passing.

Prompt 2: "Can you confirm that every part of the application that should be persisted is actually connected to the database?"

Response 2 (summary): A list of what was database-backed, what was still fake, and what was out of scope. Then Claude connected the profile page, recruiter pages, tracker and job pages.
Validation: Accepted. Confirmed by the end-to-end run.

Prompt 3: "Please seed the database with realistic sample job postings for our demo."

Response 3 (summary): Seed and cleanup scripts with 4 demo companies (with recruiter logins), 11 open postings and 1 closed. Claude ran the seed on Supabase twice to check it doesn't create duplicates.
Validation: Accepted.

Prompt 4: "Please launch the application so I can test it myself."

Response 4 (summary): Claude ran it locally instead of on Supabase, loaded the demo jobs, and created a demo job seeker with applications at every stage.
Validation: Altered (local database instead of Supabase).

Prompt 5: "Hiru has confirmed she won't be merging her branch before submission. Should we take over her endpoints and fix them ourselves?"

Response 5 (summary): Yes, since job search is required. Claude merged Hiru's branch, resolved the conflicts against the current schema, renumbered her migration to `V10`, updated her tests and added four, and added category and job type to the create-posting form.
Validation: Accepted. 74 tests passing.

Prompt 6: "Hiru isn't planning to commit the `V7` migration that's already on Supabase, so we should recreate it ourselves."

Response 6 (summary): Claude read the live `resume` table and the Flyway history, wrote a `V7` that makes the same change, updated the resume code to keep old versions, and rebuilt the local test database. Flyway's validate showed that Supabase needs a one-time `flyway repair` before the branch can start there.
Validation: Accepted. The repair itself is left for me to run.

Prompt 7: "Help me write up my AI logs for this work, with a validation section for each output."

Response 7 (summary): Claude helped draft task 1 and this file from the session.
Validation: Altered. I chose the wording and reviewed every entry.
