Appendix A – Prompt / Response History

AI Tool: Claude (Claude Code)

Chat Link: No link available.

Task #41 – Job Application Submission

Prompt 1 (summary):
Help with the backend for submitting job applications, including checks for duplicate applications, missing resumes, and closed job postings.

Response 1 (summary):
AI helped with the submission endpoint, validation conditions, and test cases. Applications are saved with an initial status of "Applied."

Acceptance: Accepted with modifications.

Task #45 – Job Application Tracking

Prompt 2 (summary):
Help with the backend endpoints for viewing submitted applications and checking their current status.

Response 2 (summary):
AI helped with the endpoints for listing applications, retrieving a specific application, sorting by date, and restricting access to the application owner.

Acceptance: Accepted with modifications.

Final Validation

The backend was tested using ./mvnw test. All 45 tests passed, including 19 new tests.

The implementation covers application submission and tracking. Recruiter status updates are handled separately.
