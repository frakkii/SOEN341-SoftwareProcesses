# Sprint 2 Plan

**Team:** DASM (CareerConnect)
**Sprint 2:** planned at Meeting 3 on 2026-10-05 ([minutes](../../minutes/2026-10-05_meeting3_sprint2-planning.md)), ends _[due date]_
**Board:** CareerConnect Sprint Board (GitHub Projects), columns To Do → In Progress → In Review → Done

We planned Sprint 2 the same way as Sprint 1 ([DASM Sprint planning 1.xlsx](DASM%20Sprint%20planning%201.xlsx)): pick the user stories for the sprint, split each one into frontend, backend, database and testing tasks as GitHub issues, and assign each task to a team member. Each story also has at least two acceptance tests (see [ACCEPTANCE_TESTS.md](../acceptance_tests/ACCEPTANCE_TESTS.md)).

## Sprint objectives

Advance the functionality of the web application, expand the database structure, and fix any unresolved issues from Sprint 1.

Deliver the four core Sprint 2 features end to end:

1. Profile and resume management
2. Job search
3. Application submission and tracking
4. Recruiter dashboard (job postings and applicants)

## Team

Responsibilities agreed at Meeting 3. Everyone also owns at least one user story and adds its subtasks.

| Name | GitHub | Sprint 2 responsibilities |
|---|---|---|
| Maria-Francesca Staicu | frakkii | Sprint 1 bugs, frontend job application submission and tracking, Sprint 2 sprint planning; later took over the CI pipeline and acceptance tests |
| Audrey Prevost | MothMother | Database maintenance, frontend recruiter dashboard and posting creation, posting management |
| Donald Champ | dchamp214 | Backend recruiter dashboard and posting management |
| Shanza Riasat | Shanza687 | Resume upload backend, Sprint 2 report submission (CI moved to Francesca) |
| Mario Habib | MarioHabib33 | Resume upload and user profile frontend |
| Hirushi Rathnayaka | hirurathnayaka77 | Sprint 1 bugs, job search and job listing backend (acceptance tests moved to Francesca) |
| Dani Tannir | datannir-bit | Backend job application submission and tracking |

## Stories and tasks

Status as of 2026-10-29. "Issue" is the GitHub issue state. "Delivered in" is the merged PR that implements it. Team members should correct anything this misses, since it was compiled from GitHub, not from the board.

### 1. Profile and resume management

**#69** As a job seeker, I want to create and edit a professional profile to showcase my skills for potential employers.
**#2** As a job seeker, I want to upload and update my resume so that recruiters can view my qualifications.
**#32** As a job seeker, I want to store multiple versions of my resume so that I can tailor which one I send to each job.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #70 Profile frontend | frakkii | Closed | #101 |
| #71 Profile backend | _unassigned_ | Closed | #101 |
| #72 Profile database integration | MothMother | Closed | #101 |
| #73 Profile testing | _unassigned_ | Closed | #101 |
| #36 Resume upload frontend | MarioHabib33 | Closed | Upload page from 2026-09-28 commit |
| #37 Resume upload backend | Shanza687 | Closed | Upload API from 2026-09-28 commit |
| #38 Resume update functionality | Shanza687 | Closed | #101 |
| #39 Resume upload testing | Shanza687 | Closed | #101 |
| #57 Multiple resume storage and selection | _unassigned_ | Open | #101 (versions kept); choosing a version still to do, from Shanza's branch |

### 2. Job search

**#3** As a job seeker, I want to search and filter job postings by criteria (location, industry, job type) so that I can find relevant opportunities quickly.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #64 Job search and filtering frontend | frakkii | Closed | #101 |
| #65 Job search and filtering backend | hirurathnayaka77 | Closed | #101 |
| #66 Job details page frontend | frakkii | Closed | #101 |

### 3. Application submission and tracking

**#4** As a job seeker, I want to submit applications directly through the platform so that I don't have to use multiple external sites.
**#5** As a job seeker, I want to track the status of my applications (Applied, Interview, Offered, Rejected) so that I can stay organized throughout my job search.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #40 Job application submission frontend | frakkii | Closed | #101 |
| #41 Job application submission backend | datannir-bit | Closed | #67 |
| #42 Application database integration | MothMother | Closed | #67 |
| #43 Job application submission testing | datannir-bit | Closed | #67 |
| #44 Application status tracking frontend | frakkii | Closed | #74 |
| #45 Application status tracking backend | datannir-bit | Closed | #67 |
| #46 Application status database integration | MothMother | Closed | #67 |
| #47 Application status tracking testing | datannir-bit | Closed | #67 |

### 4. Recruiter dashboard

**#8** As a recruiter, I want to create and manage job postings so that I can attract qualified candidates.
**#9** As a recruiter, I want to view a list of applicants for each posting so that I can review and manage candidates efficiently.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #48 Job posting management frontend | MothMother | Closed | #80, #92, #100 |
| #49 Job posting management backend | dchamp214 | Closed | #101 |
| #50 Job posting database integration | MothMother | Closed | #101 |
| #51 Job posting management testing | dchamp214 | Closed | #101 |
| #61 Recruiter applicant listing and management | _unassigned_ | Closed | #101 |

### 5. Logout (added during the sprint)

**#139** As a user, I want to log out of my account so that my account remains secure.

From the Sprint 1 planning sheet; added to Sprint 2 because the recruiter Logout links only cleared the browser and job seekers had no logout.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #140 Logout backend | frakkii | Open | #138 |
| #141 Logout frontend | frakkii | Open | #138 |

### Supporting work

| Work | Owner | Delivered in |
|---|---|---|
| UI overhaul (home feed, navigation) | MarioHabib33 | #76 |
| Application checks updated for new `job_postings` columns | datannir-bit | #98 |
| Job search fix on Postgres, smaller DB connection pool | dchamp214 | #102, #103 |
| Continuous Integration pipeline (GitHub Actions) | frakkii | #112 |
| Acceptance tests for Sprint 1 and 2 stories (#113–#134, #142, #143) | frakkii | #135, #138 |

## Carried over to Sprint 3

These stories stay in the backlog: #6 deadline notifications, #7 saved jobs, #10 AI resume feedback, #30 messaging, #31 password reset, #33 application dashboard, #34 application withdrawal.

## Before the sprint ends

- [x] Close the task issues above that are delivered (move them to Done on the board if it does not do so automatically)
- [ ] Assign #57
- [x] Close Shanza's resume PRs #104–#111 (they overwrote newer code); she will bring her new endpoints in through one PR from current `main`
- [ ] Run the acceptance tests and record each result on its issue
- [ ] Upload the other Sprint 2 meeting minutes to `minutes/` (Meeting 3 is there)
- [ ] Each member fills in their time spent in [Sprint2_MemberContribution.md](../Sprint2_MemberContribution.md)
