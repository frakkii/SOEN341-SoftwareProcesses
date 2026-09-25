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
- Database: Mysql (sql)
- AI Integration: tbd
- Testing tools: JUnit (Spring Boot Test)
- Hosting/Deployment: tbd

## Setup Instructions
```bash
git clone git@github.com:frakkii/SOEN341-SoftwareProcesses.git
cd SOEN341-SoftwareProcesses/src/backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
Then open http://localhost:8080. The Spring Boot app serves both the frontend and the API (`/api/...`). Frontend files live in `src/webapp/` and are copied into the app's static resources at build time, so restart the app after editing them.

Requires Java 17+. If port 8080 is already in use (e.g. by XAMPP/Apache), add `-Dspring-boot.run.arguments=--server.port=8081` and open http://localhost:8081. Users are stored in memory until the MySQL database is set up.

## Proposed Features
TBD

## Repository Link
https://github.com/frakkii/SOEN341-SoftwareProcesses.git
