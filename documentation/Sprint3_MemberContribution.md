# Sprint 3 Member Contributions

Compiled on 2026-11-16 from GitHub pull requests, branches and issues. Each member should check their own section, add anything missing, and fill in **Time spent**. The task split is in the [Sprint 3 plan](sprint_planning/Sprint3_Plan.md).

Repository: https://github.com/frakkii/SOEN341-SoftwareProcesses

## Maria-Francesca Staicu (40324837)
**Planned:** application analytics and history dashboard frontend; project management, documentation and CI pipeline

- Checked the Sprint 3 work on GitHub against the Sprint 3 delivery instructions and found the gaps: no Sprint 3 plan or contribution log, the notifications backend code was never pushed (only its AI log is on `main`), and the recruiter dashboard and AI features have no branch yet
- Sprint 3 plan with user stories, tasks, status and risks: `documentation/sprint_planning/Sprint3_Plan.md`
- Acceptance tests AT-32 (application history search, filter and sort) and AT-33 (applications submitted over time) for story #33, added to the acceptance test script and `ACCEPTANCE_TESTS.md`
- CI pipeline: now runs on pushes to every branch (not only `main`), cancels outdated runs, and writes a per-class test summary to each run's Summary page
- This contribution log
- Dashboard frontend: Dani delivered the dashboard in PR [#162](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/162) before I started; I wrote the acceptance tests that cover it and _[add review / manual testing of #162 here if done]_
- Register as Job Seeker or Recruiter: PR [#151](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/151)
- Branch: `docs/sprint3-management`
- AI logs: `AI_logs/Francesca/Sprint3/`
- **Time spent:** _[to fill in]_

## Audrey Prevost (40027535)
**Planned:** _[to fill in]_

- AI logs uploaded on branch `audrey-sprint3` (not yet merged)
- _[add Sprint 3 work]_
- **Time spent:** _[to fill in]_

## Donald Champ (40270957)
**Planned:** _[to fill in]_

- _[add Sprint 3 work]_
- **Time spent:** _[to fill in]_

## Shanza Riasat (40337845)
**Planned:** notifications and reminders backend

- Notifications backend (REST API, stored notifications, daily reminder job, mark as read) described in `AI_logs/Shanza/AI_Log_sprint3.md`. **The Java code is not in the repository yet**: commit `3f2da65` only added the AI log
- **Time spent:** _[to fill in]_

## Mario Habib (40283335)
**Planned:** _[to fill in]_

- _[add Sprint 3 work]_
- **Time spent:** _[to fill in]_

## Hirushi Rathnayaka (40237379)
**Planned:** notifications frontend, Sprint 3 acceptance tests

- Notifications page, unread indicator and notification links in the top bar, plus acceptance tests AT-25 to AT-31 for stories #6, #8, #10 and #33: PR [#161](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/161) (open)
- Acceptance test issues #154–#160
- AI logs and Sprint 3 individual report: `AI_logs/Hiru/Sprint3/` (in PR #161)
- **Time spent:** _[to fill in]_

## Dani Tannir (40298707)
**Planned:** application analytics and history dashboard

- Application analytics and history dashboard: stat tiles, status breakdown chart, applications per month, history search/filter/sort; 28 frontend unit tests, 4 backend tests, and a frontend test job in CI: PR [#162](https://github.com/frakkii/SOEN341-SoftwareProcesses/pull/162) (open, CI passing)
- Tasks: #56 Application summary dashboard frontend
- AI logs: `AI_logs/Dani/Sprint3/` (in PR #162)
- **Time spent:** _[to fill in]_
