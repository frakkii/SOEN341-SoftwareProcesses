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

From the team's Sprint 3 task split. Members should correct their own row.

| Name | GitHub | Sprint 3 responsibilities |
|---|---|---|
| Maria-Francesca Staicu | frakkii | Application analytics dashboard frontend; project management, documentation and CI pipeline |
| Audrey Prevost | MothMother | _[to fill in]_ |
| Donald Champ | dchamp214 | _[to fill in]_ |
| Shanza Riasat | Shanza687 | Notifications and reminders backend |
| Mario Habib | MarioHabib33 | _[to fill in]_ |
| Hirushi Rathnayaka | hirurathnayaka77 | Notifications frontend, Sprint 3 acceptance tests |
| Dani Tannir | datannir-bit | Application analytics dashboard (frontend, tests, frontend CI job) |

## Stories and tasks

Status as of 2026-11-16, compiled from GitHub issues, pull requests and branches, not from the board. Tasks marked _new_ do not have an issue yet.

### 1. Recruiter dashboard and posting management

**#8** As a recruiter, I want to create and manage job postings so that I can attract qualified candidates.
**#9** As a recruiter, I want to view a list of applicants for each posting so that I can review and manage candidates efficiently.

Sprint 2 delivered creating, closing and reopening postings and the applicant list. Sprint 3 adds editing, archiving and filtering applicants.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| Recruiter pages redesign (home, postings, applicants, create posting, settings) | MarioHabib33 | _none_ | Done: PR #166 |
| Edit postings: `PUT /api/recruiter/postings/{id}` and an edit dialog on Manage Postings | frakkii | _new_ | Done on `feature/recruiter-edit-archive-filters` |
| Archive and restore postings (new `Archived` status, hidden from job search and the main list) | frakkii | _new_ | Done on `feature/recruiter-edit-archive-filters` |
| Filter applicants by status, date applied and search, newest/oldest sort | frakkii | _new_ | Done on `feature/recruiter-edit-archive-filters` |
| Posting management tests (9 backend tests, 11 frontend tests; 4 tests for `/api/auth/me` from #166) | frakkii | _new_ | Done on `feature/recruiter-edit-archive-filters` |
| Acceptance test AT-30 | hirurathnayaka77 | #159 | Written in PR #161; passed by hand on 2026-11-16 against `feature/recruiter-edit-archive-filters` |

### 2. Application analytics and history dashboard

**#33** As a job seeker, I want a dashboard summarizing my applications by status so that I can see my progress at a glance.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| #56 Application summary dashboard frontend (stat tiles, status breakdown, applications per month, history search/filter/sort) | datannir-bit, frakkii | #56 | Done: PR #162 (merged after testing by frakkii, chart fix `ec81b77`) |
| Dashboard unit tests (28 Node tests for the dashboard logic, 4 backend tests) | datannir-bit | _in #56_ | Done: PR #162 |
| Frontend unit test job in CI | datannir-bit | _in #56_ | In review: PR #162 |
| Acceptance test AT-29 (counts by status, only my applications, empty state) | hirurathnayaka77 | #158 | Written in PR #161 |
| Acceptance tests AT-32 (history search/filter/sort) and AT-33 (applications over time) | frakkii | #164, #165 | Written in PR #163 |

The dashboard reads `GET /api/applications` (already returns status, `appliedAt` and `updatedAt`), so no new backend endpoint or migration is needed.

### 3. Notifications and reminders

**#6** As a user, I want to receive relevant notifications and reminders so that I can stay informed about my applications, interviews, deadlines, and recruiter activity.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| #63 Application deadline notification implementation | _unassigned_ | #63 | Open |
| Notifications backend (REST API, stored notifications, daily reminder job, mark as read) | Shanza687 | _new_ | **Code not in the repo.** Commit `3f2da65` on `main` only added the AI log; no notification Java classes or migration exist |
| Notifications page and unread indicator (frontend) | hirurathnayaka77 | _new_ | In review: PR #161 (depends on the backend above) |
| Notification tests | _unassigned_ | _new_ | Not started |
| Acceptance tests AT-25 to AT-28 | hirurathnayaka77 | #154–#157 | Written in PR #161 |

### 4. AI resume feedback (start in Sprint 3, finish in Sprint 4)

**#10** As a job seeker, I want to receive AI-generated feedback on my resume so that I can improve it before applying to jobs.

| Task | Assignee | Issue | Status |
|---|---|---|---|
| #60 AI resume analysis and feedback integration | _unassigned_ | #60 | Open |
| Choose the model/provider and keep the API key out of the repo | _unassigned_ | _new_ | Not started |
| Acceptance test AT-31 | hirurathnayaka77 | #160 | Written in PR #161 |

### Supporting work

| Work | Owner | Delivered in |
|---|---|---|
| CI runs on every branch push, cancels outdated runs, shows a test summary | frakkii | `docs/sprint3-management` |
| Sprint 3 plan, contribution log | frakkii | `docs/sprint3-management` |
| Register as Job Seeker or Recruiter | frakkii | PR #151 (open) |

## Risks found while planning

- **Notifications backend missing.** PR #161's notifications page calls an API that does not exist on `main`. Shanza needs to push the Java code (controller, service, entity, repository, Flyway migration and tests), or the notifications feature cannot be demoed.
- **AI feature not started.** #60 has no branch, PR or assignee. The guidelines require it to be started in Sprint 3.
- **PR #161 conflicts with `main`** in 7 files (acceptance test files from #163, recruiter pages and `shell.js` from #166). It needs resolving before the notifications frontend can merge.
- ~~Recruiter dashboard not started~~: redesign merged in #166; edit, archive and applicant filters on `feature/recruiter-edit-archive-filters`.
- ~~Dashboard not tested by hand~~: tested and merged (#162); AT-29, AT-32 and AT-33 passed.

## Before the sprint ends

- [ ] Assign the unassigned Sprint 3 tasks above and fill in the Team table
- [ ] Create GitHub issues for the _new_ tasks and add them to the board
- [x] Create issues for AT-32 and AT-33 and link them to #33 (#164, #165)
- [x] Merge #162 (dashboard)
- [ ] Merge #161 (notifications frontend) once its conflicts are resolved
- [ ] Merge `feature/recruiter-edit-archive-filters`, then record AT-30 on #159
- [ ] Run every Sprint 3 acceptance test and record the result on its issue
- [ ] Upload the Sprint 3 meeting minutes to `minutes/` (copy [TEMPLATE.md](../../minutes/TEMPLATE.md))
- [ ] Each member fills in [Sprint3_MemberContribution.md](../Sprint3_MemberContribution.md)
- [ ] Each member adds their Sprint 3 AI log and Sprint 3 PDF report in `AI_logs/<name>/Sprint3/`
- [ ] Submit the one-page Sprint 3 summary report
