# Copilot Chat Conversation Export: Overview of audrey_frontend_manageposts branch

**User:** @MothMother
**Thread URL:** https://github.com/copilot/c/ace80c0b-0ba8-4e35-ad01-988055baf65b

## @MothMother

please direct your attention to my branch called "audrey_frontend_manageposts"

## Copilot

I checked the branch and it does exist.

- Branch: audrey_frontend_manageposts
- Repository: frakkii/SOEN341-SoftwareProcesses
- Latest commit on that branch: 4a33ca93a07bc76651f626cef4ee4a5c07053111
- Last commit message: “Merge pull request #91 from frakkii/audrey_consolidated_branch”

From the branch structure, this is a frontend-heavy branch focused on recruiter/posting management in the web app:

- src/webapp/recruiterpostings.html
- src/webapp/recruiterpostdetails.html
- src/webapp/recruiterapplicants.html
- src/webapp/createposting.html
- src/webapp/dashboard.html
- src/webapp/recruiterhome.html

Supporting frontend assets:
- src/webapp/css/app.css
- src/webapp/css/style.css
- src/webapp/js/auth.js
- src/webapp/js/feed.js
- src/webapp/js/profile.js
- src/webapp/js/shell.js

I did not find a literal file or symbol named “manageposts”; the branch appears to implement the “manage postings” functionality through the recruiter posting pages and related JS/CSS rather than a single dedicated file.

If you want, I can next:
- summarize what each posting-related page is doing,
- identify likely frontend bugs in this branch,
- or review the branch diff against main for the manage-posting feature.

## @MothMother

the code is running okay, but the database isn't receiving any of the job postings i create on the job creation page. Do you have any idea why that is?

## Copilot

Yes — the most likely issue is that the “Create posting” page is not actually wired to a real database-backed API.

From the frontend code:
- The form in createposting.html submits a POST request to /api/postings/create.
- But the postings page in recruiterpostings.html is not fetching jobs from a backend at all; it is populated by a hardcoded JavaScript array:
  const jobs = [...]
- That means the “Your Postings” page is acting like a mock/demo UI, not a live database-backed list.

So even if the form submission looks successful, there is likely no matching backend endpoint or persistence layer to save the posting. In other words, the app is probably sending a request to a route that either:
- does not exist,
- is not implemented,
- or is not connected to a repository/entity that inserts into the database.

This is why nothing shows up in the database: the create page is not actually persisting records, and the posting management page is not reading data from storage in the first place.

The clearest sign is that the recruiter postings page is hardcoded rather than loaded from data, which suggests the entire “manage postings” feature is still mostly frontend/mock behavior instead of a finished database integration.

## @MothMother

yes, i'm aware that the manage posting page is only hardcoded. However, despite this, the database itself ought to be updating with new instances of job postings, and its not. I can confirm as much because when i view the database directly in our dbms, there is no job posting data.

## Copilot

Yes — the problem is in the backend controller, not in the database itself.

The create-posting endpoint is effectively a stub. In `JobPostingController.createPosting`, it validates the user session and request, then returns a successful response, but it never actually saves anything to the database:

- it checks if the user is logged in and is a recruiter
- it validates the title
- then it does this:

```java
// Save to database here
// Example:
// JobPosting posting = new JobPosting(...);
// jobPostingRepository.save(posting);
```

and then returns:

```java
return ResponseEntity.ok(Map.of("message", "Job listing created", "userId", userId));
```

So the frontend gets a 200 OK, looks like the posting was created, but no row is ever inserted into `job_postings`. That explains why your DB remains empty.

There is also a second issue in the model: `JobPosting` has an `@Id` field without a generated ID strategy, so even if the save call were added, it would need to be handled correctly for inserts to work properly in JPA.

In short: the route is returning success without persisting the job posting, which is why nothing shows up in your database.

## @MothMother

ok, neat so how would i go about correcting these two issues?

## Copilot

Yes — there are exactly two fixes needed here.

1. The controller is never saving the posting
2. The entity ID is not configured to auto-generate, so even a save would likely fail or be incomplete

The root cause is in `src/backend/src/main/java/com/example/demo/controller/JobPostingController.java`: the method validates input and returns success, but it has a commented-out stub where the database save should be.

What to change

A. Update the controller to actually persist the record

Use the repository:

```java
package com.example.demo.controller;

import com.example.demo.model.JobPosting;
import com.example.demo.repository.JobPostingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/postings")
public class JobPostingController {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingController(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPosting(@RequestBody JobPostingRequest request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User is not logged in"));
        }

        if (!"Recruiter".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only recruiters can create job postings"));
        }

        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Job title is required"));
        }

        JobPosting posting = new JobPosting(
                userId,
                request.title().trim(),
                request.department() == null ? "" : request.department().trim(),
                request.salary() == null ? "" : request.salary().trim(),
                request.requiredSkills() == null ? "" : request.requiredSkills().trim(),
                request.description() == null ? "" : request.description().trim(),
                request.applicationDeadline()
        );

        JobPosting saved = jobPostingRepository.save(posting);

        return ResponseEntity.ok(Map.of(
                "message", "Job listing created",
                "userId", userId,
                "postId", saved.getId()
        ));
    }

    public record JobPostingRequest(
            String title,
            String department,
            String salary,
            String requiredSkills,
            String description,
            LocalDate applicationDeadline
    ) {}
}
```

This is the key missing behavior: it never saved to the DB.

B. Fix the entity ID generation

In `src/backend/src/main/java/com/example/demo/model/JobPosting.java`, change the ID field to auto-generate:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "post_id")
private Integer id;
```

So the full relevant part should look like:

```java
@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer id;

    @Column(name = "employer_id", nullable = false)
    private Integer employerId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String salary;

    @Column(name = "required_skills", columnDefinition = "TEXT", nullable = false)
    private String requiredSkills;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "application_deadline", nullable = false)
    private LocalDate applicationDeadline;

    protected JobPosting() {}

    public JobPosting(Integer employerId, String title, String department, String salary,
                      String requiredSkills, String description, LocalDate applicationDeadline) {
        this.employerId = employerId;
        this.title = title;
        this.department = department;
        this.salary = salary;
        this.requiredSkills = requiredSkills;
        this.description = description;
        this.applicationDeadline = applicationDeadline;
    }

    public Integer getId() { return id; }
    public Integer getEmployerId() { return employerId; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public String getSalary() { return salary; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getDescription() { return description; }
    public LocalDate getApplicationDeadline() { return applicationDeadline; }
}
```

C. If the database table already exists without identity generation, add a migration

If your `job_postings.post_id` column is not auto-incrementing, you need a Flyway migration like:

```sql
ALTER TABLE job_postings
    ALTER COLUMN post_id ADD GENERATED BY DEFAULT AS IDENTITY;
```

If the table is missing or malformed, create it properly with:

```sql
CREATE TABLE IF NOT EXISTS job_postings (
    post_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    employer_id INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    salary VARCHAR(255) NOT NULL,
    required_skills TEXT NOT NULL,
    description TEXT NOT NULL,
    application_deadline DATE NOT NULL
);
```

One more thing to verify

Also make sure the user is actually logged in as a Recruiter before submitting. The create form posts to `/api/postings/create`, but that endpoint only works if the session contains:
- `userId`
- `role = "Recruiter"`

If those are missing, you’ll get a 401/403 even though the frontend doesn’t show it clearly.

Best next step

Implement those two fixes and restart the backend. Then test with a recruiter account and inspect the `job_postings` table. The row should appear immediately after submission.

If you want, I can write the exact Flyway migration file and the complete corrected `JobPostingController.java` and `JobPosting.java` for your branch.

## @MothMother

it works, thank you! 

## Copilot

Great — I’m glad it’s working.

The actual root cause was exactly what we traced:
- the controller was validating input and returning success
- but it never called the repository save method
- and the entity ID was not configured for auto-generation

Once those were corrected, the backend was able to persist records into `job_postings` as expected.

If you want, I can also help with the next step:
- verifying the posting appears in the DB after submission
- loading those saved postings into the recruiter “Manage Postings” page
- or adding the edit/delete functionality for the job-posting feature.

## @MothMother

i would like help with loading those saved postings into the recruiter “Manage Postings” page

## Copilot

Absolutely — the missing piece is that the recruiter page still uses a static JavaScript array instead of fetching the real postings from the backend.

Right now the flow is:
- create page posts to /api/postings/create
- backend saves to database
- recruiter manage page ignores the database and renders a hardcoded jobs list

To fix this, you need:
- a backend GET endpoint that returns the recruiter’s postings
- the frontend page to call that endpoint and render the results instead of the mocked jobs array

The key thing to note is that your DB schema does not currently have `location` or `applicants` fields. So the cleanest approach is:
- keep the DB model as it is
- return `title`, `department`, `salary`, `requiredSkills`, `description`, `applicationDeadline`
- in the UI, map missing values to placeholders like:
  - location = "Not specified"
  - applicants = 0
  - status = "Open"

1) Add a GET endpoint in the backend

In `src/backend/src/main/java/com/example/demo/controller/JobPostingController.java`, add this:

```java name=JobPostingController.java
package com.example.demo.controller;

import com.example.demo.model.JobPosting;
import com.example.demo.repository.JobPostingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/postings")
public class JobPostingController {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingController(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPosting(@RequestBody JobPostingRequest request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "User is not logged in"));
        }

        if (!"Recruiter".equals(role)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only recruiters can create job postings"));
        }

        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Job title is required"));
        }

        JobPosting posting = new JobPosting(
                userId,
                request.title().trim(),
                request.department() == null ? "" : request.department().trim(),
                request.salary() == null ? "" : request.salary().trim(),
                request.requiredSkills() == null ? "" : request.requiredSkills().trim(),
                request.description() == null ? "" : request.description().trim(),
                request.applicationDeadline()
        );

        JobPosting saved = jobPostingRepository.save(posting);

        return ResponseEntity.ok(Map.of(
                "message", "Job listing created",
                "userId", userId,
                "postId", saved.getId()
        ));
    }

    @GetMapping
    public ResponseEntity<List<JobPostingResponse>> getMyPostings(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (!"Recruiter".equals(role)) {
            return ResponseEntity.status(403).build();
        }

        List<JobPostingResponse> postings = jobPostingRepository
                .findByEmployerId(userId)
                .stream()
                .map(posting -> new JobPostingResponse(
                        posting.getId(),
                        posting.getTitle(),
                        posting.getDepartment(),
                        posting.getSalary(),
                        posting.getRequiredSkills(),
                        posting.getDescription(),
                        posting.getApplicationDeadline()
                ))
                .toList();

        return ResponseEntity.ok(postings);
    }

    public record JobPostingRequest(
            String title,
            String department,
            String salary,
            String requiredSkills,
            String description,
            LocalDate applicationDeadline
    ) {}

    public record JobPostingResponse(
            Integer id,
            String title,
            String department,
            String salary,
            String requiredSkills,
            String description,
            LocalDate applicationDeadline
    ) {}
}
```

Then add this to the repository:

```java name=JobPostingRepository.java
package com.example.demo.repository;

import com.example.demo.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Integer> {
    List<JobPosting> findByEmployerId(Integer employerId);
}
```

2) Replace the hardcoded jobs array in the recruiter page

In `src/webapp/recruiterpostings.html`, replace the static array and render logic with a fetch call.

Replace this:

```javascript
const jobs = [
  { title: "Senior Frontend Engineer", department: "Engineering", location: "Montreal, QC", applicants: 18, status: "Open" },
  ...
];
```

with this:

```javascript
let jobs = [];

async function loadPostings() {
  try {
    const response = await fetch("/api/postings", {
      method: "GET",
      credentials: "same-origin"
    });

    if (!response.ok) {
      throw new Error("Unable to load postings");
    }

    const postings = await response.json();

    jobs = postings.map(posting => ({
      id: posting.id,
      title: posting.title || "Untitled",
      department: posting.department || "Not specified",
      location: "Not specified",
      applicants: 0,
      status: "Open",
      deadline: posting.applicationDeadline
    }));

    renderTable();
  } catch (error) {
    console.error(error);
    rowsEl.innerHTML = `
      <tr>
        <td colspan="6">Unable to load postings.</td>
      </tr>
    `;
  }
}
```

Then update `renderTable()` to use `jobs` from the API instead of the hardcoded list. For example:

```javascript
function renderTable() {
  const query = searchInput.value.trim().toLowerCase();
  const filtered = jobs.filter(job =>
    [job.title, job.department, job.location, job.status]
      .join(" ")
      .toLowerCase()
      .includes(query)
  );

  filtered.sort((a, b) => {
    const aValue = a[currentSort.key];
    const bValue = b[currentSort.key];

    if (typeof aValue === "string") {
      return aValue.localeCompare(bValue) * (currentSort.direction === "asc" ? 1 : -1);
    }

    return (Number(aValue) - Number(bValue)) * (currentSort.direction === "asc" ? 1 : -1);
  });

  rowsEl.innerHTML = filtered.map(job => `
    <tr>
      <td><strong>${job.title}</strong></td>
      <td>${job.department}</td>
      <td>${job.location}</td>
      <td>${job.applicants}</td>
      <td>${statusMarkup(job.status)}</td>
      <td>
        <div class="row-actions">
          <button class="btn">View</button>
          <button class="btn">Edit</button>
          <button class="btn">Close</button>
        </div>
      </td>
    </tr>
  `).join("");
}
```

And at the end, replace:

```javascript
renderTable();
```

with:

```javascript
loadPostings();
```

3) One more important note

Right now your UI expects these columns:
- title
- department
- location
- applicants
- status

But your DB model stores:
- title
- department
- salary
- requiredSkills
- description
- applicationDeadline

So the page is not aligned with the database model.

The simplest immediate fix is to leave the working page structure and just populate:
- location = "Not specified"
- applicants = 0
- status = "Open"

Then later, once the schema is expanded, you can add real location and applicant counters.

This is the cleanest “get it working now” path.

If you want, I can give you the full corrected `recruiterpostings.html` file for this exact flow, so you can paste it in directly.

## @MothMother

okay, so i implemented your suggested changes. thank you, the hardcoded jobs list is now devoid of those entries. Thing is, none of the jobs in the database table are appearing, and i'm not sure why

## Copilot

Yes — the backend save is fixed, but the page still isn’t rendering the DB rows because there are two frontend bugs in the recruiter postings page.

The most important ones are here:

- `const jobs = [];`
- then later: `jobs = postings.map(...)`
- and inside the same function: `loadPostings();` again

That means the page is doing all of the following:

1. Declaring `jobs` as a constant
2. Trying to reassign it
3. Recursing into `loadPostings()` endlessly

So the browser is likely throwing a runtime error before it can render anything, and the page never reaches the actual list display.

This is the exact problem in your current file:

```js
const jobs = [];

async function loadPostings() {
  try {
    ...
    jobs = postings.map(posting => ({
      ...
    }));

    loadPostings();   // <-- this is a recursive call
  } catch (error) {
    ...
  }
}
```

What it should be:

```js
let jobs = [];

async function loadPostings() {
  try {
    const response = await fetch("/api/postings", {
      method: "GET",
      credentials: "same-origin"
    });

    if (!response.ok) {
      throw new Error(`Request failed with status ${response.status}`);
    }

    const postings = await response.json();

    jobs = postings.map(posting => ({
      id: posting.id,
      title: posting.title || "Untitled",
      department: posting.department || "Not specified",
      location: "Not specified",
      applicants: 0,
      status: "Open"
    }));

    renderTable();
  } catch (error) {
    console.error("loadPostings error:", error);
    rowsEl.innerHTML = `<tr><td colspan="6">Unable to load postings.</td></tr>`;
  }
}
```

Then, at the end, call:

```js
loadPostings();
```

not `loadPostings()` inside the function itself.

Also, if the fetch still returns nothing, there are two other things to check:

- The recruiter must still be logged in as a Recruiter, otherwise `/api/postings` returns 401/403
- The endpoint filters by `employer_id`, so if you are logged in as a different recruiter than the one who created the posting, you won’t see those rows

So the real logic is:
- create posting saves with employerId = current session user
- GET /api/postings fetches only postings for that logged-in employer

If you want, I can rewrite the full `recruiterpostings.html` file for you so it’s correct end-to-end and will fetch and display the database rows properly.
