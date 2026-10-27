Task: Finish the Sprint 2 features (applications, recruiter review, profile, job search) and prepare demo data

Date: 2026-10-26

AI Tool: Claude Code (VS Code extension), model Claude Opus 5.5



Purpose of AI Use: AI was used to implement the plan from task 1 on the `sprint2-features` branch, bring in Hiru's job search backend, add demo data for the presentation, run the app so I could test it, and prepare the pull request.



Chat Link or Prompt/Response: see Appendix A

AI-Suggested Content:
- Backend:
  - Recruiter endpoints (`RecruiterController`, `RecruiterService`): list the applicants to a posting, move an application forward (Applied → Under Review → Interview → Offered/Rejected, where Offered and Rejected are final), download an applicant's resume, and close or reopen a posting. Postings and applications that belong to another recruiter return 404
  - The recruiter's postings list now returns location, status and real applicant counts; creating a posting takes a location, category and job type, and requires a future deadline
  - Closed postings stop accepting applications; applications now return the job title and company
  - `GET`/`PUT /api/profile` saves headline, About, skills and location
  - Migrations: `V8` (profile fields), `V9` (generated `post_id`, written to do nothing on Supabase where it already existed), and `V10` (Hiru's search columns, renumbered from his `V7`)
- Hiru's job search (`GET /api/jobs`, `GET /api/jobs/{id}`), merged with his commit kept and then updated to the current schema: renamed to `JobSearchController` to avoid a name clash, results returned as a `JobListingResponse` instead of the database entity, keyword search extended to skills, and category falling back to department
- Frontend:
  - New job search page (`jobs.html`) and job detail page with Apply (`job.html`)
  - Home feed shows real postings; the tracker shows a step-by-step status bar
  - Profile page saves to the database
  - Recruiter postings table uses real data with View applicants and Close/Reopen
  - New applicants page where recruiters change statuses
- Removed unused files (`dashboard.html`, `auth.js`, the empty `recruiterpostdetails.html`)
- 27 new backend tests (recruiter, profile, job search)
- `demo-data/seed_demo_jobs.sql` and `remove_demo_jobs.sql`: 4 demo companies and 12 postings, kept outside the Flyway migrations
- Kept Dani's applications fix in the history by merging their branch, so their commit stays under their name



Validation:
- Automated: the test suite passed after each step: 47 tests after the first backend changes, then 66, then 74 after the job search merge (`mvnw clean test`, 0 failures)
- Syntax check: every changed JavaScript file, including scripts inside the HTML pages, was checked with `node --check`. This caught a real bug the AI had introduced: a regular expression in `shell.js` was broken by a line break, which would have stopped every job seeker page from working. It was fixed and rechecked
- End-to-end smoke test: the app was run locally against an in-memory H2 database and driven over HTTP:
  - A recruiter created a posting, and a job seeker saved a profile, uploaded a CV and applied (the confirmation included the job title and company)
  - The recruiter saw 1 applicant, moved them to Interview and downloaded the CV, and the job seeker's tracker showed Interview
  - Every page returned 200
- Job search on demo data: 11 open jobs listed, keyword "java" found 3, Toronto + Finance found 1, and the closed posting's detail page returned 404
- Demo data: the seed script was run on Supabase twice. Both runs left 4 recruiters and 12 postings, so it is safe to rerun. The team's 2 existing postings were not touched
- Manual testing in the browser: in progress by me at the time of writing



Decision:
- Recruiter, profile, application and frontend work: Accepted
- Running against Supabase: Altered. I asked to run the app for testing; the AI ran it on a local database instead, because Supabase still has the unpushed `V7`, and starting the branch there would likely fail and apply unreviewed migrations to the shared database
- Demo jobs: Accepted, written to Supabase as a separate, removable script rather than a migration
- Hiru's endpoints: Accepted. The AI recommended porting them once Hiru confirmed he wouldn't merge before submission, and I agreed
- The AI's own first version of `shell.js`: Rejected by the syntax check and fixed (see Validation)
- Deferred to Sprint 3: editing a posting (only close/reopen for now), the recruiter home page's hard-coded stats, and storing profile photos on the server
- Not done by the AI: opening the PR on GitHub (the GitHub CLI isn't installed), and the missing `V7` file, which whoever ran it on Supabase must push before this PR can merge



Reflection: Checking the work at every step mattered more than writing it. The syntax check caught a bug that would have broken most pages, and the HTTP smoke test showed the whole flow working before the browser test. Bringing in Hiru's code as a merge, not a copy, kept his authorship and made the changes to his code easy to review. The biggest remaining risk is not in the code: the migration history on Supabase has to be fixed by the team before this can merge.

Responsible Person: Donald Champ (40270957)



Appendix A: Prompt / Response history
(Prompts are paraphrased.)

Prompt 1: "Go ahead with option (a)."

Response 1 (summary): The AI created `sprint2-features` from `main` and merged in Dani's fix. It then added the recruiter endpoints, the posting status check, job titles on applications, the profile API and migrations `V8`/`V9`, with 19 new tests, and committed.
Validation: Accepted. 66 tests passing.

Prompt 2: "Is everything that should be connected to the database actually connected?"

Response 2 (summary): A list of what was database-backed, what was still fake or browser-only, and which parts were deliberately out of scope (the social feed extras, recruiter home stats). The AI then connected the profile page, recruiter postings, the new applicants page, the tracker and the job pages.
Validation: Accepted. Confirmed by the HTTP smoke test.

Prompt 3: "Create some fake jobs in the database for the demo."

Response 3 (summary): The AI wrote seed and cleanup scripts (4 demo companies with recruiter logins, 11 open postings and 1 closed), checked the live table first, ran the seed on Supabase twice to confirm it doesn't create duplicates, and listed every posting.
Validation: Accepted.

Prompt 4: "Run the app so I can test it."

Response 4 (summary): The AI explained why it shouldn't run against Supabase yet (the missing `V7`, and unreviewed migrations on a shared database). It ran the app locally on a database that persists between restarts, loaded the demo jobs, and used the real API to create a demo job seeker with applications at every stage.
Validation: Altered (local database instead of Supabase).

Prompt 5: "Hiru says he isn't planning to merge before submission. Should we fix his endpoints ourselves?"

Response 5 (summary): The AI recommended yes, because job search is a required feature, and merged Hiru's branch so his commit kept his name. It resolved the conflicts against the current schema, renumbered his migration to `V10`, renamed his controller, added a response class, updated his tests and added 4 more, and added category and job type to the create-posting form. It restarted the app and checked search on the demo data.
Validation: Accepted. 74 tests passing, and search filters checked on demo data.

Prompt 6: "Write my AI logs for this work, with my prompts phrased at a higher level and a validation section for each AI output saying whether it was accepted, rejected or altered."

Response 6 (summary): This file and task 1. The prompts are paraphrased for clarity, as in my Sprint 1 logs, but nothing was added that I didn't ask or decide.
Validation: Altered. The AI tidied the wording rather than making the prompts sound more sophisticated, so the log stays an accurate record.
