# Sprint 2 Member Contributions

Responsibilities are from Meeting 3 (2026-10-05, [minutes](../minutes/2026-10-05_meeting3_sprint2-planning.md)). Contributions list each member's merged pull requests and their Sprint 2 task issues. Time spent is as reported at Meeting 3 where given; members should add the rest.

Repository: https://github.com/frakkii/SOEN341-SoftwareProcesses

## Maria-Francesca Staicu (40324837)
**Planned:** Sprint 1 bugs, frontend job application submission and tracking, Sprint 2 sprint planning, user story

- Job application tracking frontend and theme consistency fixes: PR [#74](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/74)
- Continuous Integration pipeline (GitHub Actions builds the backend and runs all tests on every push and PR to `main`): PR [#112](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/112). Originally assigned to Shanza at Meeting 3
- Logout for job seekers and recruiters (server session ends, not just the browser): PR [#138](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/138), user story #139
- Fixed Shanza's resume features PR so it compiles and passes tests (fixed a bug choosing the current version, added 6 tests): commit `f90769c` in PR [#148](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/148)
- Acceptance tests for every Sprint 1 and Sprint 2 user story (24 tests, `documentation/acceptance_tests/`) and the script that creates them as labeled GitHub issues linked to each story. Originally assigned to Hiru at Meeting 3
- Sprint 2 plan, this contribution log, Meeting 3 minutes: branch `docs/sprint2-deliverables`
- Tasks: #40 Job application submission frontend, #44 Application status tracking frontend, #64 Job search and filtering frontend, #66 Job details page frontend, #70 Profile frontend, #140 Logout backend, #141 Logout frontend
- AI logs: `AI_logs/Francesca/Sprint2/`
- **Time spent:** 10 hours

## Audrey Prevost (40027535)
**Planned:** database maintenance, frontend recruiter dashboard and posting creation, posting management, user story

- Recruiter dashboard and job post management frontend: PR [#80](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/80)
- Job creation frontend: PRs [#86](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/86), [#87](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/87)
- Manage posts frontend: PRs [#92](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/92), [#100](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/100)
- Recruiter home frontend: PR [#97](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/97)
- Added and updated recruiter HTML pages: PR [#78](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/78)
- Consolidated branch and sync merges: PRs #68, #75, #77, #79, #81, #84, #85, #88–#91, #93–#96
- Tasks: #42 Application database integration, #46 Application status database integration, #48 Job posting management frontend, #50 Job posting database integration, #72 Profile database integration
- AI logs: `AI_logs/Audrey/`
- **Time spent:** ~20h (Meeting 3 estimate)

## Donald Champ (40270957)
**Planned:** backend recruiter dashboard and posting management, user story

- Sprint 2 features: recruiter dashboard and posting management backend, profile, job search integration, application status updates, resume versions, and their tests: PR [#101](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/101)
- Fixed job search failing on Postgres when a filter is empty: PR [#102](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/102)
- Kept the database connection pool small so several teammates can run the app at once: PR [#103](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/103)
- Tasks: #49 Job posting management backend, #51 Job posting management testing
- AI logs: `AI_logs/Donald/Sprint2/`
- **Time spent:** 15h

## Shanza Riasat (40337845)
**Planned:** resume upload feature backend, set up and demonstrate a CI pipeline (done by Francesca instead, PR #112), Sprint 2 report submission, user story

- Resume version features (list versions, choose the current one, delete) and checking that a file's contents match its extension: PR [#148](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/148). Francesca added a fix commit so it compiles and the tests pass
- Earlier resume versioning attempts: PR [#104](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/104) and follow-up uploads #105–#111, closed without merging because they replaced files that #101 had changed
- Sprint 2 report submission
- Tasks: #37 Resume upload backend, #38 Resume update functionality, #39 Resume upload testing, #57 Multiple resume storage and selection
- AI logs: `AI_logs/Shanza/AI_LOG_sprint2.md`, `AI_log_sprint2pt2.md` and `AI_log_sprint2pt3.md`
- **Time spent:** 7 hours

## Mario Habib (40283335)
**Planned:** resume upload feature and user profile frontend, user story

- UI overhaul (home feed and navigation): PR [#76](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/76)
- Sprint 2 AI logs: PR [#99](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/99)
- Tasks: #36 Resume upload frontend
- AI logs: `AI_logs/Mario/Sprint2/`
- **Time spent:** 10h (Meeting 3 estimate)

## Hirushi Rathnayaka (40237379)
**Planned:** Sprint 1 bug fixes, job search and job listing backend, acceptance tests for sprint 1 and 2 (sprint 1 completed by Francesca instead), user story

- Implemented the job search and job listing backend for Sprint 2, including the job posting model, repository, service, and REST API endpoints.
- Added functionality to retrieve active job postings, search using keywords, filter by location, industry, and job type, combine multiple filters, and retrieve individual job details.
- Added automated backend tests. All 49 project tests passed locally, including 4 job-search controller tests.
- Job search backend was integrated into main through PR #101, with the database migration updated for compatibility.
- Contributed to User Story #3 (Job Search and Filtering).
- Acceptance tests were completed by Francesca.
- Tasks: #65 Job search and filtering backend
- AI logs: AI_logs/Hiru/Sprint2/AI_LOG_Sprint2.md
- Time spent: 2 days

## Dani Tannir (40298707)
**Planned:** backend job application submission and tracking, user story

- Job seekers can submit and track job applications: PR [#67](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/67)
- Updated application checks for the new `job_postings` columns: PR [#98](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/98)
- Tasks: #41 Job application submission backend, #43 Job application submission testing, #45 Application status tracking backend, #47 Application status tracking testing
- AI logs: `AI_logs/Dani/Sprint2/`
- **Time spent:** _[to fill in]_
