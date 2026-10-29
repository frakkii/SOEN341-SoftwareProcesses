# Creates the Sprint 1 and Sprint 2 acceptance tests as GitHub issues, labels them
# "acceptance-test", and links each one to its user story as a sub-issue.
#
# Needs the GitHub CLI, signed in:   winget install GitHub.cli   then   gh auth login
#
#   .\create-acceptance-test-issues.ps1             create the issues
#   .\create-acceptance-test-issues.ps1 -Markdown   only rewrite ACCEPTANCE_TESTS.md
#   .\create-acceptance-test-issues.ps1 -Only AT-11 create just the listed tests
param([switch]$Markdown, [string[]]$Only)

$Repo = 'frakkii/SOEN341-SoftwareProcesses'
$Label = 'acceptance-test'

# Story -> the task issues that implement it
$Stories = @{
    1  = @{ Sprint = 1; Title = 'Create an account and log in';           Tasks = 12, 13, 14, 15, 16 }
    2  = @{ Sprint = 2; Title = 'Upload and update my resume';            Tasks = 36, 37, 38, 39 }
    32 = @{ Sprint = 2; Title = 'Store multiple versions of my resume';   Tasks = 57 }
    69 = @{ Sprint = 2; Title = 'Create and edit a professional profile'; Tasks = 70, 71, 72, 73 }
    3  = @{ Sprint = 2; Title = 'Search and filter job postings';         Tasks = 64, 65, 66 }
    4  = @{ Sprint = 2; Title = 'Submit applications on the platform';    Tasks = 40, 41, 42, 43 }
    5  = @{ Sprint = 2; Title = 'Track the status of my applications';    Tasks = 44, 45, 46, 47 }
    8  = @{ Sprint = 2; Title = 'Create and manage job postings';         Tasks = 48, 49, 50, 51 }
    9  = @{ Sprint = 2; Title = 'View applicants for each posting';       Tasks = 61 }
}

$Tests = @(
    # ---- #1 Account creation and login (Sprint 1) ----
    @{ Id = 'AT-01'; Story = 1; Title = 'New job seeker registers and logs in'
       Given = 'I am on the login page (index.html) and have no account'
       Steps = 'Switch to Register', 'Fill in every required field with valid values and choose Job Seeker', 'Submit', 'Log in with the same email and password'
       Then  = 'A success message is shown, the account is created, and logging in takes me to the homepage with my name displayed'
       Auto  = 'AuthControllerTests.registerThenLoginReturnsUserDetails' }
    @{ Id = 'AT-02'; Story = 1; Title = 'Registering with an email that is already used is rejected'
       Given = 'An account already exists for test@example.com'
       Steps = 'Open the Register form', 'Enter valid details using test@example.com', 'Submit'
       Then  = 'An error says the email is already in use and no second account is created'
       Auto  = 'AuthControllerTests.registerRejectsDuplicateEmail, registerRejectsEmailAlreadyUsedByOtherAccountType' }
    @{ Id = 'AT-03'; Story = 1; Title = 'Invalid registration fields and wrong passwords show errors'
       Given = 'I am on the login page'
       Steps = 'Try to register with a blank name and an invalid email', 'Try to log in to an existing account with the wrong password'
       Then  = 'Each attempt shows an error message naming the problem, and I stay on the login page'
       Auto  = 'AuthControllerTests.registerListsMissingFields, registerRejectsInvalidEmail, loginRejectsWrongPassword' }

    # ---- #2 Resume upload and update ----
    @{ Id = 'AT-04'; Story = 2; Title = 'Job seeker uploads a PDF resume'
       Given = 'I am logged in as a job seeker with no resume'
       Steps = 'Open the resume page', 'Choose a PDF file under 5 MB', 'Upload it'
       Then  = 'A confirmation is shown and my resume can be opened from the resume page'
       Auto  = 'ResumeControllerTests.jobSeekerCanUploadAndDownloadResume' }
    @{ Id = 'AT-05'; Story = 2; Title = 'Unsupported or empty resume files are rejected'
       Given = 'I am logged in as a job seeker'
       Steps = 'Try to upload a .exe file', 'Try to upload an empty file'
       Then  = 'Each upload shows an error message and my current resume is unchanged'
       Auto  = 'ResumeControllerTests.uploadRejectsOtherFileTypes, uploadRejectsEmptyFile' }

    # ---- #32 Multiple resume versions ----
    @{ Id = 'AT-06'; Story = 32; Title = 'Uploading a new resume keeps the previous version'
       Given = 'I am logged in as a job seeker and already uploaded resume A'
       Steps = 'Upload resume B from the resume page'
       Then  = 'Resume B becomes my current resume and resume A is still stored as an earlier version'
       Auto  = 'ResumeControllerTests.uploadingAgainReplacesTheCurrentResumeAndKeepsTheOldVersion' }
    @{ Id = 'AT-07'; Story = 32; Title = 'Job seekers can only see their own resumes'
       Given = 'Job seekers Alice and Bob have each uploaded a resume'
       Steps = 'Log in as Bob', 'Open the resume page'
       Then  = "Only Bob's resume is shown; Alice's resume cannot be opened"
       Auto  = 'ResumeControllerTests.jobSeekersOnlySeeTheirOwnResume' }

    # ---- #69 Professional profile ----
    @{ Id = 'AT-08'; Story = 69; Title = 'Job seeker edits their profile and it is saved'
       Given = 'I am logged in as a job seeker'
       Steps = 'Open the profile page', 'Fill in headline, About and Skills', 'Save', 'Log out, log back in and reopen the profile page'
       Then  = 'A confirmation is shown and the saved headline, About and Skills are still there'
       Auto  = 'ProfileControllerTests.jobSeekerCanEditTheirProfileAndItIsSaved' }
    @{ Id = 'AT-09'; Story = 69; Title = 'Invalid profile input is rejected and the profile needs a job seeker'
       Given = 'I am logged in as a job seeker'
       Steps = 'Enter a headline longer than the allowed length and save', 'Log in as a recruiter and open the profile page'
       Then  = 'The long headline shows an error and is not saved; the recruiter cannot open a job seeker profile'
       Auto  = 'ProfileControllerTests.rejectsFieldsThatAreTooLongOrInvalid, profileRequiresAJobSeeker' }

    # ---- #3 Job search and filtering ----
    @{ Id = 'AT-10'; Story = 3; Title = 'Keyword search and filters return matching open jobs'
       Given = 'There are open postings in different locations and job types, plus one closed posting'
       Steps = 'Open the jobs page', 'Search a keyword from a job title or company', 'Add a location and job type filter'
       Then  = 'Only open, unexpired postings matching all criteria are listed, each with title, company, location and job type'
       Auto  = 'JobPostingControllerTests.listsOnlyActiveAndUnexpiredJobsWithCompanyName, appliesKeywordAndAllFiltersTogether' }
    @{ Id = 'AT-11'; Story = 3; Title = 'No matches shows a message; a job opens its details page'
       Given = 'There are open postings'
       Steps = 'Search for a keyword that matches nothing', 'Clear the search and click a posting'
       Then  = 'A "no jobs found" message is shown; clicking a posting opens job.html with its full details'
       Auto  = 'JobPostingControllerTests.returnsEmptyArrayForNoMatchesAndNotFoundForMissingOrUnavailableDetails, showsTheDetailsOfAnOpenJob' }

    # ---- #4 Application submission ----
    @{ Id = 'AT-12'; Story = 4; Title = 'Job seeker applies to an open job'
       Given = 'I am logged in as a job seeker with an uploaded resume, viewing an open posting'
       Steps = 'Click Apply', 'Confirm the application'
       Then  = 'A confirmation is shown and the application appears on my applications page with status Applied'
       Auto  = 'ApplicationControllerTests.jobSeekerCanApplyAndGetsConfirmation, ApplicationTest.newApplicationStartsAsApplied' }
    @{ Id = 'AT-13'; Story = 4; Title = 'Invalid applications are blocked with a message'
       Given = 'I am logged in as a job seeker'
       Steps = 'Apply to the same job a second time', 'Apply to a closed or expired posting', 'Apply without having uploaded a resume'
       Then  = 'Each attempt shows an error explaining why and no application is created'
       Auto  = 'ApplicationControllerTests.cannotApplyTwiceToTheSameJob, applyRejectsClosedPosting, applyRejectsPostingPastItsDeadline, applyRequiresAnUploadedResume' }

    # ---- #5 Application tracking ----
    @{ Id = 'AT-14'; Story = 5; Title = 'Job seeker sees only their own applications'
       Given = 'Alice has applied to two jobs and Bob to one'
       Steps = 'Log in as Alice', 'Open the applications page'
       Then  = "Alice's two applications are listed with job title, company, submission date and status; Bob's is not shown"
       Auto  = 'ApplicationControllerTests.jobSeekerSeesAllTheirApplications, jobSeekersOnlySeeTheirOwnApplications' }
    @{ Id = 'AT-15'; Story = 5; Title = 'Status changes made by the recruiter show on the tracker'
       Given = 'I applied to a job and the recruiter moved my application to Interview'
       Steps = 'Open the applications page'
       Then  = 'The application shows status Interview'
       Auto  = 'ApplicationControllerTests.jobSeekerSeesTheCurrentStatus, RecruiterControllerTests.statusMovesThroughTheStagesAndTheJobSeekerSeesIt' }
    @{ Id = 'AT-16'; Story = 5; Title = 'A new job seeker sees an empty-state message'
       Given = 'I am logged in as a job seeker who has never applied'
       Steps = 'Open the applications page'
       Then  = 'A message says I have no applications yet'
       Auto  = 'ApplicationControllerTests.newJobSeekerHasNoApplications' }

    # ---- #8 Job posting management ----
    @{ Id = 'AT-17'; Story = 8; Title = 'Recruiter creates a job posting'
       Given = 'I am logged in as a recruiter'
       Steps = 'Open Create Posting', 'Fill in title, department, salary, skills, description and a future deadline', 'Submit'
       Then  = 'A success message is shown, the posting appears in my postings, and job seekers can find it in job search'
       Auto  = 'RecruiterControllerTests.recruiterSeesTheirPostingsWithApplicantCounts' }
    @{ Id = 'AT-18'; Story = 8; Title = 'Posting with a past deadline is rejected'
       Given = 'I am logged in as a recruiter on Create Posting'
       Steps = 'Fill in all fields with a deadline in the past', 'Submit'
       Then  = 'An error is shown and no posting is created'
       Auto  = 'RecruiterControllerTests.creatingAPostingRequiresAFutureDeadline' }
    @{ Id = 'AT-19'; Story = 8; Title = 'Recruiter closes and reopens their own posting only'
       Given = 'Recruiter A has an open posting; recruiter B has another'
       Steps = 'As A, close the posting', 'Check job search as a job seeker', 'As A, reopen it', 'Try to close B''s posting as A'
       Then  = 'A closed posting disappears from search and accepts no applications, reappears when reopened; A cannot close B''s posting'
       Auto  = 'RecruiterControllerTests.closedPostingStopsNewApplicationsAndCanBeReopened, recruitersCannotCloseAnotherRecruitersPosting' }

    # ---- #9 Recruiter applicant list ----
    @{ Id = 'AT-20'; Story = 9; Title = 'Recruiter reviews applicants and their resumes'
       Given = 'I am a recruiter and two job seekers applied to my posting'
       Steps = 'Open My Postings', 'Open the applicants for that posting', 'Open one applicant''s resume'
       Then  = 'Both applicants are listed with name, application date and status, and the resume opens'
       Auto  = 'RecruiterControllerTests.recruiterSeesWhoAppliedToTheirPosting, recruiterCanDownloadAnApplicantsResume' }
    @{ Id = 'AT-21'; Story = 9; Title = 'Recruiter moves an applicant forward through the stages'
       Given = 'An applicant to my posting has status Applied'
       Steps = 'Change the status to Interview', 'Change it to Offered', 'Try to change it back to Applied'
       Then  = 'Interview and Offered are saved; going backwards or changing a final decision is rejected with a message'
       Auto  = 'RecruiterControllerTests.statusMovesThroughTheStagesAndTheJobSeekerSeesIt, statusCannotGoBackwards, finalDecisionsCannotBeChanged' }
    @{ Id = 'AT-22'; Story = 9; Title = "Recruiters cannot see another recruiter's applicants"
       Given = 'Recruiter B has a posting with applicants'
       Steps = 'Log in as recruiter A', 'Try to open the applicants for B''s posting'
       Then  = 'Access is denied and no applicant information is shown'
       Auto  = 'RecruiterControllerTests.recruitersCannotSeeApplicantsForAnotherRecruitersPosting, recruitersCannotChangeApplicationsToAnotherRecruitersPosting' }
)

function Get-Body($t) {
    $s = $Stories[$t.Story]
    $steps = for ($i = 0; $i -lt $t.Steps.Count; $i++) { "$($i + 1). $($t.Steps[$i])" }
    @"
- **User story:** #$($t.Story) ($($s.Title))
- **Related tasks:** $(($s.Tasks | ForEach-Object { "#$_" }) -join ', ')
- **Sprint:** $($s.Sprint)

**Given** $($t.Given)

**Steps**
$($steps -join "`n")

**Then** $($t.Then)

**Automated coverage:** ``$($t.Auto)``

**Result:** _Not yet run (Pass / Fail, date, tester)_
"@
}

# Markdown copy for the repo
$md = @('# Acceptance Tests (Sprints 1 and 2)', '',
        'Each test is also a GitHub issue labeled `acceptance-test` and linked to its user story as a sub-issue.',
        'Regenerate this file with `.\create-acceptance-test-issues.ps1 -Markdown`.', '')
foreach ($n in 1, 2, 32, 69, 3, 4, 5, 8, 9) {
    $md += "## #$n $($Stories[$n].Title) (Sprint $($Stories[$n].Sprint))", ''
    foreach ($t in $Tests | Where-Object { $_.Story -eq $n }) {
        $md += "### $($t.Id): $($t.Title)", '', (Get-Body $t), ''
    }
}
$md -join "`n" | Out-File -Encoding utf8 (Join-Path $PSScriptRoot 'ACCEPTANCE_TESTS.md')
if ($Markdown) { return }

gh label create $Label --repo $Repo --color 0E8A16 --description 'Acceptance test for a user story' --force
$bodyFile = New-TemporaryFile
foreach ($t in $Tests | Where-Object { -not $Only -or $Only -contains $_.Id }) {
    $title = "$($t.Id): $($t.Title)"
    # Pass the body as a file: Windows PowerShell breaks double quotes in native arguments
    [IO.File]::WriteAllText($bodyFile, (Get-Body $t))
    $url = gh issue create --repo $Repo --title $title --label $Label --body-file $bodyFile
    if (-not $url) { Write-Error "Failed to create $title"; continue }
    # Sub-issue links take the issue's database id, not its number
    $number = ($url -split '/')[-1]
    $id = gh api "repos/$Repo/issues/$number" --jq .id
    gh api -X POST "repos/$Repo/issues/$($t.Story)/sub_issues" -F sub_issue_id=$id | Out-Null
    Write-Host "$title -> $url (linked to #$($t.Story))"
}
