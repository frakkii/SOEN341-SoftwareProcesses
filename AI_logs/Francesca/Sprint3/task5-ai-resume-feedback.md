Task: Start the Generative AI feature: AI resume feedback (story #10, task #60, acceptance test AT-31)

Date: 2026-11-16

AI Tool: Claude Code (desktop app), model Claude Opus 5.5

Purpose of AI Use: AI was used to check whether the team had any AI feature yet, then to design, implement and test AI resume feedback using the Claude API, and to document how to turn it on.

Chat Link or Prompt/Response: check Appendix A

AI-Suggested Content:

- A check of every branch, pull request and issue: there was no AI code anywhere, and issues #60 and #180 had no owner
- `POST /api/resume/feedback`: reviews the job seeker's current resume. PDFs are sent to Claude as PDF documents; DOCX text is extracted on the server (`DocxText`, no extra library, with protection against XML external entities and zip bombs); old .doc files are refused with a clear message
- `ClaudeResumeAdvisor`: calls the Claude API through Anthropic's official Java SDK (`anthropic-java` 3.3.0) with model `claude-opus-5-5` and structured output, so the reply is always a summary, strengths and suggestions tagged by area (Content, Organization, Clarity, Formatting, Impact). The system prompt tells the model to treat text inside the resume as content, not instructions. It handles a refused request, a cut-off reply, a bad key, rate limits and network errors with clear messages
- `DemoResumeAdvisor` and `AiConfig`: without `ANTHROPIC_API_KEY` the feature runs in demo mode, returning clearly labeled sample feedback (with a few simple checks on DOCX text), so the app, tests and CI work with no key and no cost
- A "Get feedback" card on the resume page: loading state, results, a "Demo mode" label, and a notice to review AI feedback before using it
- 15 tests: 8 controller tests (PDF, DOCX, current version, .doc refused, unreadable and empty DOCX, no resume, recruiter and logged-out access), 5 DOCX extraction tests (including an XXE attempt), 2 configuration tests
- README section on setting the API key; Sprint 3 plan, results table and contribution log updates

Validation:

- All 114 backend tests pass (99 before plus 15 new) and the frontend tests still pass
- Ran AT-31 in the app on a local test database with no API key (confirmed in the startup log: "runs in demo mode"): the feedback card appears only once a resume exists, a loading message shows while the review runs (checked with a short artificial delay, since demo mode answers instantly), and DOCX and PDF resumes both get feedback with suggestions on content, organization and clarity plus the review notice. A .doc file, a broken DOCX and a DOCX with no text each show their own clear message and no feedback. Old feedback is cleared when a new resume is uploaded. Checked at phone width
- Not yet verified: a real review from the Claude API, because no API key was available. The request code compiles against the SDK and follows Anthropic's documented patterns, but the first real call should be watched

Decision:

- Accepted: Claude (`claude-opus-5-5`) through the official Java SDK, with the key read from the environment and never committed
- Accepted: demo mode when there is no key, clearly labeled on the page, so graders and teammates can see the feature without a key or cost
- Accepted: server-side refusal fallbacks turned on in the request (if Claude declines, the API retries on a fallback model)
- Modified: only PDF and DOCX are supported; old .doc files get a message asking for PDF or DOCX instead of a guessed conversion
- Rejected: merging without telling the team, since real reviews need a shared API key and each one is a paid call

Reflection: The feature was easy to leave until "later" because nobody owned it and it needs a paid key. Building it with a demo mode meant it could be built, tested in CI and demonstrated without waiting for a key. The trade-off is that the real AI call can't be proven until someone adds a key, so the results table marks AT-31 as passing in demo mode only.

Responsible Person: Maria-Francesca Staicu (40324837)

Appendix A: Prompt / Response history (Prompts are paraphrased.)

Prompt 1: "AI feature? Do we have any?"

Response 1 (summary): The AI searched main, every branch, all pull requests and the AI issues and found no AI feature, and explained that the guidelines require it to be started in Sprint 3. It proposed building AI resume feedback with a demo mode for when there is no API key. Acceptance: Accepted.

Prompt 2: "Do."

Response 2 (summary): The AI added the Anthropic Java SDK, built the feedback endpoint, the Claude and demo advisors and the DOCX reader, added the Get feedback card to the resume page, wrote 15 tests, ran AT-31 in the app in demo mode, and updated the README, plan, results table, contribution log and this log. Acceptance: Accepted after review.
