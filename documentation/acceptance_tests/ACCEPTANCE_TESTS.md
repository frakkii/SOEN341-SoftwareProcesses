# Acceptance Tests (Sprints 1 and 2)

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

