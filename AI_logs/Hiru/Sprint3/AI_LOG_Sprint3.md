Sprint 3 — AI Usage Log

Name: Hirushi Rathnayaka
Course: SOEN 341 — Software Processes
Sprint: 3
Tasks: Notifications Frontend and Acceptance Tests
AI Tools Used: GitHub Copilot and ChatGPT

1. Planning and Requirements

For Sprint 3, I worked on the Notifications frontend and the acceptance tests for CareerConnect.

I used ChatGPT to help review the project requirements and organize the work. I also went through the existing user stories and acceptance criteria to understand what was already covered and what still needed to be added.

My main responsibility was the frontend, so I made sure the implementation stayed separate from the backend work being handled by other team members.

2. Notifications Frontend

I worked on the Notifications interface for both job seekers and recruiters, using GitHub Copilot to assist with parts of the HTML, CSS, and JavaScript implementation.

The frontend supports:

Application status notifications.

Interview invitations.

Application deadline reminders.

New application alerts for recruiters.

Read/unread indicators and mark-as-read interactions.

Empty and error states.

I also worked on integrating the Notifications page into the existing navigation while keeping the design consistent with the rest of CareerConnect.

During testing, I noticed that recruiter accounts were displaying job-seeker notifications and navigation. I reviewed the issue and used Copilot to help adjust the role-based filtering and navigation.

3. Testing and Debugging

Since the Notifications backend was not available yet, I tested the interface using temporary demo data.

I tested the page using both a recruiter account and a job-seeker account to verify that each role displayed the correct notification types.

I also checked the unread counter, mark-as-read interactions, and empty state. The frontend JavaScript syntax checks passed.

The demo mode was kept separate from normal operation, and the frontend still uses the expected API when demo mode is disabled.

These tests verified the frontend behavior with sample data, not the complete backend integration.

I used ChatGPT mainly to help organize the testing process and interpret issues when something did not behave as expected.

4. Acceptance Tests

I was also responsible for preparing Sprint 3 acceptance tests.

I reviewed the existing Sprint 2 acceptance tests to maintain the same structure and created AT-25 through AT-31.

These cover:

AT-25–AT-28: Notifications and reminders.

AT-29: Application analytics.

AT-30: Recruiter job-posting management.

AT-31: AI-generated resume feedback.

I used ChatGPT and GitHub Copilot to help structure the test scenarios and review the requirements.

During the review, I identified that some tests needed additional coverage, including notification ownership, duplicate reminders, empty states, and error handling.

I updated the tests and their corresponding GitHub issues (#154–#160). I also reviewed and expanded user story #6 to include all required notification types.

All acceptance tests remain marked as Not yet run because the relevant functionality still needs to be verified against the completed application.

5. GitHub and Collaboration

I used Git throughout Sprint 3 to manage my changes on a separate branch.

My work was submitted through PR #161, which includes the Notifications frontend, the frontend API contract documentation, and the acceptance tests.

I used ChatGPT for occasional guidance with Git commands and GitHub CLI, particularly when creating and updating the acceptance-test issues.

I also reviewed the changes before committing, verified the available GitHub checks, and kept unrelated temporary files out of the commits.

6. Reflection

AI tools were useful for speeding up parts of the implementation, organizing acceptance tests, and troubleshooting issues.

However, the generated suggestions still required review and testing. For example, I had to correct the notification filtering between recruiters and job seekers and improve several acceptance tests after checking them against the user stories.

One important thing I learned was the difference between testing a frontend with demo data and verifying a complete feature with backend integration. I also gained a better understanding of how acceptance tests connect user requirements to the functionality being developed.

Overall, I used AI as a supporting tool throughout Sprint 3 while remaining responsible for reviewing the work, making corrections, testing the interface, and preparing my contribution for team integration.
