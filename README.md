# CareerConnect

## Project Description
CareerConnect is a web-based platform that helps job seekers manage their job search with features such as creating profiles, uploading resumes, searching for jobs, and tracking applications in one 
place.

**Primary Users:** Job Seekers and Recruiters

## Problem
Job seekers lose track of applications spread across multiple platforms, with no easy way to 
monitor status, deadlines, or follow-ups.

## Solution
A centralized platform where users can search/apply for jobs, track application status 
(Applied, Interview, Offered, Rejected), manage resumes, and get deadline reminders. Recruiters 
can post and manage job listings.

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
