# Sprint 2 — AI Usage Log

**Name:** Hirushi Rathnayaka

**Course:** SOEN 341 — Software Processes

**Sprint:** Sprint 2

**Task:** Job Search and Job Listing Backend (Issue #65)

**AI Tools Used:** GitHub Copilot and ChatGPT

**Time Spent:** 2 days

## 1. Planning and Understanding the Task

I used ChatGPT to help me understand my assigned Sprint 2 responsibilities and the requirements for User Story #3. We reviewed what the job search and job listing backend needed to support, including keyword searches, filtering, active job listings, and retrieving individual job details.

**How AI helped:** It helped me break down the requirements and identify the backend components I needed.

## 2. Backend Implementation

I used GitHub Copilot to assist with implementing the job search and job listing backend in Java using Spring Boot.

The implementation involved:
- `JobPosting.java` — Job posting model
- `JobPostingRepository.java` — Database queries
- `JobPostingService.java` — Search and filtering logic
- `JobPostingController.java` — REST API endpoints

Copilot also assisted with adding the database migration needed for the search filters.

**How AI helped:** It provided code suggestions and helped implement the required backend functionality.

## 3. Testing and Debugging

I used ChatGPT to guide me through running Maven commands in the VS Code terminal and understanding the test results.

I ran the job-search-specific tests and the complete backend test suite.

**Results:**
- 4 job-search controller tests passed.
- All 49 automated tests passed locally.
- No test failures or errors were reported.

**How AI helped:** It explained the test output and helped me understand what had been verified.

## 4. Database Migration Issue

While trying to run the application, I encountered a PostgreSQL authentication error because the database password was not configured in my PowerShell terminal.

After resolving that issue, I encountered a Flyway V7 migration checksum mismatch with the shared Supabase database.

I used ChatGPT and Copilot to investigate the error and understand why the migration was conflicting.

The migration was later adjusted during team integration.

**How AI helped:** It helped identify the database connection issue and distinguish it from the migration conflict without making unnecessary changes to the shared database.

## 5. GitHub and Collaboration

I used ChatGPT to help with Git commands for staging, committing, and pushing my work to my Sprint 2 branch.

My backend contribution was later included in the team's integration PR #101.

## 6. Reflection

AI was helpful throughout Sprint 2, especially when implementing the backend, understanding errors, and testing my code. It helped me work through problems step by step and understand what each part of the backend was doing.

I still needed to review the generated code, run the tests, and communicate issues with my teammates. One important thing I learned was that passing automated tests does not necessarily mean the application will run correctly with the shared database.

Overall, AI helped me complete my assigned backend work and troubleshoot issues more efficiently.
