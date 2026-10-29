Task: Add a logout feature and fix a teammate's resume features pull request

Date: 2026-10-29

AI Tool: Claude Code (desktop app), model Claude Opus 5.5

Purpose of AI Use: AI was used to add a working logout for job seekers and recruiters, add it to the backlog with acceptance tests, check whether teammates' resume pull requests were safe to merge, and fix Shanza's resume features pull request (#148) so it compiled and passed the tests.

Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:

- A POST /api/auth/logout endpoint that ends the server session, with two tests: after logging out, the next request is refused (401), and logging out without a session still works
- A shared js/logout.js used by the job seeker top bar (new Log out link) and every recruiter page. It finds that the recruiter Logout links only cleared the browser, so the server still treated the browser as logged in, and the Create Posting Logout link did nothing
- Keeping Audrey's recruiter logout code unchanged and running logout.js alongside it, using a keepalive request so the logout still reaches the server when her code redirects first
- User story #139 with tasks #140 and #141, and acceptance tests AT-23 (#142) and AT-24 (#143), plus updates to the Sprint 2 plan and contribution log
- A review of Shanza's PRs #106–#111: merging them would not conflict but would stop the backend compiling, because each replaced a whole file with an older copy and would undo #101
- Restoring Shanza's branches from GitHub's saved pull request history after she deleted them by accident
- A review of PR #148 that found it did not compile and, once patched, broke 25 of 76 tests because the new file-content check rejected the tests' fake files
- A fix commit on PR #148: added Resume.setCurrent, removed an endpoint that duplicated an existing recruiter endpoint, fixed a bug where choosing the already-current resume left no current resume, updated the tests to upload real PDF and DOCX contents, and added 6 tests for the new endpoints

Validation:

- Ran all backend tests locally after each change: 76 passing after the logout, 80 passing after the PR #148 fix
- Checked logout in the browser for a job seeker and a recruiter on a separate copy of the app using an in-memory database (not Supabase): each time it returned to the login page, cleared the saved name, and the server then refused requests
- Before calling the resume bug fixed, undid the fix and confirmed the new test failed without it
- Before recommending against merging Shanza's PRs, merged them into a throwaway copy of main and ran the build to confirm the compile errors
- CI passed on PR #138 and PR #148 before they were merged

Decision:

- Accepted: the logout endpoint and shared script
- Modified: asked for the logout to work alongside Audrey's existing recruiter logout instead of replacing her code
- Accepted: closing PR #144 (an old copy of ApplicationService.java that broke the build) instead of fixing it
- Accepted: the fix commit on PR #148; I merged it after CI passed
- Rejected: having the AI write teammates' AI logs, since it does not know their prompts

Reflection: CI was useful straight away: it caught two teammate pull requests that would have broken main before anyone merged them. The most important lesson was that "no merge conflicts" on GitHub doesn't mean a PR is safe; you have to build and test the merged result. I also learned that uploading whole files through the GitHub website replaces everyone else's changes to those files, which is why branching from the latest main matters.

Responsible Person: Maria-Francesca Staicu (40324837)

Appendix A: Prompt / Response history (Prompts are paraphrased.)

Prompt 1: "How do I run the acceptance tests?" and, after the app wouldn't start, the error output

Response 1 (summary): Steps to start the app, create test accounts and record results on each issue. The app failed to start because a Windows setting stopped run.cmd from finding mvnw.cmd; the AI gave a command to turn it off for that terminal. Acceptance: Accepted.

Prompt 2: "Can you add a logout feature?"

Response 2 (summary): The AI found the recruiter Logout links only cleared the browser and job seekers had none. It added the logout endpoint, tests and a shared script, checked both roles in the browser, and opened PR #138. Acceptance: Accepted.

Prompt 3: "Is this OK to add?" and "Can't it coexist with Audrey's logout for recruiters?"

Response 3 (summary): CI passed and it only added behaviour, but it should be reviewed by Audrey and logged here. The AI then restored Audrey's code exactly and made logout.js work alongside it, and re-checked both roles. Acceptance: Modified (my idea to keep her code).

Prompt 4: "Update backlog."

Response 4 (summary): Created user story #139, tasks #140 and #141, acceptance tests #142 and #143, and updated the Sprint 2 docs. Acceptance: Accepted.

Prompt 5: "The build and test CI check failed."

Response 5 (summary): The failure was on PR #144, a re-upload of an old ApplicationService.java, not on the logout PR. The AI also noticed its last docs commit had missed the PR #138 merge and moved it to PR #145. I agreed to close #144. Acceptance: Accepted.

Prompt 6: (relaying Shanza) "Will merging my PRs break the code?", "How do I bring my changes in?", and "I deleted my branches"

Response 6 (summary): Merging #106–#111 would break the build. The AI explained how to bring her new endpoints in through one PR from current main, and restored all six deleted branches. Acceptance: Accepted.

Prompt 7: "Check the resume features upload PR #148. Is it mergeable?" then "Can you fix it for her?" then "Merge it."

Response 7 (summary): Not mergeable: it didn't compile and broke 25 tests once patched. The AI pushed one fix commit to her branch (see AI-Suggested Content). All 80 tests and CI passed, and I merged it. Acceptance: Accepted.

Prompt 8: "Close #57" and "Write an AI log for her as well."

Response 8 (summary): Closed task #57 and updated the Sprint 2 docs (PR #149). Wrote this log for my own prompts and said it could not write teammates' AI logs because it does not know their prompts. Acceptance: Accepted.
