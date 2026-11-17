Task: Run and record the Sprint 1 and Sprint 2 acceptance tests (AT-01 to AT-24), and clean up the project board

Date: 2026-11-16

AI Tool: Claude Code (desktop app), model Claude Opus 5.5

Purpose of AI Use: AI was used to update the project board for Sprint 3, then to run all 24 Sprint 1 and 2 acceptance tests in the running app, record each result on its GitHub issue, and summarize the results in the repository.

Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:

- Board cleanup: 6 new task issues for Sprint 3 work that had no issue (#175 to #180), each linked under its story; Sprint 3 items moved out of Backlog to To Do, In progress or Done; story #33 closed; owners assigned from Meeting 4
- While cleaning the board, it found that none of the 24 Sprint 1 and 2 acceptance tests had ever been run or recorded
- Ran all 24 tests on a local in-memory database (no test data in the team's Supabase) with five test accounts (job seekers Alice, Bob and Carl; recruiters Acme Corp and Globex), using the real pages in the browser for each step, and sending a request directly only where the page already blocks the action (for example, applying twice)
- Posted each result on its issue (build tested, what was done, what happened, notes), closed the 22 that passed, and left the 2 failures open
- `documentation/acceptance_tests/RESULTS.md`: one table with the result of every acceptance test (Sprints 1 to 3)
- Fixes for the two failures: the profile page now shows "Profiles are for job seeker accounts" with a link to recruiter home when a recruiter opens it (AT-09), and the jobs page has a Job type dropdown that is sent to the existing `jobType` API filter and kept in the URL (AT-10)

Validation:

- Each step was checked against what the page showed (messages, lists, statuses) and against the server's response, not only one of them
- Where the page behaved differently from the test's wording, the result says so instead of passing silently: no separate success or confirmation steps (AT-01, AT-12), the past-deadline error is a browser popup (AT-18), and the recruiter logout is now "Sign out" in a menu (AT-24)
- Rechecked AT-03 after the first attempt showed an old message: the browser's own validation had blocked the form, and that is what the user sees
- Confirmed AT-02 did not create a second account by logging in afterwards and checking the original surname
- After the fixes, reran AT-09 and AT-10 from the start on a fresh database: both passed. Job seekers still see their normal profile, and the job type filter works alone and combined with keyword and location, survives a reload and is reset by Clear filters. The 39 frontend unit tests still pass
- Corrected the first AT-09 result: the page did show a short pop-up ("Profiles are for job seeker accounts.") that the first check missed; the failure stood because the empty, editable profile stayed on screen

Decision:

- Accepted: the board changes, the 24 results and the results table
- Accepted: recording AT-09 and AT-10 as failures on the first run, then fixing both. AT-09: recruiters could open the profile page, which showed an empty job seeker profile (the data itself was protected with 403). AT-10: the jobs page had no job type filter, although the backend supports one
- Accepted: keeping stories #8 and #9 open until their Sprint 2 tests were run; those tests (AT-17 to AT-22) have now passed
- Rejected: closing the Sprint 1 and 2 stories on the board in the same step; that is a separate decision

Reflection: The board cleanup turned up the biggest gap of the sprint: 24 acceptance tests written in Sprint 2 but never run. Running them by hand found two real problems that the automated tests missed, because those tests check the API, not what the page lets a user do. Writing down small differences from each test's wording also showed which tests need updating.

Responsible Person: Maria-Francesca Staicu (40324837)

Appendix A: Prompt / Response history (Prompts are paraphrased.)

Prompt 1: "Do the board cleanup."

Response 1 (summary): The AI read the board's columns and Sprint 3 items, created issues #175 to #180 for untracked tasks, moved Sprint 3 items to the right columns, closed #33, assigned owners, and noted that AT-01 to AT-24 had never been run. Acceptance: Accepted.

Prompt 2: "Yes, run them."

Response 2 (summary): The AI ran all 24 tests on a local test database and recorded the results: 22 passed and were closed; AT-09 and AT-10 failed and stay open with what is needed to fix them. It added a results table to the repository and this log. Acceptance: Accepted after review.

Prompt 3: "Fix both."

Response 3 (summary): The AI fixed the profile page for recruiters and added the job type filter, reran AT-09 and AT-10 on a fresh test database (both passed), corrected its first AT-09 result (it had missed a short pop-up message), and updated the results table, plan, contribution log and this log. Acceptance: Accepted.
