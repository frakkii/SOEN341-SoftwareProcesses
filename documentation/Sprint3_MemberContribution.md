# Sprint 3 Member Contributions

Compiled on 2026-11-16 from GitHub pull requests, branches and issues. Each member should check their own section, add anything missing, and fill in **Time spent**. **Planned** is from Meeting 4 (2026-11-05, [minutes](../minutes/2026-11-05_meeting4_sprint3-planning.md)); the task split is in the [Sprint 3 plan](sprint_planning/Sprint3_Plan.md).

Repository: https://github.com/frakkii/SOEN341-SoftwareProcesses

## Maria-Francesca Staicu (40324837)
**Planned:** application analytics and history dashboard frontend; project management, documentation and CI pipeline. Also took on the missing parts of the recruiter dashboard feature

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
- Register as Job Seeker or Recruiter: PR [#151](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/151)
- Branches: `docs/sprint3-management` (PR [#163](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/163)), `docs/francesca-sprint3-ai-log2` (PR [#168](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/168)), `feature/recruiter-edit-archive-filters`
- AI logs: `AI_logs/Francesca/Sprint3/`
- **Time spent:** _[to fill in]_

## Audrey Prevost (40027535)
**Planned:** user story, fixed bugs from Sprint 2

- AI logs (account type selector, manage posts branch overview, Spring Data projections, creating job postings): PR [#167](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/167)
- _[add Sprint 3 work]_
- **Time spent:** _[to fill in]_

## Donald Champ (40270957)
**Planned:** user story, fixing and reviewing code

- _[add Sprint 3 work]_
- **Time spent:** _[to fill in]_

## Shanza Riasat (40337845)
**Planned:** user story, notifications and reminders backend

- Notifications backend (REST API, stored notifications, daily reminder job, mark as read) described in `AI_logs/Shanza/AI_Log_sprint3.md`. **The Java code is not in the repository yet**: commit `3f2da65` only added the AI log
- **Time spent:** _[to fill in]_

## Mario Habib (40283335)
**Planned:** user story, recruiter dashboard, settings feature (6 hours)

- Recruiter UI redesign (recruiter home, postings, applicants, create posting), recruiter and job seeker settings pages, `GET /api/auth/me` session check, session cookie settings: PR [#166](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/166)
- AI log: `AI_logs/Mario/Sprint3/task1-profile-employer-refinement.md`
- **Time spent:** _[to fill in]_

## Hirushi Rathnayaka (40237379)
**Planned:** user story, notifications and reminders frontend, acceptance tests

- Notifications page, unread indicator and notification links in the top bar, plus acceptance tests AT-25 to AT-31 for stories #6, #8, #10 and #33: PR [#161](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/161) (open)
- Acceptance test issues #154–#160
- AI logs and Sprint 3 individual report: `AI_logs/Hiru/Sprint3/` (in PR #161)
- **Time spent:** _[to fill in]_

## Dani Tannir (40298707)
**Planned:** user story, application analytics and history dashboard backend

- Application analytics and history dashboard: stat tiles, status breakdown chart, applications per month, history search/filter/sort; 28 frontend unit tests, 4 backend tests, and a frontend test job in CI: PR [#162](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/162) (merged)
- Tasks: #56 Application summary dashboard frontend
- AI logs: `AI_logs/Dani/Sprint3/` (in PR #162)
- **Time spent:** _[to fill in]_
