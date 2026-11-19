Task: Finish the Generative AI feature: line-by-line AI resume suggestions that the job seeker accepts or rejects, with figures they fill in, a downloadable improved resume and report, and saving as a new resume version (story #10)

Date: 2026-11-19

AI Tool: Claude Code (VS Code extension), model Claude Opus 5.5



Purpose of AI Use: I used Claude to check the state of the repository before starting, then to design, implement and test an AI feature that reads the job seeker's current resume and suggests improvements line by line, producing output files. Francesca gave me the go-ahead to finish the AI feature she started. Partway through I decided to replace her general feedback card with this feature and to let job seekers type in the real figures the AI can't know.



Chat Link or Prompt/Response: see Appendix A

AI-Suggested Content:
- A check before starting: my local `main` was 51 commits behind, two uncommitted files were set aside with `git stash`, and the open branches were listed so I could spot overlapping work (only the notifications branches were recent)
- Backend (`/api/resume/optimization`):
  - `POST` reads the current resume (PDF or DOCX) and asks Claude for every line in order, each with an optional rewrite, the area it improves and a one-sentence reason. The result is kept in the user's session
  - `POST /resume` and `POST /report` return an improved resume and a report of every suggestion as .docx files. `POST /save` stores the improved resume as the new current version, and earlier versions are kept
  - The page sends back only the numbers of the accepted suggestions and the figures typed into them, never free text, so a saved resume always comes from the AI's result and the user's own resume. Each stored result is tied to the logged-in user
  - Figures: when a rewrite needs a number the AI doesn't know, it writes a placeholder like `[X%]` or `[number of users]`. The job seeker types the real figure into it, and the improved resume can't be made while an accepted suggestion still has an empty figure. Brackets that were already in the person's own line are not treated as placeholders
- `ClaudeResumeAdvisor`: Claude API through the official Java SDK, model `claude-opus-5-5`, structured output generated from the `ResumeOptimization` record. The prompt tells the model never to invent employers, dates, skills or numbers, and to treat text inside the resume as content, not instructions. Error handling and refusal fallbacks are reused from Francesca's code
- `DemoResumeAdvisor`: without an API key, simple rule-based rewrites (for example "Responsible for" becomes "Led") plus a `[measurable result]` figure, clearly labeled as demo mode
- `DocxWriter`: writes .docx files with no extra library, the same approach as Francesca's `DocxText`
- Frontend: a new card on the resume page with Accept and Reject on every suggestion, Accept all and Reject all, a running count (accepted, rejected, to review, figures to fill in), input boxes inside the suggestion text, the two downloads and a confirmation before saving
- Removed: Francesca's "Get feedback" card, its endpoint, service, DTO and tests. Her PDF/DOCX reading, error handling and demo mode were kept and reused
- README section rewritten for the new feature



Validation:
- Tests: 126 backend tests pass on a clean build. The 8 feedback tests were removed with the feature, and 20 were added: 14 endpoint tests (all lines returned, only accepted suggestions used, figures go into the resume, empty or invalid figures refused, saving creates a new version, invalid suggestion numbers refused, PDF, .doc refused, no resume, recruiter and logged-out access), 4 DOCX writer tests and 2 placeholder tests
- A clean build mattered: the first run still counted the deleted feedback tests from old compiled files, so Claude ran `mvnw clean test` to get the real result
- JavaScript: the page script passed a syntax check, and the placeholder-splitting code was run in Node on sample lines to confirm it matches the server's rule and escapes text safely
- Running app: started locally in demo mode against Supabase. The page loads, the old feedback endpoint returns 404 and the new one requires login
- Not yet verified: a real review from the Claude API (no key used in this session) and my own click-through in the browser. Both should happen before the PR is merged



Decision:
- Line-by-line suggestions with accept/reject, both output files and "Save as new resume version": Accepted (my choices when Claude asked)
- Keeping the AI result on the server and sending back only suggestion numbers and figures: Accepted
- Placeholders for unknown figures, filled in by the job seeker: Accepted. The improved resume and saving require every accepted figure, while the report can be downloaded before they are filled in
- Removing Francesca's feedback card: my decision, after she agreed to me finishing the AI feature
- The improved resume is rebuilt as a clean, simple document instead of keeping the original layout: Accepted
- My own Claude API key for demos: Accepted, set only in my own terminal and never committed. Each click on Suggest improvements is a paid call
- The local database password: the one saved on my machine was out of date, so Claude used the README password for the local run only



Reflection: Most of the AI feature already existed, so checking `main` first saved me from building a second version beside Francesca's. The most useful AI output wasn't the rewriting but the guardrails around it: the model is told not to invent numbers, and placeholders leave the real figures to the job seeker, so the resume stays truthful. Keeping the AI result on the server means nobody can send their own text and have it saved as their resume. A real call with an API key is still the important check left.

Responsible Person: Donald Champ (40270957)



Appendix A: Prompt / Response history
(Prompts are paraphrased.)

Prompt 1: "We're going to implement a feature. Are we on main, and are there open branches to be aware of?"

Response 1 (summary): On `main` but 51 commits behind, with two uncommitted files. Claude listed the unmerged branches and flagged that editing the existing `V7` migration would break Flyway checksums.
Validation: Accepted.

Prompt 2: "Implement an AI feature that reads the applicant's current resume, suggests improvements line by line, and produces an output file."

Response 2 (summary): Claude found that Francesca's AI feedback had just been merged and proposed building on it. It asked about the output (both an improved resume and a report), the review step (accept or reject each) and saving (also offer "Save as new version"). It then stashed my local changes, updated `main`, created `feature/ai-resume-optimizer` and built the backend, the frontend and 15 tests.
Validation: Accepted. 129 tests passing at that point.

Prompt 3: "Francesca has given me the green light to finish."

Response 3 (summary): Claude continued with her `ResumeAdvisor` design instead of a separate system.
Validation: Accepted.

Prompt 4: "Can I demo it first? And can I use my own Claude API key?"

Response 4 (summary): Claude started the app in demo mode, explained the cost per click and how to set the key in my own terminal without pasting it into the chat or committing it.
Validation: Accepted.

Prompt 5: "Remove Francesca's resume feedback. When a suggestion needs a figure, let the user enter the specific metric."

Response 5 (summary): Claude removed the feedback feature, kept the shared parts, added figure input boxes with server-side checks, updated the prompt, the README and the tests, and restarted the app.
Validation: Accepted. 126 tests passing on a clean build.

Prompt 6: "Write my Sprint 3 AI log and create the PR."

Response 6 (summary): Claude drafted this log from the session and opened the pull request.
Validation: Draft written by Claude from the session. I review and edit it before submitting.
