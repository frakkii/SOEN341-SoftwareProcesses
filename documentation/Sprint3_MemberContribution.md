# Sprint 3 Member Contributions

Compiled on 2026-11-16 and updated on 2026-11-19 from GitHub pull requests, branches and issues. Each member should check their own section, add anything missing, and fill in **Time spent**. **Planned** is from Meeting 4 (2026-11-05, [minutes](../minutes/2026-11-05_meeting4_sprint3-planning.md)); the task split is in the [Sprint 3 plan](sprint_planning/Sprint3_Plan.md).

Repository: https://github.com/frakkii/SOEN341-SoftwareProcesses

## Maria-Francesca Staicu (40324837)
**Planned:** application analytics and history dashboard frontend; project management, documentation and CI pipeline. Also took on the missing parts of the recruiter dashboard feature and started the AI feature, which had no owner

- Checked the Sprint 3 work on GitHub against the Sprint 3 delivery instructions and found the gaps: no Sprint 3 plan or contribution log, the notifications backend code was never pushed (only its AI log is on `main`), and the recruiter dashboard and AI features have no branch yet
- Sprint 3 plan with user stories, tasks, status and risks: `documentation/sprint_planning/Sprint3_Plan.md`
- Acceptance tests AT-32 (application history search, filter and sort) and AT-33 (applications submitted over time) for story #33, added to the acceptance test script and `ACCEPTANCE_TESTS.md`
- CI pipeline: now runs on pushes to every branch (not only `main`), cancels outdated runs, and writes a per-class test summary to each run's Summary page
- This contribution log
- Dashboard frontend: Dani delivered the dashboard in PR [#162](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/162) before I started. I tested it in the running app (all states, filters, ranges, phone width), fixed a chart alignment bug (commit `ec81b77`), merged it, and ran and recorded AT-29, AT-32 and AT-33 (#158, #164, #165: passed)
- Recruiter posting management (Sprint 3 feature 1): edit a posting's details (`PUT /api/recruiter/postings/{id}` and an edit dialog), archive and restore postings, and filter applicants by status, date applied and search. 9 backend tests, 11 frontend unit tests, CI now runs every frontend test file. Tested AT-30 by hand. Branch `feature/recruiter-edit-archive-filters`
- Tests for `GET /api/auth/me`, which #166 added without tests (4 tests)
- Ran all 24 Sprint 1 and 2 acceptance tests (AT-01 to AT-24) by hand, which had never been run: 22 passed; AT-09 (recruiters could open the profile page) and AT-10 (no job type filter) failed, so I fixed both and they passed on a rerun. Results on each issue and in `documentation/acceptance_tests/RESULTS.md`
- Project board cleanup: created task issues #175 to #180 for untracked Sprint 3 work, moved Sprint 3 items to the right columns, assigned owners from Meeting 4
- AI resume feedback (Sprint 3 feature 4, story #10): `POST /api/resume/feedback` using the Claude API through the Anthropic Java SDK for PDF and DOCX resumes, a Get feedback card on the resume page, and a demo mode so it works with no API key. 15 tests; AT-31 passed in demo mode. PR [#183](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/183) (merged). Donald later replaced the feedback card with line-by-line suggestions (PR [#184](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/184)), reusing the resume reading, Claude client and demo mode from this work
- `run.sh` / `run.cmd` now explain why the app won't start instead of showing Maven's generic error: PR [#173](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/173)
- Meeting 4 minutes (from Shanza's notes) and Sprint 3 plan updates: PR [#171](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/171), PR [#170](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/170)
- Sprint 3 individual report: PR [#174](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/174)
- One-page Sprint 3 summary report: `submissions/Sprint3_Summary_Report.pdf`
- Register as Job Seeker or Recruiter: PR [#151](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/151)
- Branches: `docs/sprint3-management` (PR [#163](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/163)), `docs/francesca-sprint3-ai-log2` (PR [#168](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/168)), `feature/recruiter-edit-archive-filters`
- AI logs: `AI_logs/Francesca/Sprint3/`
- **Time spent:** 3 hours

## Audrey Prevost (40027535)
**Planned:** user story, fixed bugs from Sprint 2

- AI logs (account type selector, manage posts branch overview, Spring Data projections, creating job postings): PR [#167](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/167)
- Account type selector on the sign-up page: Job Seeker / Recruiter radio buttons styled as a toggle, recruiter-only fields shown for recruiters, role change handling cleaned up: PR [#153](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/153) (merged 2026-11-02)
- Resume versions migration (`V7__add_resume_versions.sql`) refactored with comments and checks, a fix carried over from Sprint 2: PR [#152](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/152) (merged 2026-11-02)
- Cleaned up `AI_logs/Audrey/` (removed old copied source folders, re-uploaded the logs)
- **Time spent:** 3 hours

## Donald Champ (40270957)
**Planned:** user story, fixing and reviewing code

- Finished the AI feature (Sprint 3 feature 4, story #10): line-by-line AI resume suggestions on the resume page. The job seeker accepts or rejects each rewrite, types in real figures where the AI leaves a placeholder like `[X%]`, downloads the improved resume and a suggestions report as .docx, and can save the result as a new resume version. `POST /api/resume/optimization` with the Claude API, demo mode without a key, `DocxWriter` for the output files. Replaced the Get feedback card from #183 and reused its resume reading, Claude client and demo mode. 20 new backend tests (126 pass): PR [#184](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/184) (merged)
- AI log: `AI_logs/Donald/Sprint3/task1-ai-resume-optimizer.md`
- **Time spent:** 3 hours

## Shanza Riasat (40337845)
**Planned:** user story, notifications and reminders backend

- Notifications and reminders backend: `Notification` entity and `V11__create_notifications.sql` migration; `GET /api/notifications` (optionally unread only), `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read` and `/read-all`; notifications created when a job seeker applies (to the recruiter) and when a recruiter changes an application's status (to the job seeker); a daily `DeadlineReminderService` job that reminds job seekers one day before a posting's deadline. 5 controller tests, CI passes: PR [#182](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/182) (open; replaces PR [#172](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/172))
- Took the notes at Meeting 4 (2026-11-05) and proposed the Sprint 3 feature split
- AI log: `AI_logs/Shanza/AI_Log_sprint3.md`
- **Time spent:** 3 hours

## Mario Habib (40283335)
**Planned:** user story, recruiter dashboard, settings feature (6 hours)

- Recruiter UI redesign (recruiter home, postings, applicants, create posting), recruiter and job seeker settings pages, `GET /api/auth/me` session check, session cookie settings: PR [#166](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/166)
- AI log: `AI_logs/Mario/Sprint3/task1-profile-employer-refinement.md`
- **Time spent:** 3 hours

## Hirushi Rathnayaka (40237379)
**Planned:** user story, notifications and reminders frontend, acceptance tests

- Notifications page, unread indicator and notification links in the top bar, plus acceptance tests AT-25 to AT-31 for stories #6, #8, #10 and #33: PR [#161](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/161) (open)
- Acceptance test issues #154–#160
- Frontend API contract for the notifications backend (`documentation/notifications/frontend-api-contract.md`) and a demo mode (`notifications.html?demoNotifications=1`) so the page can be shown without the backend (in PR #161)
- AI logs and Sprint 3 individual report: `AI_logs/Hiru/Sprint3/` (in PR #161)
- **Time spent:** 3 hours

## Dani Tannir (40298707)
**Planned:** user story, application analytics and history dashboard backend

- Application analytics and history dashboard: stat tiles, status breakdown chart, applications per month, history search/filter/sort; 28 frontend unit tests, 4 backend tests, and a frontend test job in CI: PR [#162](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/162) (merged)
- Tasks: #56 Application summary dashboard frontend
- AI logs: `AI_logs/Dani/Sprint3/` (in PR #162)
- **Time spent:** 3 hours
