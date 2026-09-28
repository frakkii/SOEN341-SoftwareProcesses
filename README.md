# CareerConnect

## Project Description
CareerConnect is a web-based platform that brings the entire job search into one place. Job seekers can build a profile, upload and manage multiple versions of their resume, search and apply for jobs, and track every application from submission to final decision. Recruiters can post openings, review applicants, and update candidates on their status, so both sides of the hiring process work from the same up-to-date information.

## Primary Users
Job seekers are students, recent graduates, and working professionals who apply to many positions at once and need a reliable way to stay organized. Recruiters are hiring managers and HR staff who need a simple way to publish listings, collect applications, and communicate progress to candidates.

## Problem
Today's job search is fragmented. Applications are scattered across company career pages, job boards, and email, and most job seekers fall back on spreadsheets or memory to keep track. As a result, they miss deadlines, forget to follow up after interviews, lose track of which resume version they sent to which employer, and often never hear back about where they stand. Recruiters face the mirror image of this problem: candidate information arrives through multiple channels, and keeping applicants informed is time-consuming, so many simply go silent.

## Solution
CareerConnect gives job seekers a single dashboard for their search. Users can search and filter job listings by title, location, job type, and keywords, then apply directly with a resume selected from their saved versions. Each application appears on a tracker with a clear status (Applied, Interview, Offered, or Rejected) that the user or the recruiter can update. The platform sends reminders for upcoming application deadlines, scheduled interviews, and follow-ups that are due, so nothing slips through the cracks. Users can also add personal notes to each application, such as interviewer names or questions to prepare.

On the recruiter side, CareerConnect lets employers create, edit, and close job postings, view the list of applicants for each position, access submitted resumes, and move candidates through the hiring stages. Status changes made by the recruiter are reflected immediately in the job seeker's tracker, which reduces uncertainty for candidates and cuts down on repetitive status-update emails for recruiters.

## Team Members
| Name | Student ID | GitHub Username |
|------|-----------|-----------------|
| Maria-Francesca Staicu | 40324837 | @frakkii |
| Audrey Prevost | 40027535 | @MothMother |
| Donald Champ | 40270957 | @dchamp214 |
| Shanza Riasat | 40337845 | @Shanza687 |
| Mario Habib | 40283335 | @MarioHabib33 |
|  Hirushi Rathnayaka | 40237379 | @hirurathnayaka77 | 
| Dani Tannir  |  40298707  |  @datannir-bit  |

## Technologies Used
- Frontend: HTML/CSS/JavaScript (served by Spring Boot)
- Backend: Spring Boot (Java 17)
- Database: PostgreSQL hosted on Supabase (schema managed with Flyway)
- AI Integration: tbd
- Testing tools: JUnit (Spring Boot Test)
- Hosting/Deployment: tbd

## Setup Instructions
```bash
git clone git@github.com:frakkii/SOEN341-SoftwareProcesses.git
cd SOEN341-SoftwareProcesses
export SUPABASE_DB_PASSWORD='...'   # Windows PowerShell: $env:SUPABASE_DB_PASSWORD='...'
./run.sh        # Windows PowerShell: .\run   (cmd: run)
```
The launcher starts the app from `src/backend` for you (same as running `./mvnw spring-boot:run` there). Stop it with Ctrl+C. Then open http://localhost:8081. The Spring Boot app serves both the frontend and the API (`/api/...`). Frontend files live in `src/webapp/` and are copied into the app's static resources at build time, so restart the app after editing them.

Requires Java 17+. The app runs on port 8081 (set in `src/backend/src/main/resources/application.properties`) so it doesn't clash with XAMPP/Apache on 8080.

### Database
The app stores its data in the team's Supabase Postgres database. The connection host and user are already in `application.properties`; you only need to set the database password (ask a teammate for it, and never commit it) in the `SUPABASE_DB_PASSWORD` environment variable before starting the app. To use a different database, also set `SUPABASE_DB_URL` (a JDBC URL, e.g. `jdbc:postgresql://<host>:5432/postgres?sslmode=require`) and `SUPABASE_DB_USER`.

Job seekers are stored in `person_user` and recruiters in `employer_user`. The schema is managed by Flyway migrations in `src/backend/src/main/resources/db/migration`, which run when the app starts: `common/` holds SQL that works everywhere, and `postgresql/` and `h2/` hold the few statements that differ between Supabase and the test database. Version 1 is the schema originally created in the Supabase dashboard, so Flyway skips it there. To change the schema, add a new `V<next number>__description.sql` file (in `common/` unless it needs database-specific SQL) instead of editing an existing migration or changing tables in the dashboard. Tests run against an in-memory H2 database, so `./mvnw test` doesn't need the password.

## Proposed Features
TBD

## Repository Link
https://github.com/frakkii/SOEN341-SoftwareProcesses.git
