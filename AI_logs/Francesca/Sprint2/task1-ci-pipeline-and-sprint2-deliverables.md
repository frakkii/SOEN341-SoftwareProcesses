Task: Set up the CI pipeline and prepare the remaining Sprint 2 deliverables

Date: 2026-10-29

AI Tool: Claude Code (desktop app), model Claude Opus 5.5

Purpose of AI Use: AI was used to sync my local repo with the team's latest work, check our repository against the Sprint 2 delivery instructions, set up a Continuous Integration pipeline, and draft the acceptance tests, Sprint 2 plan and contribution log template.

Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:

- Stashing my uncommitted change to JobPosting.java before pulling 53 new commits, then keeping the team's version of the file instead of mine because theirs was used by the new code and tests
- A gap check of the repo against the Sprint 2 instructions: no acceptance tests, no CI pipeline, empty minutes folder, no Sprint 2 plan or contribution log, unlabeled and unassigned task issues, and finished work whose issues were still open
- A GitHub Actions workflow (.github/workflows/ci.yml) that sets up Java 17 and runs ./mvnw -B verify in src/backend on every push and PR to main, and uploads the test reports
- An explanation of why CI needs no database secrets: tests use the in-memory H2 database from src/test/resources/application.properties
- 22 acceptance tests (AT-01 to AT-22) covering the Sprint 1 story (#1) and the Sprint 2 stories (#2, #3, #4, #5, #8, #9, #32, #69), each with Given / Steps / Then and the backend test that covers it automatically
- A PowerShell script that creates the acceptance tests as GitHub issues labeled acceptance-test and links each one to its user story as a sub-issue
- A Sprint 2 plan and a contribution log template pre-filled from GitHub issues and pull requests
- A meeting minutes template

Validation:

- Ran all backend tests locally before adding CI: 74 tests, 0 failures
- The CI workflow passed on its first run on PR #112, and I merged it
- Checked each acceptance test against the existing backend test names so the expected results match what the code actually does
- Checked which PR delivered each task by comparing the merged PRs and changed files, not just the issue titles
- Reviewed the generated plan and contribution log before committing

Decision:

- Accepted: the CI workflow as written
- Accepted: keeping the team's JobPosting.java and leaving my old edit in a git stash as a backup
- Accepted: acceptance tests only for the stories we actually built in Sprints 1 and 2, not the backlog stories
- Modified: the AI could not sign in to GitHub, so I opened and merged the CI pull request myself
- Rejected: having the AI write meeting minutes or fill in teammates' time spent, since it could not know those; it made templates instead

Reflection: The gap check was the most useful part, since it showed that acceptance tests and CI were missing entirely, which we would have lost marks for. Setting up CI was smaller than I expected because the tests already used an in-memory database. I learned the difference between GitHub Actions (the service) and a CI pipeline (the workflow file we write).

Responsible Person: Maria-Francesca Staicu (40324837)

Appendix A: Prompt / Response history (Prompts are paraphrased.)

Prompt 1: "Do you have the fresh git pull updates I just added to the SOEN341 folder?"

Response 1 (summary): The AI found my local main was 53 commits behind origin/main, likely because my uncommitted change to JobPosting.java blocked the pull. Acceptance: Accepted.

Prompt 2: "Just bring everything in even if my files change."

Response 2 (summary): The AI stashed my change, pulled the 53 commits, and saw the team had rewritten the same file differently. It kept their version and left mine in the stash. Acceptance: Accepted.

Prompt 3: (screenshot of the Sprint 2 delivery instructions) "Anything missing?"

Response 3 (summary): A checklist comparison of the repo and GitHub issues. The biggest gaps were no acceptance tests, no CI pipeline, no meeting minutes, no Sprint 2 plan or contribution log, and my missing Sprint 2 AI log. Acceptance: Accepted.

Prompt 4: "Set up CI."

Response 4 (summary): The AI checked the backend build, ran the 74 tests locally, wrote the GitHub Actions workflow, and committed it on a new branch. Acceptance: Accepted.

Prompt 5: "What's the point of requiring a CI pipeline if GitHub already has something built in?" and "How does the CI you made work specifically?"

Response 5 (summary): GitHub Actions is only the engine that runs jobs; the pipeline is the workflow file that says what to check and when. Then a walkthrough of each part of ci.yml. Acceptance: Accepted.

Prompt 6: "Go ahead" (push and open the PR)

Response 6 (summary): The AI pushed the branch but could not sign in to GitHub, so I opened and merged PR #112 myself. CI passed on the PR. Acceptance: Modified.

Prompt 7: "Go ahead, do everything. Someone mentioned we also need acceptance tests for Sprint 1."

Response 7 (summary): The AI wrote 22 acceptance tests for the Sprint 1 and Sprint 2 stories and a script to create them as linked GitHub issues. It also drafted the Sprint 2 plan, the contribution log template, this AI log and a minutes template. It said it could not write the minutes or teammates' time itself. Acceptance: Accepted with changes.
