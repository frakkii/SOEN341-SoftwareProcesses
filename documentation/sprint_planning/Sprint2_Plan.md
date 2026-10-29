# Sprint 2 Plan

**Team:** DASM (CareerConnect)
**Sprint 2:** starts 2026-10-12, ends _[due date]_
**Board:** CareerConnect Sprint Board (GitHub Projects), columns To Do → In Progress → In Review → Done

We planned Sprint 2 the same way as Sprint 1 ([DASM Sprint planning 1.xlsx](DASM%20Sprint%20planning%201.xlsx)): pick the user stories for the sprint, split each one into frontend, backend, database and testing tasks as GitHub issues, and assign each task to a team member. Each story also has at least two acceptance tests (see [ACCEPTANCE_TESTS.md](../acceptance_tests/ACCEPTANCE_TESTS.md)).

## Sprint goal

Deliver the four core Sprint 2 features end to end:

1. Profile and resume management
2. Job search
3. Application submission and tracking
4. Recruiter dashboard (job postings and applicants)

## Team

| Name | GitHub |
|---|---|
| Maria-Francesca Staicu | frakkii |
| Audrey Prevost | MothMother |
| Donald Champ | dchamp214 |
| Shanza Riasat | Shanza687 |
| Mario Habib | MarioHabib33 |
| Hirushi Rathnayaka | hirurathnayaka77 |
| Dani Tannir | datannir-bit |

## Stories and tasks

Status as of 2026-10-29. "Issue" is the GitHub issue state. "Delivered in" is the merged PR that implements it. Team members should correct anything this misses, since it was compiled from GitHub, not from the board.

### 1. Profile and resume management

**#69** As a job seeker, I want to create and edit a professional profile to showcase my skills for potential employers.
**#2** As a job seeker, I want to upload and update my resume so that recruiters can view my qualifications.
**#32** As a job seeker, I want to store multiple versions of my resume so that I can tailor which one I send to each job.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #70 Profile frontend | frakkii | Open | #101 |
| #71 Profile backend | _unassigned_ | Open | #101 |
| #72 Profile database integration | MothMother | Open | #101 |
| #73 Profile testing | _unassigned_ | Open | #101 |
| #36 Resume upload frontend | MarioHabib33 | Open | Upload page from 2026-09-28 commit |
| #37 Resume upload backend | Shanza687 | Open | Upload API from 2026-09-28 commit |
| #38 Resume update functionality | Shanza687 | Open | #101 |
| #39 Resume upload testing | Shanza687 | Open | #101 |
| #57 Multiple resume storage and selection | _unassigned_ | Open | #101 (versions kept); #104 still open |

### 2. Job search

**#3** As a job seeker, I want to search and filter job postings by criteria (location, industry, job type) so that I can find relevant opportunities quickly.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #64 Job search and filtering frontend | frakkii | Open | #101 |
| #65 Job search and filtering backend | hirurathnayaka77 | Open | #101 |
| #66 Job details page frontend | frakkii | Open | #101 |

### 3. Application submission and tracking

**#4** As a job seeker, I want to submit applications directly through the platform so that I don't have to use multiple external sites.
**#5** As a job seeker, I want to track the status of my applications (Applied, Interview, Offered, Rejected) so that I can stay organized throughout my job search.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #40 Job application submission frontend | frakkii | Open | #101 |
| #41 Job application submission backend | datannir-bit | Closed | #67 |
| #42 Application database integration | MothMother | Open | #67 |
| #43 Job application submission testing | datannir-bit | Open | #67 |
| #44 Application status tracking frontend | frakkii | Open | #74 |
| #45 Application status tracking backend | datannir-bit | Closed | #67 |
| #46 Application status database integration | MothMother | Open | #67 |
| #47 Application status tracking testing | datannir-bit | Open | #67 |

### 4. Recruiter dashboard

**#8** As a recruiter, I want to create and manage job postings so that I can attract qualified candidates.
**#9** As a recruiter, I want to view a list of applicants for each posting so that I can review and manage candidates efficiently.

| Task | Assignee | Issue | Delivered in |
|---|---|---|---|
| #48 Job posting management frontend | MothMother | Closed | #80, #92, #100 |
| #49 Job posting management backend | dchamp214 | Open | #101 |
| #50 Job posting database integration | MothMother | Closed | #101 |
| #51 Job posting management testing | dchamp214 | Open | #101 |
| #61 Recruiter applicant listing and management | _unassigned_ | Open | #101 |

### Supporting work

| Work | Owner | Delivered in |
|---|---|---|
| UI overhaul (home feed, navigation) | MarioHabib33 | #76 |
| Application checks updated for new `job_postings` columns | datannir-bit | #98 |
| Job search fix on Postgres, smaller DB connection pool | dchamp214 | #102, #103 |
| Continuous Integration pipeline (GitHub Actions) | frakkii | #112 |
| Acceptance tests for Sprint 1 and 2 stories | frakkii | this PR |

## Carried over to Sprint 3

These stories stay in the backlog: #6 deadline notifications, #7 saved jobs, #10 AI resume feedback, #30 messaging, #31 password reset, #33 application dashboard, #34 application withdrawal.

## Before the sprint ends

- [ ] Close the task issues above that are delivered, and move them to Done on the board
- [ ] Assign #57, #61, #71 and #73
- [ ] Merge or close Shanza's resume versioning PR #104 (and #105–#111)
- [ ] Run the acceptance tests and record each result on its issue
- [ ] Upload all Sprint 2 meeting minutes to `minutes/`
- [ ] Each member fills in their time and links in [Sprint2_MemberContribution.md](../Sprint2_MemberContribution.md)
