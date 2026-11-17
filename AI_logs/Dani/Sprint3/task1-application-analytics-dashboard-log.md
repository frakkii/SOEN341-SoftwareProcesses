Task ID/Title: #56 – Application Summary Dashboard Frontend (User Story #33, Acceptance Test AT-29 #158)
Date: 2026-11-16
AI Tool: Claude (Claude Code)
Purpose of AI Use: I used AI to help me write parts of the frontend code, answer questions, and debug problems. I also used it to do a final check and help write the pull request description.
Chat Link or Prompt/Response: No link available. See Appendix A.
AI-Suggested Content:

* Helped write code for the application summary dashboard frontend.
* Suggested ways to fix issues while debugging.
* Helped explain parts of the code when I had questions.
* Helped review the code for possible problems before submission.
* Helped write and improve the pull request description.
* Specific files and functions: applications.html (page layout), js/analytics.js (counting statuses, grouping by month, filtering and sorting), js/charts.js (donut chart and monthly bar chart), js/applications.js (loads data from GET /api/applications and updates the page), css/analytics.css (styling). Also 28 frontend tests in src/webapp/tests/analytics.test.js, 4 backend tests for AT-29 in ApplicationControllerTests.java, and a frontend test job in .github/workflows/ci.yml.

Validation:

* Ran `./mvnw verify`: 86 tests run, 0 failures.
* Ran `node --test`: 28 tests passed.
* CI checks passed on PR #162.
* Manual testing: Not done yet. I will test the dashboard in the running app with real accounts before the demo.

Decision:

* Accepted – Used AI suggestions to help with writing the frontend code.
* Accepted – Used AI explanations to help understand and debug problems.
* Accepted – Used AI assistance when writing the pull request description.
* Modified – The percentages first showed 1 out of 3 as 34%, so I had it changed to normal rounding (33%). The page also scrolled sideways on a phone, which was fixed.
* Rejected – I did not use the documentation the AI wrote (AI log, report, contribution notes). I kept only the code and am writing the documents myself.

Reflection: I learned how to build the dashboard using the data from the existing GET /api/applications endpoint instead of adding a new backend endpoint, and how to test JavaScript logic with Node without opening a browser. Using AI helped me with coding, debugging, and understanding things I had questions about. I still checked the code using tests instead of relying only on AI. Next time, I would test the dashboard in the running app with real accounts earlier instead of waiting until the end.
Responsible Person: Dani Tannir (40298707)

Appendix A – Prompt / Response History

Prompt 1 (summary): I asked AI to help me write code for the application summary dashboard frontend.
Response 1 (summary): AI helped me with the coding process. Specific files and changes: created analytics.js, charts.js, applications.js and analytics.css, and updated applications.html with stat tiles, a status donut chart, a monthly chart, and search/filter/sort for the application history. It also added 28 frontend tests, 4 backend tests and a frontend CI job.
Acceptance: Accepted with modifications

Prompt 2 (summary): I asked AI questions about the code and how certain parts worked.
Response 2 (summary): AI provided explanations to help me understand the code better.
Acceptance: Accepted

Prompt 3 (summary): I used AI to help debug issues I encountered while working on the task.
Response 3 (summary): AI helped me investigate problems and suggested possible fixes. Exact fixes: changed the percentage rounding so 1 out of 3 shows 33% instead of 34%, fixed the monthly chart making the page scroll sideways on phones, and helped me install Node in WSL so I could run the frontend tests.
Acceptance: Accepted

Prompt 4 (summary): I asked AI to help with a final check of my work before submission.
Response 4 (summary): AI helped review the work for possible issues. Specific findings: all 86 backend tests and 28 frontend tests passed, only the 8 code files were in the commit, and the branch could be merged into main without conflicts.
Acceptance: Accepted

Prompt 5 (summary): I asked AI to help write the pull request description for my Sprint 3 contribution.
Response 5 (summary): AI helped me describe the work I had done for the dashboard.
Acceptance: Accepted with modifications (I asked for a simpler version)
