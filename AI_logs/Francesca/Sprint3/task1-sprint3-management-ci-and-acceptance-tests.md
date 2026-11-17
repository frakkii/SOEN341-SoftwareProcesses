Task: Sprint 3 project management, documentation, CI pipeline, and acceptance tests for the application analytics dashboard (story #33, task #56)

Date: 2026-11-16

AI Tool: Claude Code (desktop app), model Claude Opus 5.5

Purpose of AI Use: AI was used to compare the repository against the Sprint 3 guidelines, check what my teammates had already pushed for my assigned feature, and draft the Sprint 3 plan, contribution log, acceptance tests and CI changes.

Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:

- Before writing any code, the AI checked the remote branches and found that Dani had already built the application analytics dashboard (PR #162, CI passing) that morning. Building a second version would have produced two competing PRs editing the same files, so it stopped and asked me how to proceed
- A gap check of Sprint 3 work on GitHub: the notifications backend code was never pushed (commit 3f2da65 on main only added Shanza's AI log, and Hiru's notifications page in PR #161 calls an API that does not exist), and the recruiter dashboard and AI features have no branch or PR
- Sprint 3 plan (documentation/sprint_planning/Sprint3_Plan.md): objectives, team table, stories and tasks with their issue and PR status, risks, and an end-of-sprint checklist
- Two acceptance tests for story #33 that AT-29 did not cover: AT-32 (search, filter and sort the application history) and AT-33 (applications submitted over time, with the 6 months / 12 months / All time ranges), each linked to the unit tests in PR #162 that cover the same logic
- CI changes (.github/workflows/ci.yml): run on pushes to every branch instead of only main, cancel a run when a newer push arrives, and write a per-class test count table to the run's Summary page
- Sprint 3 contribution log (documentation/Sprint3_MemberContribution.md) pre-filled from GitHub, with placeholders for each member's time and missing work

Validation:

- Read Dani's PR #162 (markup, test names, AI log) so the acceptance test steps use the real labels on the page (Search, Status, Sort by, Clear filters, 6 months / 12 months / All time, View as table) and the "Automated coverage" lines name tests that actually exist
- Regenerated ACCEPTANCE_TESTS.md with the script (-Markdown) instead of editing it by hand, so the file and the script stay in sync
- Ran the new CI test summary step locally against real Surefire reports: it produced the table for all 8 test classes (80 tests, 0 failed)
- Confirmed that notifications code is missing by listing the backend source folders on main, not only by reading commit messages
- Reviewed every document before committing

Decision:

- Accepted: not building a second dashboard; I chose to do the management, documentation and CI side instead (option 2 of the three the AI gave me)
- Accepted: the Sprint 3 plan, AT-32 and AT-33, and the CI changes
- Modified: the contribution log only lists what GitHub shows; teammates fill in their own planned work and time spent
- Rejected: having the AI write meeting minutes, since it was not at the meetings
- Rejected: having the AI create GitHub issues or push without my go-ahead

Reflection: Checking remote branches before starting saved me from duplicating a teammate's finished feature. It also found that the notifications backend was only an AI log with no code, which would have broken the demo if nobody noticed. I learned that GitHub Actions can write a Markdown summary for each run, which makes test results easier to show during the evaluation.

Responsible Person: Maria-Francesca Staicu (40324837)

Appendix A: Prompt / Response history (Prompts are paraphrased.)

Prompt 1: (Discord screenshot showing I am doing the frontend of the Application Analytics and History Dashboard, plus management, documentation and CI; and the Sprint 3 guidelines PDF) "Here is sprint 3, implement AI logs and everything needed for my tasks."

Response 1 (summary): The AI read the guidelines and the repository, then found PR #162 by Dani already implementing the same dashboard with tests and a frontend CI job. It did not write code and gave three options: review and finish #162, do the management/documentation/CI side work, or build my own version only if the team agreed. Acceptance: Accepted.

Prompt 2: "2"

Response 2 (summary): The AI created the branch docs/sprint3-management from main and wrote the Sprint 3 plan, acceptance tests AT-32 and AT-33, the CI changes, the Sprint 3 contribution log and this AI log. It flagged the missing notifications backend and the overlap between this branch and PR #161 on the acceptance test files. Acceptance: Accepted with changes.
