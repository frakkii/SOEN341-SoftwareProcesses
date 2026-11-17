# Acceptance Test Results

Results of running the acceptance tests in [ACCEPTANCE_TESTS.md](ACCEPTANCE_TESTS.md) by hand. Each result is also posted on its GitHub issue with the steps taken and what happened; passed tests are closed.

**Summary:** 28 of 33 tests run, all 28 passed. AT-09 and AT-10 failed on the first run and passed after the fixes in this PR. AT-25 to AT-28 (notifications) wait on the notifications backend (#172) and frontend (#161); AT-31 waits on the AI feature (#60).

## Sprints 1 and 2 (run on 2026-11-16)

Build: `main` at `cba0205`, run locally on an in-memory H2 database with test accounts. Tester: Maria-Francesca Staicu.

| Test | Story | Result | Issue | Notes |
|---|---|---|---|---|
| AT-01: New job seeker registers and logs in | #1 | Pass | #113 | No separate success message: registering signs the user in directly, which is the confirmation. |
| AT-02: Registering with an email that is already used is rejected | #1 | Pass | #114 |  |
| AT-03: Invalid registration fields and wrong passwords show errors | #1 | Pass | #115 |  |
| AT-04: Job seeker uploads a PDF resume | #2 | Pass | #116 |  |
| AT-05: Unsupported or empty resume files are rejected | #2 | Pass | #117 |  |
| AT-06: Uploading a new resume keeps the previous version | #32 | Pass | #118 | The resume page only shows the current file; the earlier version was checked through GET /api/resume/versions. |
| AT-07: Job seekers can only see their own resumes | #32 | Pass | #119 |  |
| AT-08: Job seeker edits their profile and it is saved | #69 | Pass | #120 |  |
| AT-09: Invalid profile input is rejected and the profile needs a job seeker | #69 | Pass (rerun) | #121 | Failed first: recruiters could open profile.html and saw an empty, editable profile (only a short pop-up said it was for job seekers). Fixed: the page now shows "Profiles are for job seeker accounts" with a link to recruiter home |
| AT-10: Keyword search and filters return matching open jobs | #3 | Pass (rerun) | #122 | Failed first: the jobs page had no job type filter. Fixed: added a Job type dropdown (kept in the URL like the other filters) |
| AT-11: No matches shows a message; a job opens its details page | #3 | Pass | #134 |  |
| AT-12: Job seeker applies to an open job | #4 | Pass | #123 | No separate confirmation step: one click submits the application. |
| AT-13: Invalid applications are blocked with a message | #4 | Pass | #124 | The duplicate and closed cases were sent as direct requests because the page already prevents them. |
| AT-14: Job seeker sees only their own applications | #5 | Pass | #125 |  |
| AT-15: Status changes made by the recruiter show on the tracker | #5 | Pass | #126 |  |
| AT-16: A new job seeker sees an empty-state message | #5 | Pass | #127 |  |
| AT-17: Recruiter creates a job posting | #8 | Pass | #128 |  |
| AT-18: Posting with a past deadline is rejected | #8 | Pass | #129 | The error is the browser's own popup on the date field, not the page's red message box. |
| AT-19: Recruiter closes and reopens their own posting only | #8 | Pass | #130 |  |
| AT-20: Recruiter reviews applicants and their resumes | #9 | Pass | #131 |  |
| AT-21: Recruiter moves an applicant forward through the stages | #9 | Pass | #132 |  |
| AT-22: Recruiters cannot see another recruiter's applicants | #9 | Pass | #133 |  |
| AT-23: Job seeker logs out and the session ends | #139 | Pass | #142 |  |
| AT-24: Recruiter logs out from any recruiter page | #139 | Pass | #143 | Since #166 the recruiter logout is "Sign out" in the account menu (the test says "Logout"). |

## Sprint 3

| Test | Story | Result | Issue | Notes |
|---|---|---|---|---|
| AT-25 to AT-28: notifications | #6 | Not run | #154-#157 | Waiting on #172 (backend) and #161 (frontend) |
| AT-29: Job seeker views accurate application analytics by status | #33 | Pass | #158 | Run 2026-11-16 on PR #162 |
| AT-30: Recruiter edits and archives their own job posting | #8 | Pass | #159 | Run 2026-11-16 on PR #169. Another recruiter's posting returns 404, by design |
| AT-31: AI resume feedback | #10 | Not run | #160 | AI feature (#60) not started |
| AT-32: Job seeker searches, filters and sorts their application history | #33 | Pass | #164 | Run 2026-11-16 on PR #162 |
| AT-33: Job seeker sees applications submitted over time | #33 | Pass | #165 | Run 2026-11-16 on PR #162. Earlier-month data covered by unit tests |
