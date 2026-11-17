# Sprint 3 Individual Report: Maria-Francesca Staicu (40324837)

_Draft. Fill in the bracketed parts, then export as `Sprint3_Individual_Report.pdf` in this folder and delete this draft._

**Team:** DASM (CareerConnect) · **Repository:** https://github.com/frakkii/SOEN341-SoftwareProcesses

## My Sprint 3 responsibilities
- Application analytics and history dashboard frontend (story #33, task #56)
- Project management, documentation and the CI pipeline

## What I did
- **Dashboard (#33):** Dani delivered the dashboard frontend in PR #162 before I started, so instead of building a second version I wrote the acceptance tests that cover the rest of the story: AT-32 (search, filter and sort the application history) and AT-33 (applications submitted over time). _[Add your review / manual test of #162 and the result.]_
- **Project management:** checked all Sprint 3 work on GitHub against the guidelines and wrote the Sprint 3 plan with stories, tasks, status, risks and a checklist. I found that the notifications backend had not been pushed (only its AI log is on `main`) and that the recruiter dashboard and AI features had not been started, and raised these with the team. _[Add what the team decided.]_
- **CI pipeline:** CI now runs on pushes to every branch instead of only `main`, cancels outdated runs, and shows a per-class test summary on each run's page. Dani's PR adds a second job that runs the dashboard's frontend unit tests.
- **Documentation:** Sprint 3 contribution log, AI usage log (`task1-sprint3-management-ci-and-acceptance-tests.md`). _[Add meeting minutes you wrote, if any.]_
- **Also:** registration lets users choose Job Seeker or Recruiter (PR #151).

## Challenges and how I handled them
- **Two people on the same feature.** My assigned dashboard was already implemented by a teammate. I switched to work the dashboard still needed (acceptance tests) and to my management/CI tasks instead of opening a conflicting PR.
- **Missing code behind a feature.** The notifications frontend depends on a backend that was not in the repository. I recorded it as a risk in the plan so it gets pushed before the demo.
- **Overlapping documentation PRs.** My branch and PR #161 both edit the acceptance test files; the plan is to merge #161 first and rebase mine.

## AI usage
I used Claude Code for the gap check, drafting the plan and logs, writing the acceptance tests and the CI summary step. I validated its output by reading Dani's code and test names, regenerating the acceptance test file from the script, and running the CI summary step on real test reports. Full details are in my Sprint 3 AI log in this folder.

## Time spent
_[hours]_
