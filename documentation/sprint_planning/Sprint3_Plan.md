# Sprint 3 Plan

**Team:** DASM (CareerConnect)
**Sprint 3:** ends _[due date / lab demo date]_
**Board:** CareerConnect Project Board (GitHub Projects), columns To Do → In Progress → In Review → Done

We planned Sprint 3 the same way as Sprints 1 and 2: each Sprint 3 feature maps to a user story, each story is split into frontend, backend, database and testing tasks as GitHub issues, and each story has at least one acceptance test in [ACCEPTANCE_TESTS.md](../acceptance_tests/ACCEPTANCE_TESTS.md).

## Sprint objectives

Finish the remaining core features of CareerConnect, make testing stronger, and start the Generative AI feature:

1. Recruiter dashboard and posting management (create, edit, archive; applicants per posting with filters; update candidate status)
2. Application analytics and history dashboard for job seekers
3. Notifications and reminders
4. AI-powered resume feedback (start in Sprint 3, finish in Sprint 4)

Every Sprint 3 feature needs unit tests that run in CI, and every Sprint 3 story needs at least one acceptance test.

## Team

Responsibilities agreed at Meeting 4 on 2026-11-05 ([minutes](../../minutes/2026-11-05_meeting4_sprint3-planning.md)). Everyone also owns a user story.

| Name | GitHub | Sprint 3 responsibilities |
|---|---|---|
| Maria-Francesca Staicu | frakkii | Application analytics dashboard frontend; CI pipeline; project board, backlog, sprint plan and task assignments. Later also took on the missing recruiter features (edit, archive, applicant filters) |
| Audrey Prevost | MothMother | Fixed bugs from Sprint 2 |
| Donald Champ | dchamp214 | Fixing and reviewing code |
| Shanza Riasat | Shanza687 | Notifications and reminders backend |
| Mario Habib | MarioHabib33 | Recruiter dashboard, settings feature (6 hours) |
| Hirushi Rathnayaka | hirurathnayaka77 | Notifications and reminders frontend, acceptance tests |
| Dani Tannir | datannir-bit | Application analytics dashboard backend (delivered the frontend, tests and frontend CI job too, in #162) |

## Stories and tasks

Status as of 2026-11-16, compiled from GitHub issues, pull requests and branches, not from the board. Tasks marked _new_ do not have an issue yet.

### 1. Recruiter dashboard and posting management

**#8** As a recruiter, I want to create and manage job postings so that I can attract qualified candidates.
**#9** As a recruiter, I want to view a list of applicants for each posting so that I can review and manage candidates efficiently.

Sprint 2 delivered creating, closing and reopening postings and the applicant list. Sprint 3 adds editing, archiving and filtering applicants.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| Recruiter pages redesign (home, postings, applicants, create posting, settings) | MarioHabib33 | _none_ | Done: PR #166 |
| Edit postings: `PUT /api/recruiter/postings/{id}` and an edit dialog on Manage Postings | frakkii | _new_ | Done: PR #169 |
| Archive and restore postings (new `Archived` status, hidden from job search and the main list) | frakkii | _new_ | Done: PR #169 |
| Filter applicants by status, date applied and search, newest/oldest sort | frakkii | _new_ | Done: PR #169 |
| Posting management tests (9 backend tests, 11 frontend tests; 4 tests for `/api/auth/me` from #166) | frakkii | _new_ | Done: PR #169 |
| Acceptance test AT-30 | hirurathnayaka77 | #159 | Written in PR #161; passed on 2026-11-16 (recorded on #159) |

### 2. Application analytics and history dashboard

**#33** As a job seeker, I want a dashboard summarizing my applications by status so that I can see my progress at a glance.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| #56 Application summary dashboard frontend (stat tiles, status breakdown, applications per month, history search/filter/sort) | datannir-bit, frakkii | #56 | Done: PR #162 (merged after testing by frakkii, chart fix `ec81b77`) |
| Dashboard unit tests (28 Node tests for the dashboard logic, 4 backend tests) | datannir-bit | _in #56_ | Done: PR #162 |
| Frontend unit test job in CI | datannir-bit | _in #56_ | Done: PR #162 |
| Acceptance test AT-29 (counts by status, only my applications, empty state) | hirurathnayaka77 | #158 | Written in PR #161 |
| Acceptance tests AT-32 (history search/filter/sort) and AT-33 (applications over time) | frakkii | #164, #165 | Written in PR #163 |

The dashboard reads `GET /api/applications` (already returns status, `appliedAt` and `updatedAt`), so no new backend endpoint or migration is needed.

### 3. Notifications and reminders

**#6** As a user, I want to receive relevant notifications and reminders so that I can stay informed about my applications, interviews, deadlines, and recruiter activity.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| #63 Application deadline notification implementation | Shanza687 | #63 | Done in PR #182: daily reminder one day before a saved job's deadline. Can't be used in the app until job seekers can save jobs (#62) |
| Notifications backend (REST API, stored notifications, daily reminder job, mark as read) | Shanza687 | #178 | Done: PR #182 |
| Notifications page and unread indicator (frontend) | hirurathnayaka77 | #179 | Done: PR #161, merged through PR #186 |
| Merge the frontend with `main`, match the notification types to the page, Notifications link on recruiter pages | frakkii | _none_ | Done: PR #186 |
| Notification tests | Shanza687, frakkii | _none_ | Done: 5 backend tests in PR #182, 1 end-to-end backend test in PR #186, 13 frontend tests in PR #196 (including a check that every type the backend sends is one the page shows) |
| Acceptance tests AT-25 to AT-28 | hirurathnayaka77 | #154–#157 | Written in PR #161. Run 2026-11-19: AT-25, AT-27 and AT-28 passed; AT-26 blocked until saved jobs exist (#62) |

### 4. AI resume feedback (start in Sprint 3, finish in Sprint 4)

**#10** As a job seeker, I want to receive AI-generated feedback on my resume so that I can improve it before applying to jobs.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| #60 AI resume analysis and feedback integration: Claude API (Anthropic Java SDK), PDF and DOCX resume reading, demo mode without an API key, first version as a "Get feedback" card | frakkii | #60 | Done: PR #183 |
| Line-by-line AI suggestions to accept or reject, figures to fill in, download the improved resume and a report as .docx, save as a new resume version (replaces the "Get feedback" card) | dchamp214 | _none_ | Done: PR #184. Tested in the app on 2026-11-19 in demo mode; a real Claude call still needs a team API key |
| AI tests | frakkii, dchamp214 | _none_ | Done: 7 tests in PR #183 still in use (resume reading, API key setup), 20 tests in PR #184 (suggestions, accept/reject, figures, downloads, save as version) |
| Choose the model/provider and keep the API key out of the repo | frakkii | #180 | Claude (`claude-opus-5-5`); key read from `ANTHROPIC_API_KEY`, never committed. The team still needs a key for real reviews |
| Acceptance test AT-31 | hirurathnayaka77 | #160 | Written in PR #161; passed in demo mode on the first version (#183). Steps rewritten for the line-by-line suggestions (#184) on 2026-11-19; needs a rerun |

### Supporting work

| Work | Owner | Delivered in |
|---|---|---|
| CI runs on every branch push, cancels outdated runs, shows a test summary | frakkii | `docs/sprint3-management` |
| Fixed two tests that failed only in CI: a test-order bug (saved jobs left behind by the notification tests) and a resume test that compared .docx bytes with a timestamp inside | frakkii | PR #186 |
| A failed apply said "You have already applied to this job." for any database error; it now says so only for a real duplicate (2 tests) | frakkii | PR #192 |
| Sprint 3 plan, contribution log | frakkii | `docs/sprint3-management` |
| Register as Job Seeker or Recruiter | frakkii | PR #151 (open) |

## Unit tests

Every Sprint 3 feature has unit tests, and CI runs all of them on every push and pull request. On 2026-11-19 `main` has **186 unit tests, all passing: 134 backend (JUnit) and 52 frontend (Node's test runner)**.

| Feature | Backend tests | Frontend tests |
|---|---|---|
| Recruiter posting management | `RecruiterControllerTests` (23, of which 9 from #169) | `applicantFilters.test.js` (11) |
| Application analytics dashboard | 4 in `ApplicationControllerTests` (#162) | `analytics.test.js` (28) |
| Notifications and reminders | `NotificationControllerTests` (6) | `notificationLogic.test.js` (13) |
| AI resume suggestions | `ResumeOptimizationControllerTests` (14), `ResumeOptimizationServiceTest` (2), `DocxWriterTest` (4), `DocxTextTest` (5), `AiConfigTest` (2) | _none_ |
| Apply error message (#192) | `ApplicationServiceTest` (2) | _none_ |

The other backend tests cover Sprint 1 and 2 (accounts, resumes, profile, job search, applications).

How to run them:
- Backend, from `src/backend`: `mvn test`
- Frontend, from the repo root (needs Node): `node --test src/webapp/tests/`
- In CI: the run's **Summary** page on GitHub (Actions → CI) lists every backend test class; the **Frontend unit tests** job log ends with the totals

## Risks found while planning

- ~~Notifications backend missing~~: pushed and merged in #182.
- **AI feature needs an API key.** Built on `feature/ai-resume-feedback` and working in demo mode; real Claude reviews need an Anthropic API key (each review is a paid call). Decide who holds the key before the demo.
- ~~PR #161 conflicts with `main`~~: resolved and merged in #186, which also fixed the notification types the page was hiding; AT-25, AT-27 and AT-28 passed.
- **Deadline reminders can't be shown in the demo.** They only go to saved jobs, and saving jobs (#62) isn't built.
- ~~Recruiter dashboard not started~~: redesign merged in #166; edit, archive and applicant filters merged in #169; AT-30 passed.
- ~~Dashboard not tested by hand~~: tested and merged (#162); AT-29, AT-32 and AT-33 passed.

## Before the sprint ends

- [ ] Assign the unassigned Sprint 3 tasks above and fill in the Team table
- [ ] Create GitHub issues for the _new_ tasks and add them to the board
- [x] Create issues for AT-32 and AT-33 and link them to #33 (#164, #165)
- [x] Merge #162 (dashboard)
- [x] Merge #161 (notifications frontend) once its conflicts are resolved (merged through #186)
- [x] Merge the recruiter edit, archive and filters work (#169) and record AT-30 on #159
- [x] Run every Sprint 3 acceptance test and record the result on its issue (AT-25, AT-27, AT-28, AT-29, AT-30, AT-32, AT-33 passed; AT-31 passed in demo mode on the first AI card and needs a rerun with its new steps; AT-26 blocked until saved jobs exist, see [RESULTS.md](../acceptance_tests/RESULTS.md))
- [x] Run the Sprint 1 and 2 acceptance tests (AT-01 to AT-24): all passed (AT-09 and AT-10 after fixes); see [RESULTS.md](../acceptance_tests/RESULTS.md)
- [x] Clean up the project board and create issues for untracked Sprint 3 tasks (#175 to #180)
- [x] Upload the Sprint 3 meeting minutes to `minutes/` (Meeting 4; add any later meetings with [TEMPLATE.md](../../minutes/TEMPLATE.md))
- [ ] Each member fills in [Sprint3_MemberContribution.md](../Sprint3_MemberContribution.md)
- [ ] Each member adds their Sprint 3 AI log and Sprint 3 PDF report in `AI_logs/<name>/Sprint3/`
- [ ] Submit the one-page Sprint 3 summary report
