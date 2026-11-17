Task: Test, fix and merge the application analytics dashboard (story #33, task #56, PR #162), record its acceptance tests, and track Sprint 3 progress on GitHub

Date: 2026-11-16

AI Tool: Claude Code (desktop app), model Claude Opus 5.5

Purpose of AI Use: AI was used to open the Sprint 3 documentation PR, create the new acceptance test issues, check the team's progress on GitHub against the Sprint 3 guidelines, test Dani's dashboard PR in the running app, fix a bug it found, and record the acceptance test results.

Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:

- Opened PR #163 (Sprint 3 plan, acceptance tests AT-32 and AT-33, CI updates) and created issues #164 and #165 for AT-32 and AT-33 with the team's script, linked as sub-issues of story #33
- A progress check after Mario's #166 and Audrey's #167 were merged: #162 merges cleanly, #161 conflicts in 7 files, the notifications backend code is still missing, the AI feature (#60) has not started, there are no Sprint 3 meeting minutes, and only a few members have Sprint 3 AI logs
- A way to run the app for testing on an in-memory H2 database instead of the team's Supabase, so test accounts and applications never reach the shared database
- Test data created through the app's own API: two recruiters with 6 postings, job seeker Alice with one application in each status, Bob with one, and Carl with none
- A one-line CSS fix for a bug found during testing: in the "Applications over time" chart, months that show the year under them (the first month and every January) sat about 10px higher than the others because their label takes two lines. Giving every label the same height (`.bar-lbl{height:2.4em}`) lines them up
- Comments recording each acceptance test result on #158 (AT-29), #164 (AT-32) and #165 (AT-33), with the build tested and what was checked

Validation:

- Ran all 86 backend tests (including Dani's 4 new dashboard tests) and all 28 frontend unit tests on PR #162 merged with the latest main: all passed
- Tested the dashboard in the browser as Alice, Carl and a recruiter: counts per status, Bob's application excluded, donut chart and tables, search, status filter, no-match message, Clear filters, both sort orders, the 6 months / 12 months / All time ranges, Refresh after a new application, the empty state, the recruiter message, and phone width (375px) with no sideways scrolling
- Tried the CSS fix in the page before committing and measured the bar positions: before, the bars sat at two different heights; after, all at one, on both desktop and phone width
- CI passed on PR #162 after the fix commit, before merging
- Read every issue comment before it was posted

Decision:

- Accepted: testing on a local in-memory database instead of Supabase
- Accepted: the CSS fix, pushed to Dani's branch as commit ec81b77 ("Line up month bars when a label shows the year")
- Accepted: merging PR #162 after CI passed, and closing AT-29, AT-32, AT-33 and task #56
- Modified: the AT-33 result says the "earlier month" part was covered by the unit tests rather than by hand, because all test applications were made on the same day
- Rejected: a shortcut the AI first tried, setting a browser storage key to sign in; it removed it and signed in through the real login form instead, so the test matched what a user does

Reflection: Testing in the running app found a layout bug that 28 passing unit tests did not, because the tests check the chart's HTML but not how it looks on screen. Using an in-memory database let me create as much test data as I needed without touching the team's real data. I also learned that recording acceptance test results on the issues, with the build and steps, is what makes them evidence for the evaluation rather than just a list.

Responsible Person: Maria-Francesca Staicu (40324837)

Appendix A: Prompt / Response history (Prompts are paraphrased.)

Prompt 1: "Commit the working tree changes with a sensible message." Then "Create PR."

Response 1 (summary): The AI committed the Sprint 3 plan, acceptance tests, CI changes and logs, pushed the branch, and opened PR #163 with a description that flagged the missing notifications backend and the merge order with #161. Acceptance: Accepted.

Prompt 2: "Create the issues for AT-32 and AT-33." Then "Commit and push."

Response 2 (summary): The AI ran the team's acceptance test script for just those two tests, which created #164 and #165 and linked them under story #33. It then updated the Sprint 3 plan to reference them and pushed that to PR #163. Acceptance: Accepted.

Prompt 3: "Check GitHub right now and look at the Sprint 3 tasks, what else can I do?"

Response 3 (summary): A status table of every Sprint 3 requirement and a prioritized list. The top suggestion was to test and merge Dani's dashboard PR #162, since that was my assigned feature. Acceptance: Accepted.

Prompt 4: "Yes, test #162."

Response 4 (summary): The AI merged #162 with the latest main on a test branch, ran the backend and frontend tests, started the app on an in-memory database, created test accounts and applications, and went through every part of the dashboard in the browser. Everything worked except the chart baseline bug, and it showed a one-line fix that it had tried in the page. Acceptance: Accepted.

Prompt 5: "Push fix to #162."

Response 5 (summary): The AI committed the CSS fix on Dani's branch and pushed it. CI passed on the new commit. Acceptance: Accepted.

Prompt 6: "OK, what else is there to do?" Then "Merge and record."

Response 6 (summary): The AI merged PR #162, posted the result of AT-29, AT-32 and AT-33 on their issues with the build tested and each check, and closed them. Acceptance: Accepted.

Prompt 7: "Yes, write it" (this AI log).

Response 7 (summary): The AI wrote this log from the session. Acceptance: Accepted after review.
