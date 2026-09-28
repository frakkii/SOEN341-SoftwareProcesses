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

## Running the Web App

### What you need
- **Java 17 or newer.** Check with `java -version`. You don't need to install Maven; the project includes its own copy (`mvnw`).
- **Git**, to clone the repository.

### 1. Get the code
```bash
git clone git@github.com:frakkii/SOEN341-SoftwareProcesses.git
cd SOEN341-SoftwareProcesses
```

### 2. Set the database password
The app stores everything in the team's Supabase database. The password is `soen341proj`. Set it in the same terminal you'll start the app from:

| Terminal | Command |
|---|---|
| macOS / Linux / Git Bash | `export SUPABASE_DB_PASSWORD='soen341proj'` |
| Windows PowerShell | `$env:SUPABASE_DB_PASSWORD='soen341proj'` |
| Windows Command Prompt | `set SUPABASE_DB_PASSWORD=soen341proj` |

This only lasts until you close the terminal, so set it again each time you open a new one.

### 3. Start the app
Run this from the repository root:

| Terminal | Command |
|---|---|
| macOS / Linux / Git Bash | `./run.sh` |
| Windows PowerShell | `.\run` |
| Windows Command Prompt | `run` |

The first start takes a minute or two while dependencies download. The app is ready when the output shows `Started DemoApplication`.

### 4. Open it
Go to **http://localhost:8081**. You can register a job seeker or recruiter account there and sign in. Job seekers can upload their resume under **My Tools → My CVs & Cover Letters**.

Stop the app with **Ctrl+C** in its terminal.

### Making changes
- **Frontend** files (HTML/CSS/JS) are in `src/webapp/`. **Backend** code (Spring Boot) is in `src/backend/`. The one app serves both the pages and the API (`/api/...`).
- Restart the app after any change, including frontend edits, since the pages are copied into the app when it starts.
- Run the tests with `./mvnw test` from `src/backend` (`.\mvnw test` on Windows). They use a temporary in-memory database, so they don't need the password and never touch Supabase.

### Troubleshooting
| Problem | Fix |
|---|---|
| `Port 8081 was already in use` | Another copy of the app is still running, probably in another terminal or IDE. Stop it, or start this one on another port: `./run.sh -Dspring-boot.run.arguments=--server.port=8082` |
| `password authentication failed` or a missing-password error | The password isn't set in this terminal. Repeat step 2. |
| `max clients reached` | Supabase allows about 15 connections and each running app holds up to 10. Close any copies of the app you're not using. |
| `Migration checksum mismatch` or `Detected applied migration not resolved locally` | Your code's database migrations don't match what's in Supabase. Pull the latest `main`; if it still happens, see the Database section below. |
| `java: command not found` or `release version 17 not supported` | Install Java 17+ and make sure `java -version` shows it. |

### Database
- **Connection settings:** these are in `src/backend/src/main/resources/application.properties`. To use a different database, set `SUPABASE_DB_URL` (a JDBC URL such as `jdbc:postgresql://<host>:5432/postgres?sslmode=require`) and `SUPABASE_DB_USER` along with the password.
- **Tables:** job seekers are in `person_user`, recruiters in `employer_user`, and each job seeker's resume in `resume`.
- **Migrations:** the schema is managed by Flyway files in `src/backend/src/main/resources/db/migration`, which run automatically when the app starts. `common/` works on every database; `postgresql/` and `h2/` hold the few statements that differ between Supabase and the test database.
- **Changing the schema:** add a new `V<next number>__description.sql` file (in `common/` unless it needs database-specific SQL). Don't edit a migration that has already run, and don't change tables in the Supabase dashboard. Merge new migrations to `main` promptly: once one runs against Supabase, everyone else's app needs the same file to start.

## Proposed Features
TBD

## Repository Link
https://github.com/frakkii/SOEN341-SoftwareProcesses.git
