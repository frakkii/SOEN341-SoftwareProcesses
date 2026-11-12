# Acceptance Tests (Sprints 1, 2 and 3)

Each test is also a GitHub issue labeled `acceptance-test` and linked to its user story as a sub-issue.
Regenerate this file with `.\create-acceptance-test-issues.ps1 -Markdown`.

## #1 Create an account and log in (Sprint 1)

### AT-01: New job seeker registers and logs in

- **User story:** #1 (Create an account and log in)
- **Related tasks:** #12, #13, #14, #15, #16
- **Sprint:** 1

**Given** I am on the login page (index.html) and have no account

**Steps**
1. Switch to Register
2. Fill in every required field with valid values and choose Job Seeker
3. Submit
4. Log in with the same email and password

**Then** A success message is shown, the account is created, and logging in takes me to the homepage with my name displayed

**Automated coverage:** `AuthControllerTests.registerThenLoginReturnsUserDetails`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-02: Registering with an email that is already used is rejected

- **User story:** #1 (Create an account and log in)
- **Related tasks:** #12, #13, #14, #15, #16
- **Sprint:** 1

**Given** An account already exists for test@example.com

**Steps**
1. Open the Register form
2. Enter valid details using test@example.com
3. Submit

**Then** An error says the email is already in use and no second account is created

**Automated coverage:** `AuthControllerTests.registerRejectsDuplicateEmail, registerRejectsEmailAlreadyUsedByOtherAccountType`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-03: Invalid registration fields and wrong passwords show errors

- **User story:** #1 (Create an account and log in)
- **Related tasks:** #12, #13, #14, #15, #16
- **Sprint:** 1

**Given** I am on the login page

**Steps**
1. Try to register with a blank name and an invalid email
2. Try to log in to an existing account with the wrong password

**Then** Each attempt shows an error message naming the problem, and I stay on the login page

**Automated coverage:** `AuthControllerTests.registerListsMissingFields, registerRejectsInvalidEmail, loginRejectsWrongPassword`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #2 Upload and update my resume (Sprint 2)

### AT-04: Job seeker uploads a PDF resume

- **User story:** #2 (Upload and update my resume)
- **Related tasks:** #36, #37, #38, #39
- **Sprint:** 2

**Given** I am logged in as a job seeker with no resume

**Steps**
1. Open the resume page
2. Choose a PDF file under 5 MB
3. Upload it

**Then** A confirmation is shown and my resume can be opened from the resume page

**Automated coverage:** `ResumeControllerTests.jobSeekerCanUploadAndDownloadResume`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-05: Unsupported or empty resume files are rejected

- **User story:** #2 (Upload and update my resume)
- **Related tasks:** #36, #37, #38, #39
- **Sprint:** 2

**Given** I am logged in as a job seeker

**Steps**
1. Try to upload a .exe file
2. Try to upload an empty file

**Then** Each upload shows an error message and my current resume is unchanged

**Automated coverage:** `ResumeControllerTests.uploadRejectsOtherFileTypes, uploadRejectsEmptyFile`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #32 Store multiple versions of my resume (Sprint 2)

### AT-06: Uploading a new resume keeps the previous version

- **User story:** #32 (Store multiple versions of my resume)
- **Related tasks:** #57
- **Sprint:** 2

**Given** I am logged in as a job seeker and already uploaded resume A

**Steps**
1. U

**Then** Resume B becomes my current resume and resume A is still stored as an earlier version

**Automated coverage:** `ResumeControllerTests.uploadingAgainReplacesTheCurrentResumeAndKeepsTheOldVersion`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-07: Job seekers can only see their own resumes

- **User story:** #32 (Store multiple versions of my resume)
- **Related tasks:** #57
- **Sprint:** 2

**Given** Job seekers Alice and Bob have each uploaded a resume

**Steps**
1. Log in as Bob
2. Open the resume page

**Then** Only Bob's resume is shown; Alice's resume cannot be opened

**Automated coverage:** `ResumeControllerTests.jobSeekersOnlySeeTheirOwnResume`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #69 Create and edit a professional profile (Sprint 2)

### AT-08: Job seeker edits their profile and it is saved

- **User story:** #69 (Create and edit a professional profile)
- **Related tasks:** #70, #71, #72, #73
- **Sprint:** 2

**Given** I am logged in as a job seeker

**Steps**
1. Open the profile page
2. Fill in headline, About and Skills
3. Save
4. Log out, log back in and reopen the profile page

**Then** A confirmation is shown and the saved headline, About and Skills are still there

**Automated coverage:** `ProfileControllerTests.jobSeekerCanEditTheirProfileAndItIsSaved`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-09: Invalid profile input is rejected and the profile needs a job seeker

- **User story:** #69 (Create and edit a professional profile)
- **Related tasks:** #70, #71, #72, #73
- **Sprint:** 2

**Given** I am logged in as a job seeker

**Steps**
1. Enter a headline longer than the allowed length and save
2. Log in as a recruiter and open the profile page

**Then** The long headline shows an error and is not saved; the recruiter cannot open a job seeker profile

**Automated coverage:** `ProfileControllerTests.rejectsFieldsThatAreTooLongOrInvalid, profileRequiresAJobSeeker`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #3 Search and filter job postings (Sprint 2)

### AT-10: Keyword search and filters return matching open jobs

- **User story:** #3 (Search and filter job postings)
- **Related tasks:** #64, #65, #66
- **Sprint:** 2

**Given** There are open postings in different locations and job types, plus one closed posting

**Steps**
1. Open the jobs page
2. Search a keyword from a job title or company
3. Add a location and job type filter

**Then** Only open, unexpired postings matching all criteria are listed, each with title, company, location and job type

**Automated coverage:** `JobPostingControllerTests.listsOnlyActiveAndUnexpiredJobsWithCompanyName, appliesKeywordAndAllFiltersTogether`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-11: No matches shows a message; a job opens its details page

- **User story:** #3 (Search and filter job postings)
- **Related tasks:** #64, #65, #66
- **Sprint:** 2

**Given** There are open postings

**Steps**
1. Search for a keyword that matches nothing
2. Clear the search and click a posting

**Then** A "no jobs found" message is shown; clicking a posting opens job.html with its full details

**Automated coverage:** `JobPostingControllerTests.returnsEmptyArrayForNoMatchesAndNotFoundForMissingOrUnavailableDetails, showsTheDetailsOfAnOpenJob`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #4 Submit applications on the platform (Sprint 2)

### AT-12: Job seeker applies to an open job

- **User story:** #4 (Submit applications on the platform)
- **Related tasks:** #40, #41, #42, #43
- **Sprint:** 2

**Given** I am logged in as a job seeker with an uploaded resume, viewing an open posting

**Steps**
1. Click Apply
2. Confirm the application

**Then** A confirmation is shown and the application appears on my applications page with status Applied

**Automated coverage:** `ApplicationControllerTests.jobSeekerCanApplyAndGetsConfirmation, ApplicationTest.newApplicationStartsAsApplied`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-13: Invalid applications are blocked with a message

- **User story:** #4 (Submit applications on the platform)
- **Related tasks:** #40, #41, #42, #43
- **Sprint:** 2

**Given** I am logged in as a job seeker

**Steps**
1. Apply to the same job a second time
2. Apply to a closed or expired posting
3. Apply without having uploaded a resume

**Then** Each attempt shows an error explaining why and no application is created

**Automated coverage:** `ApplicationControllerTests.cannotApplyTwiceToTheSameJob, applyRejectsClosedPosting, applyRejectsPostingPastItsDeadline, applyRequiresAnUploadedResume`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #5 Track the status of my applications (Sprint 2)

### AT-14: Job seeker sees only their own applications

- **User story:** #5 (Track the status of my applications)
- **Related tasks:** #44, #45, #46, #47
- **Sprint:** 2

**Given** Alice has applied to two jobs and Bob to one

**Steps**
1. Log in as Alice
2. Open the applications page

**Then** Alice's two applications are listed with job title, company, submission date and status; Bob's is not shown

**Automated coverage:** `ApplicationControllerTests.jobSeekerSeesAllTheirApplications, jobSeekersOnlySeeTheirOwnApplications`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-15: Status changes made by the recruiter show on the tracker

- **User story:** #5 (Track the status of my applications)
- **Related tasks:** #44, #45, #46, #47
- **Sprint:** 2

**Given** I applied to a job and the recruiter moved my application to Interview

**Steps**
1. O

**Then** The application shows status Interview

**Automated coverage:** `ApplicationControllerTests.jobSeekerSeesTheCurrentStatus, RecruiterControllerTests.statusMovesThroughTheStagesAndTheJobSeekerSeesIt`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-16: A new job seeker sees an empty-state message

- **User story:** #5 (Track the status of my applications)
- **Related tasks:** #44, #45, #46, #47
- **Sprint:** 2

**Given** I am logged in as a job seeker who has never applied

**Steps**
1. O

**Then** A message says I have no applications yet

**Automated coverage:** `ApplicationControllerTests.newJobSeekerHasNoApplications`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #8 Create and manage job postings (Sprint 2)

### AT-17: Recruiter creates a job posting

- **User story:** #8 (Create and manage job postings)
- **Related tasks:** #48, #49, #50, #51
- **Sprint:** 2

**Given** I am logged in as a recruiter

**Steps**
1. Open Create Posting
2. Fill in title, department, salary, skills, description and a future deadline
3. Submit

**Then** A success message is shown, the posting appears in my postings, and job seekers can find it in job search

**Automated coverage:** `RecruiterControllerTests.recruiterSeesTheirPostingsWithApplicantCounts`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-18: Posting with a past deadline is rejected

- **User story:** #8 (Create and manage job postings)
- **Related tasks:** #48, #49, #50, #51
- **Sprint:** 2

**Given** I am logged in as a recruiter on Create Posting

**Steps**
1. Fill in all fields with a deadline in the past
2. Submit

**Then** An error is shown and no posting is created

**Automated coverage:** `RecruiterControllerTests.creatingAPostingRequiresAFutureDeadline`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-19: Recruiter closes and reopens their own posting only

- **User story:** #8 (Create and manage job postings)
- **Related tasks:** #48, #49, #50, #51
- **Sprint:** 2

**Given** Recruiter A has an open posting; recruiter B has another

**Steps**
1. As A, close the posting
2. Check job search as a job seeker
3. As A, reopen it
4. Try to close B's posting as A

**Then** A closed posting disappears from search and accepts no applications, reappears when reopened; A cannot close B's posting

**Automated coverage:** `RecruiterControllerTests.closedPostingStopsNewApplicationsAndCanBeReopened, recruitersCannotCloseAnotherRecruitersPosting`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #8 Create and manage job postings (Sprint 3)

### AT-30: Recruiter edits and archives an owned posting

- **User story:** #8 (Create and manage job postings)
- **Related tasks:** #48, #49, #50, #51
- **Sprint:** 3

**Given** Recruiter A owns an active posting and Recruiter B owns a separate active posting

**Steps**
1. Log in as Recruiter A and open My Postings
2. Edit posting A's details and save
3. Reopen posting A and verify the updated details
4. Archive or deactivate posting A
5. Open job search as a job seeker
6. Log in as Recruiter A and try to edit posting B

**Then** Posting A retains its updated details, is archived or deactivated, and is no longer shown as an active job to job seekers; Recruiter A cannot edit posting B and receives a clear authorization error

**Automated coverage:** `Pending: no automated test method currently exists`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #9 View applicants for each posting (Sprint 2)

### AT-20: Recruiter reviews applicants and their resumes

- **User story:** #9 (View applicants for each posting)
- **Related tasks:** #61
- **Sprint:** 2

**Given** I am a recruiter and two job seekers applied to my posting

**Steps**
1. Open My Postings
2. Open the applicants for that posting
3. Open one applicant's resume

**Then** Both applicants are listed with name, application date and status, and the resume opens

**Automated coverage:** `RecruiterControllerTests.recruiterSeesWhoAppliedToTheirPosting, recruiterCanDownloadAnApplicantsResume`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-21: Recruiter moves an applicant forward through the stages

- **User story:** #9 (View applicants for each posting)
- **Related tasks:** #61
- **Sprint:** 2

**Given** An applicant to my posting has status Applied

**Steps**
1. Change the status to Interview
2. Change it to Offered
3. Try to change it back to Applied

**Then** Interview and Offered are saved; going backwards or changing a final decision is rejected with a message

**Automated coverage:** `RecruiterControllerTests.statusMovesThroughTheStagesAndTheJobSeekerSeesIt, statusCannotGoBackwards, finalDecisionsCannotBeChanged`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-22: Recruiters cannot see another recruiter's applicants

- **User story:** #9 (View applicants for each posting)
- **Related tasks:** #61
- **Sprint:** 2

**Given** Recruiter B has a posting with applicants

**Steps**
1. Log in as recruiter A
2. Try to open the applicants for B's posting

**Then** Access is denied and no applicant information is shown

**Automated coverage:** `RecruiterControllerTests.recruitersCannotSeeApplicantsForAnotherRecruitersPosting, recruitersCannotChangeApplicationsToAnotherRecruitersPosting`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #139 Log out of my account (Sprint 2)

### AT-23: Job seeker logs out and the session ends

- **User story:** #139 (Log out of my account)
- **Related tasks:** #140, #141
- **Sprint:** 2

**Given** I am logged in as a job seeker on the home page

**Steps**
1. Click Log out in the top bar
2. Use the browser Back button, or open applications.html directly

**Then** I am taken to the login page, and going back does not show my applications: I am sent to the login page again

**Automated coverage:** `AuthControllerTests.logoutEndsTheSession`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-24: Recruiter logs out from any recruiter page

- **User story:** #139 (Log out of my account)
- **Related tasks:** #140, #141
- **Sprint:** 2

**Given** I am logged in as a recruiter

**Steps**
1. Open Your Postings and click Logout
2. Log in again, open Create Posting and click Logout

**Then** Each time I am taken to the login page and my recruiter pages need a new login

**Automated coverage:** `AuthControllerTests.logoutEndsTheSession, logoutWithoutASessionStillSucceeds`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #6 Notifications and reminders (Sprint 3)

### AT-25: Job seeker sees status updates and distinct interview notifications

- **User story:** #6 (Notifications and reminders)
- **Related tasks:** #63
- **Sprint:** 3

**Given** I am logged in as a job seeker with an application owned by me

**Steps**
1. As the recruiter, change my application to a non-interview status
2. As the recruiter, advance my application to Interview
3. Open Notifications as the job seeker
4. Review the status-update and interview notifications
5. Log in as a different job seeker and open Notifications

**Then** The intended job seeker sees the job title and new status in the status-update notification, sees a distinct interview-invitation notification for the interview event, each unread notification has an unread indicator, and the other job seeker cannot see either notification

**Automated coverage:** `Pending: no automated test method currently exists`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-26: Job seeker sees deadline reminders without duplicates

- **User story:** #6 (Notifications and reminders)
- **Related tasks:** #63
- **Sprint:** 3

**Given** I am logged in as a job seeker with one saved job whose deadline is tomorrow, one saved job whose deadline has expired, and no other saved jobs with upcoming deadlines

**Steps**
1. Open Notifications
2. Review the reminder for the saved job due tomorrow
3. Refresh Notifications or request reminders again
4. Review the expired saved job
5. Review the empty upcoming-deadlines state after the tomorrow deadline is no longer upcoming

**Then** The upcoming reminder shows the job title, formatted deadline, and actionable message; requesting reminders again does not create a duplicate for the same deadline and reminder interval; the expired job is clearly indicated and does not receive an active upcoming reminder; and the no-upcoming-deadlines message is shown when applicable

**Automated coverage:** `Pending: no automated test method currently exists`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-27: Recruiter sees new-application notifications for owned postings

- **User story:** #6 (Notifications and reminders)
- **Related tasks:** #63
- **Sprint:** 3

**Given** Recruiter A owns posting A, Recruiter B owns posting B, and a job seeker applies to posting A

**Steps**
1. Log in as Recruiter A and open Notifications
2. Review the new-application notification
3. Log in as Recruiter B and open Notifications

**Then** Recruiter A sees a notification identifying posting A and the new applicant activity; Recruiter B does not see that notification; and the notification is marked for the posting owner only

**Automated coverage:** `Pending: no automated test method currently exists`

**Result:** _Not yet run (Pass / Fail, date, tester)_

### AT-28: Notifications show empty and API-error states

- **User story:** #6 (Notifications and reminders)
- **Related tasks:** #63
- **Sprint:** 3

**Given** The authenticated user has no notifications, and the notifications API can be made to return an error

**Steps**
1. Open Notifications with an authenticated account that has no notifications
2. Verify the empty state
3. Make the notifications API unavailable or return an error
4. Reload Notifications

**Then** The empty state gives a clear no-notifications message and renders no notification cards; when the API fails, a clear unavailable/error message is shown instead of a false successful result

**Automated coverage:** `Pending: no automated test method currently exists`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #33 Application analytics dashboard (Sprint 3)

### AT-29: Job seeker views isolated application analytics and empty state

- **User story:** #33 (Application analytics dashboard)
- **Related tasks:** #56
- **Sprint:** 3

**Given** Job seeker Alice has applications in Applied, Interview, Offered, and Rejected statuses, and job seeker Bob has separate applications

**Steps**
1. Log in as Alice and open the application analytics dashboard
2. Review the total application count
3. Review the count shown for Applied, Interview, Offered, and Rejected
4. Compare the dashboard counts with Alice's application records
5. Log in as a new job seeker with no applications and open the dashboard

**Then** Alice sees the correct total and per-status counts for Alice's current applications only, Bob's applications are excluded, and a clear no-applications empty state is shown for the new job seeker

**Automated coverage:** `Pending: no automated test method currently exists`

**Result:** _Not yet run (Pass / Fail, date, tester)_

## #10 AI resume feedback (Sprint 3)

### AT-31: Job seeker receives safe AI resume feedback

- **User story:** #10 (AI resume feedback)
- **Related tasks:** #60
- **Sprint:** 3

**Given** I am logged in as a job seeker with a supported readable resume, an unsupported resume file, and an unreadable or unprocessable resume file available for testing

**Steps**
1. Open the resume feedback feature
2. Request feedback for the supported readable resume
3. Observe the page while analysis is in progress
4. Wait for the analysis to finish and review the generated feedback
5. Request feedback for the unsupported file
6. Request feedback for the unreadable or unprocessable file

**Then** A loading indicator is shown while processing; successful feedback contains suggestions about resume content, organization, and clarity plus a clear advisory message that it should be reviewed before use; the unsupported file is rejected with a clear message; and the unreadable or unprocessable file shows a clear processing-error message without a false success result

**Automated coverage:** `Pending: no automated test method currently exists`

**Result:** _Not yet run (Pass / Fail, date, tester)_
