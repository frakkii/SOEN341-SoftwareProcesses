Task: Audit the Sprint 2 features against the requirements and plan the fixes

Date: 2026-10-26

AI Tool: Claude Code (VS Code extension), model Claude Opus 5.5



Purpose of AI Use: I used Claude to check whether our four Sprint 2 features (profile and resume management, job search, application submission and tracking, recruiter dashboard) were actually finished on `main`, and to help me turn the gaps into a plan.



Chat Link or Prompt/Response: see Appendix A

AI-Suggested Content:
- An audit of `main`, feature by feature:
  - Resume upload worked, but profile edits only saved in the browser and the Experience/Education sections were sample content
  - Job search had no backend at all; the home feed showed six hard-coded jobs
  - Applications had a backend but no way to apply from the UI, and nothing could move an application past "Applied"
  - The recruiter pages showed made-up numbers
- Two bugs found by reading the code: the "is this posting open?" query used columns that a recent schema change had removed, so every application would fail; and new postings had no generated id
- A five-phase plan (fix what's broken, job search, applications, profile, recruiter dashboard) with the endpoints, migrations and tests for each
- Three decisions it left to me: keep an open/closed flag on postings, how much of the profile to build, and how to split the work
- After checking GitHub and Supabase, a warning that Dani, Hiru and Audrey already had branches covering parts of the plan, that someone had edited the `job_postings` table by hand, and that Supabase had a `V7` migration applied that wasn't in the repo



Validation:
- I had Claude run the test suite on `main`: 11 of 45 tests failed with `Column "posted_date" not found`, which confirmed the column bug from the audit
- Claude checked the live database with read-only queries. This showed `post_id` was already generated on Supabase, so that part of the plan wasn't needed there, and it found the missing `V7`
- I compared the plan with my teammates' open branches and the PR Audrey had just merged (#100) before choosing a scope



Decision:
- Audit: Accepted
- Plan: Altered. I decided to keep the open/closed flag, build only headline/About/Skills for the profile (and remove the fake sections), and do one large PR at the end
- Altered after the database check: use the `status` column that already existed on Supabase instead of adding a new one
- Scope: I picked option (a), only building work nobody had started. I rejected merging everyone's branches into mine and redoing everything from `main`
- Changed later: when Hiru said she wouldn't merge her branch before submission, I decided to bring her job search into my PR (see task 2)



Reflection: Running the tests first turned a guess into a confirmed bug that would have broken applying in the demo. Checking the other branches and the live database before writing anything was the most useful step: my plan would have clashed with three teammates' work and with a migration nobody had pushed.

Responsible Person: Donald Champ (40270957)



Appendix A: Prompt / Response history
(Prompts are paraphrased.)

Prompt 1: "Below are the four Sprint 2 requirements. Review our repository against them and assess, feature by feature, whether each one has been fully and correctly implemented."

Response 1 (summary): Claude went through the backend and every frontend page and reported what was complete, partial, fake or missing, with file and line references. It found the column bug and the missing `post_id` generation, and ran the tests.
Validation: Accepted. The test run reproduced the bug (11 of 45 tests failing).

Prompt 2: "Based on your findings, draft an implementation plan that addresses each of the gaps you identified."

Response 2 (summary): A five-phase plan in priority order, with three decisions for me.
Validation: Altered. I made the three decisions: keep the flag, use the simpler profile, and do one PR at the end.

Prompt 3: "The plan is approved. Proceed with the implementation."

Response 3 (summary): Before writing code, Claude checked Supabase and the open branches, found the hand-edited table, the missing `V7`, and my teammates' overlapping work, and stopped to ask how I wanted to proceed.
Validation: Accepted. This stopped me from duplicating my teammates' work.

Prompt 4: "A teammate has just merged into `main`. Do I need to re-clone the repository, or can you incorporate the new merge and reassess where things stand?"

Response 4 (summary): Pulling `main` was enough. The merge was Audrey's PR #100; the `V7` problem and Dani's and Hiru's branches were still open.
Validation: Accepted.

Prompt 5: "Of the options you outlined, which approach would you recommend?"

Response 5 (summary): Option (a): build the unstarted work, let Dani merge their small fix, let Hiru update her own branch, and build the job search pages against Hiru's API.
Validation: Accepted.
