

## prompts and responces
### 1. Initial request

**Prompt**

> i added the files NotificationConfig.java and NotificationController.java to my project. Whta should i add to make my existing code to make a proper backend for notifications and reminders knowing the following changes were implemenmt by a teammates working on the frontend "- Added a Notifications page for job seekers and recruiters.
> - Added role-based notification filtering and navigation.
> - Added unread indicators and mark-as-read functionality.
> - Added empty and error states.
> - Added a temporary demo mode for frontend testing.
> - Documented the expected backend API contract."

**Response**

Your current code is a **WebSocket broadcast prototype**, not a complete notification backend. [`NotificationController.java`](../src/backend/src/main/java/com/example/demo/controller/NotificationController.java) sends a message to everyone subscribed to `/topic/notifications`; it doesn’t save notifications, identify the recipient, or support unread/mark-as-read. The frontend’s notification page and documented API contract aren’t present in the workspace snapshot I inspected, so confirm endpoint names and JSON fields against your teammate’s contract.

I’d build this in order:

1. **Persist notifications.** Add a `Notification` entity, repository, and Flyway migration. Store a title/message, type, link or related entity ID, creation time, and read time (or `is_read`). Since job-seeker and recruiter IDs may overlap, identify the recipient by role and ID—or use separate nullable foreign keys to each user table. Add a deduplication key for generated reminders.
2. **Add authenticated REST endpoints.** For example:
   - `GET /api/notifications` — current user’s notifications, newest first; optionally filter to unread.
   - `PATCH /api/notifications/{id}/read` — mark one as read.
   - `GET /api/notifications/unread-count` — if the frontend needs a badge count.

   Resolve the user ID and role from the login session, as [`ApplicationController.java`](../src/backend/src/main/java/com/example/demo/controller/ApplicationController.java) and [`RecruiterController.java`](../src/backend/src/main/java/com/example/demo/controller/RecruiterController.java) do. Never accept a recipient ID from the request; make sure users can only read or update their own notifications.
3. **Create notifications from real events.** For example, when a recruiter changes an application status, notify that job seeker; when an application arrives, notify the posting’s recruiter. The status-change flow is in [`RecruiterService.java`](../src/backend/src/main/java/com/example/demo/service/RecruiterService.java).
4. **Implement reminders separately from event notifications.** A scheduled job can find approaching deadlines and create reminders, with a unique key so the same reminder isn’t created on every run. Your current models include job-posting deadlines, but I didn’t find interview or follow-up due dates; those reminders need corresponding data fields or records first.
5. **Keep WebSockets optional.** Use them only for live updates after saving the notification. Don’t broadcast private notifications on a shared topic. Add `package com.example.demo.config;` to [`NotificationConfig.java`](../src/backend/src/main/java/com/example/demo/config/NotificationConfig.java), and configure the actual frontend origin (without a trailing slash).

Also add tests for role/ownership isolation, unread filtering, mark-as-read, reminder deduplication, and the notification events you support. The REST endpoints are the essential part for the page’s list and read-state behavior; WebSockets can come later.

### 2. Request to generate the backend code

**Prompt**

> can you help me by generating the code for such functions

**Response**

Implemented the notification backend.

- Added database-backed notifications and a Flyway migration in [`V11__create_notifications.sql`](../src/backend/src/main/resources/db/migration/common/V11__create_notifications.sql).
- Added session-scoped endpoints in [`NotificationController.java`](../src/backend/src/main/java/com/example/demo/controller/NotificationController.java):
  - `GET /api/notifications` — list notifications; add `?unreadOnly=true` to filter.
  - `GET /api/notifications/unread-count` — returns `{"count": 2}`.
  - `PATCH /api/notifications/{id}/read` — mark one as read.
  - `PATCH /api/notifications/read-all` — mark all as read.
- Notifications are created when a recruiter receives an application and when an application’s status changes. Access is scoped to the logged-in user’s role and ID.
- Added a daily deadline reminder job for job seekers’ saved postings that are open and close within three days. Reminders are deduplicated.

The frontend API contract wasn’t present in the repository, so confirm these routes and response fields (`id`, `title`, `message`, `type`, `link`, `createdAt`, `read`, `readAt`) match its expectations. Interview and follow-up reminders still need stored interview or follow-up dates; those aren’t modeled in the current backend.

Validation passed: **all 86 backend tests** with `.\mvnw.cmd test`.

### 3. Request for a step-by-step explanation

**Prompt**

> please go over the changes setp by step

**Response**

Here’s how the changes fit together, from database to frontend:

1. **Create somewhere to store notifications.**  
   [`V11__create_notifications.sql`](../src/backend/src/main/resources/db/migration/common/V11__create_notifications.sql) creates the `notification` table. Each row stores its recipient’s role and ID, title, message, type, optional navigation link, creation time, read time, and an optional deduplication key. That role-plus-ID combination matters because a recruiter and a job seeker could have the same numeric user ID.

2. **Represent each row in Java.**  
   [`Notification.java`](../src/backend/src/main/java/com/example/demo/model/Notification.java) maps to that table. `readAt` indicates whether a notification has been read; `markRead()` sets it once, so marking it read again doesn’t replace the original timestamp.

3. **Query and save notifications.**  
   [`NotificationRepository.java`](../src/backend/src/main/java/com/example/demo/repository/NotificationRepository.java) provides database queries for a user’s notifications, unread notifications, unread count, and a notification belonging to a specific user.

4. **Apply the notification behavior.**  
   [`NotificationService.java`](../src/backend/src/main/java/com/example/demo/service/NotificationService.java) uses those queries to list notifications, count unread ones, mark one or all as read, and create new ones. Its response record exposes fields for the frontend, including `read`, so the UI can display unread indicators.

5. **Expose the REST API.**  
   [`NotificationController.java`](../src/backend/src/main/java/com/example/demo/controller/NotificationController.java) uses the existing login session to determine the caller. The frontend doesn’t send a recipient ID, and the backend scopes notification reads and updates to that session.

   | Request | What it does |
   |---|---|
   | `GET /api/notifications` | List the signed-in user’s notifications |
   | `GET /api/notifications?unreadOnly=true` | List only unread notifications |
   | `GET /api/notifications/unread-count` | Return the unread count |
   | `PATCH /api/notifications/{id}/read` | Mark one notification as read |
   | `PATCH /api/notifications/read-all` | Mark all of the user’s notifications as read |

6. **Create notifications when existing actions happen.**  
   [`ApplicationService.java`](../src/backend/src/main/java/com/example/demo/service/ApplicationService.java) creates a notification for the recruiter when a job seeker applies. [`RecruiterService.java`](../src/backend/src/main/java/com/example/demo/service/RecruiterService.java) creates one for the job seeker when the recruiter updates their application status.

7. **Generate deadline reminders.**  
   [`DeadlineReminderService.java`](../src/backend/src/main/java/com/example/demo/service/DeadlineReminderService.java) runs daily at 9 a.m. and checks saved, open postings whose deadlines fall within the next three days. It uses a deduplication key based on the user, posting, and deadline, preventing the daily run from repeatedly creating the same reminder. The query for matching saved jobs is in [`JobPostingRepository.java`](../src/backend/src/main/java/com/example/demo/repository/JobPostingRepository.java), and [`DemoApplication.java`](../src/backend/src/main/java/com/example/demo/DemoApplication.java) enables scheduled tasks.

8. **Verify the behavior with tests.**  
   [`NotificationControllerTests.java`](../src/backend/src/test/java/com/example/demo/controller/NotificationControllerTests.java) checks listing, unread filtering, marking read, requiring login, preventing access to another user’s notification, and deduplicating deadline reminders. The full backend suite passed: **86 tests, 0 failures**.

One important distinction: the backend currently provides REST notifications that the page fetches; it does **not** push live updates over WebSockets. The WebSocket setup remains in [`NotificationConfig.java`](../src/backend/src/main/java/com/example/demo/config/NotificationConfig.java), but the current notification controller no longer broadcasts messages. Also, the reminders cover saved job deadlines only; interview and follow-up reminders need those dates represented in the backend first.

### 4. Request to put the discussion into Markdown

**Prompt**

> can you rewrite every responses you gave me into an md file

**Response**

Created [`Notifications_Backend_Implementation.md`](Notifications_Backend_Implementation.md), combining the three earlier answers: initial recommendations, implementation summary, and step-by-step walkthrough.

### 5. Request to include prompts, responses, and code

**Prompt**

> please rewrite the file. include the prompt and then the responces as well as the code you generated

**Response**

This revised document includes the prompts and responses above, followed by the generated implementation code below.

## Generated code

### `NotificationController.java`

```java
package com.example.demo.controller;

import com.example.demo.exception.ApplicationException;
import com.example.demo.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<NotificationService.NotificationResponse> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly, HttpSession session) {
        Recipient recipient = recipient(session);
        return notifications.list(recipient.role(), recipient.id(), unreadOnly);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(HttpSession session) {
        Recipient recipient = recipient(session);
        return Map.of("count", notifications.unreadCount(recipient.role(), recipient.id()));
    }

    @PatchMapping("/{id}/read")
    public NotificationService.NotificationResponse markRead(@PathVariable Long id, HttpSession session) {
        Recipient recipient = recipient(session);
        return notifications.markRead(recipient.role(), recipient.id(), id);
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllRead(HttpSession session) {
        Recipient recipient = recipient(session);
        return Map.of("updated", notifications.markAllRead(recipient.role(), recipient.id()));
    }

    private static Recipient recipient(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Please sign in first.");
        }

        String role = (String) session.getAttribute(AuthController.SESSION_ROLE);
        if (!"Job Seeker".equals(role) && !"Recruiter".equals(role)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Unsupported account role.");
        }
        return new Recipient(role, userId);
    }

    private record Recipient(String role, Integer id) {
    }
}
```

### `Notification.java`

```java
package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_role", nullable = false, length = 20)
    private String recipientRole;

    @Column(name = "recipient_id", nullable = false)
    private Integer recipientId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(length = 500)
    private String link;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "deduplication_key", unique = true, length = 255)
    private String deduplicationKey;

    protected Notification() {
    }

    public Notification(String recipientRole, Integer recipientId, String title, String message,
                        String type, String link, String deduplicationKey) {
        this.recipientRole = recipientRole;
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.link = link;
        this.deduplicationKey = deduplicationKey;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public Integer getRecipientId() {
        return recipientId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getLink() {
        return link;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markRead() {
        if (readAt == null) {
            readAt = Instant.now();
        }
    }
}
```

### `NotificationRepository.java`

```java
package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientRoleAndRecipientIdOrderByCreatedAtDesc(
            String recipientRole, Integer recipientId);

    List<Notification> findByRecipientRoleAndRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(
            String recipientRole, Integer recipientId);

    Optional<Notification> findByIdAndRecipientRoleAndRecipientId(
            Long id, String recipientRole, Integer recipientId);

    long countByRecipientRoleAndRecipientIdAndReadAtIsNull(String recipientRole, Integer recipientId);

    Optional<Notification> findByDeduplicationKey(String deduplicationKey);
}
```

### `NotificationService.java`

```java
package com.example.demo.service;

import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(String role, Integer userId, boolean unreadOnly) {
        List<Notification> records = unreadOnly
                ? notifications.findByRecipientRoleAndRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(role, userId)
                : notifications.findByRecipientRoleAndRecipientIdOrderByCreatedAtDesc(role, userId);
        return records.stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String role, Integer userId) {
        return notifications.countByRecipientRoleAndRecipientIdAndReadAtIsNull(role, userId);
    }

    @Transactional
    public NotificationResponse markRead(String role, Integer userId, Long notificationId) {
        Notification notification = notifications.findByIdAndRecipientRoleAndRecipientId(
                        notificationId, role, userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Notification not found."));
        notification.markRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public int markAllRead(String role, Integer userId) {
        List<Notification> unread = notifications
                .findByRecipientRoleAndRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(role, userId);
        unread.forEach(Notification::markRead);
        notifications.saveAll(unread);
        return unread.size();
    }

    public Notification create(String role, Integer userId, String title, String message,
                               String type, String link, String deduplicationKey) {
        if (deduplicationKey != null) {
            Notification existing = notifications.findByDeduplicationKey(deduplicationKey).orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        try {
            return notifications.saveAndFlush(new Notification(
                    role, userId, title, message, type, link, deduplicationKey));
        } catch (DataIntegrityViolationException exception) {
            if (deduplicationKey != null) {
                return notifications.findByDeduplicationKey(deduplicationKey).orElseThrow(() -> exception);
            }
            throw exception;
        }
    }

    public record NotificationResponse(
            Long id,
            String title,
            String message,
            String type,
            String link,
            Instant createdAt,
            boolean read,
            Instant readAt
    ) {
        private static NotificationResponse from(Notification notification) {
            return new NotificationResponse(
                    notification.getId(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getType(),
                    notification.getLink(),
                    notification.getCreatedAt(),
                    notification.isRead(),
                    notification.getReadAt());
        }
    }
}
```

### `DeadlineReminderService.java`

```java
package com.example.demo.service;

import com.example.demo.repository.JobPostingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DeadlineReminderService {

    private static final String JOB_SEEKER = "Job Seeker";
    private static final int REMINDER_WINDOW_DAYS = 3;

    private final JobPostingRepository postings;
    private final NotificationService notifications;

    public DeadlineReminderService(JobPostingRepository postings, NotificationService notifications) {
        this.postings = postings;
        this.notifications = notifications;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void createUpcomingDeadlineReminders() {
        LocalDate today = LocalDate.now();
        postings.findSavedPostingsNearingDeadline(today, today.plusDays(REMINDER_WINDOW_DAYS))
                .forEach(candidate -> notifications.create(
                        JOB_SEEKER,
                        candidate.getPersonId(),
                        "Saved job deadline approaching",
                        candidate.getTitle() + " closes on " + candidate.getApplicationDeadline() + ".",
                        "JOB_DEADLINE",
                        "/job.html?id=" + candidate.getPostId(),
                        "JOB_DEADLINE:" + candidate.getPersonId() + ":" + candidate.getPostId()
                                + ":" + candidate.getApplicationDeadline()));
    }
}
```

### `V11__create_notifications.sql`

```sql
CREATE TABLE notification (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    recipient_role VARCHAR(20) NOT NULL,
    recipient_id INTEGER NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(40) NOT NULL,
    link VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    deduplication_key VARCHAR(255) UNIQUE,
    CONSTRAINT notification_recipient_role_check
        CHECK (recipient_role IN ('Job Seeker', 'Recruiter'))
);

CREATE INDEX notification_recipient_created_idx
    ON notification (recipient_role, recipient_id, created_at);

CREATE INDEX notification_recipient_unread_idx
    ON notification (recipient_role, recipient_id, read_at);
```

### `NotificationConfig.java`

This file was initially added to the project before the implementation request. The generated changes added the package declaration and corrected the allowed origin format.

```java
package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class NotificationConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:8081").withSockJS();
    }
}
```

### `DemoApplication.java`

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### `JobPostingRepository.java` addition

The following interface and query were added to the existing repository. The rest of the repository’s existing methods were left unchanged.

```java
interface SavedDeadlineCandidate {
    Integer getPersonId();

    Integer getPostId();

    String getTitle();

    LocalDate getApplicationDeadline();
}

@Query(value = """
        SELECT f.person_id AS "personId", j.post_id AS "postId", j.title AS "title",
               j.application_deadline AS "applicationDeadline"
        FROM fav_postings f
        JOIN job_postings j ON j.post_id = f.post_id
        WHERE f.status = TRUE
          AND (j.status IS NULL OR j.status = 'Open')
          AND j.application_deadline BETWEEN :today AND :reminderThrough
        """, nativeQuery = true)
List<SavedDeadlineCandidate> findSavedPostingsNearingDeadline(
        @Param("today") LocalDate today,
        @Param("reminderThrough") LocalDate reminderThrough
);
```

### `ApplicationService.java` integration

The notification service was added as a dependency:

```java
private final NotificationService notifications;

public ApplicationService(ApplicationRepository applications, ResumeRepository resumes,
                          JobPostingRepository postings, EmployerUserRepository employers,
                          NotificationService notifications) {
    this.applications = applications;
    this.resumes = resumes;
    this.postings = postings;
    this.employers = employers;
    this.notifications = notifications;
}
```

After a new application is successfully saved, the recruiter is notified:

```java
// The unique constraint on (person_id, post_id) also covers two requests at once
Application saved;
try {
    saved = applications.saveAndFlush(new Application(personId, postId));
} catch (DataIntegrityViolationException e) {
    throw alreadyApplied();
}
JobPosting posting = postings.findById(postId).orElseThrow();
notifications.create(
        "Recruiter",
        posting.getEmployerId(),
        "New job application",
        "A candidate applied to " + posting.getTitle() + ".",
        "NEW_APPLICATION",
        "/recruiterpostings.html",
        "NEW_APPLICATION:" + saved.getId());
return describe(List.of(saved)).get(0);
```

### `RecruiterService.java` integration

The notification service was added as a dependency:

```java
private final NotificationService notifications;

public RecruiterService(JobPostingRepository postings, ApplicationRepository applications,
                        PersonUserRepository personUsers, ResumeRepository resumes,
                        NotificationService notifications) {
    this.postings = postings;
    this.applications = applications;
    this.personUsers = personUsers;
    this.resumes = resumes;
    this.notifications = notifications;
}
```

After a recruiter changes an application’s status, the job seeker is notified:

```java
application.updateStatus(next);
Application saved = applications.save(application);
PersonUser person = personUsers.findById(saved.getPersonId()).orElseThrow();
JobPosting posting = postings.findById(saved.getPostId()).orElseThrow();
notifications.create(
        "Job Seeker",
        saved.getPersonId(),
        "Application status updated",
        "Your application for " + posting.getTitle() + " is now " + next.getLabel() + ".",
        "APPLICATION_STATUS",
        "/applications.html",
        "APPLICATION_STATUS:" + saved.getId() + ":" + next.name());
return ApplicantResponse.from(saved, person, resumes.existsByPersonIdAndCurrentTrue(saved.getPersonId()));
```

### `NotificationControllerTests.java`

```java
package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import com.example.demo.service.DeadlineReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTests {

    private static final AtomicInteger NEXT_POST_ID = new AtomicInteger(9000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notifications;

    @Autowired
    private DeadlineReminderService deadlineReminders;

    @Autowired
    private JdbcTemplate jdbc;

    private MockHttpSession loggedInJobSeeker() throws Exception {
        String email = "notify-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"Job Seeker\",\"name\":\"Jane\",\"surname\":\"Doe\","
                                + "\"email\":\"" + email + "\",\"password\":\"secret123\","
                                + "\"phone\":\"514-555-1234\",\"address\":\"1455 De Maisonneuve Blvd W\","
                                + "\"city\":\"Montreal\",\"provinceState\":\"Quebec\",\"country\":\"Canada\","
                                + "\"postalCodeZip\":\"H3G 1M8\"}"))
                .andExpect(status().isOk());

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    }

    private MockHttpSession loggedInRecruiter() throws Exception {
        String email = "recruiter-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"Recruiter\",\"name\":\"Riley\",\"surname\":\"Recruiter\","
                                + "\"companyName\":\"Acme\",\"email\":\"" + email + "\","
                                + "\"password\":\"secret123\",\"phone\":\"514-555-1234\","
                                + "\"address\":\"1455 De Maisonneuve Blvd W\",\"city\":\"Montreal\","
                                + "\"provinceState\":\"Quebec\",\"country\":\"Canada\","
                                + "\"postalCodeZip\":\"H3G 1M8\"}"))
                .andExpect(status().isOk());

        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();
    }

    @Test
    void userCanListFilterAndMarkOnlyTheirOwnNotificationsRead() throws Exception {
        MockHttpSession session = loggedInJobSeeker();
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_USER_ID);
        notifications.create("Job Seeker", userId, "Interview update", "Your application changed.",
                "APPLICATION_STATUS", "/applications.html", null);

        String listResponse = mockMvc.perform(get("/api/notifications").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Interview update"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/notifications").param("unreadOnly", "true").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/notifications/unread-count").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
        Number notificationId = com.jayway.jsonpath.JsonPath.read(listResponse, "$[0].id");
        long id = notificationId.longValue();

        mockMvc.perform(patch("/api/notifications/{id}/read", id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").isNotEmpty());

        mockMvc.perform(get("/api/notifications/unread-count").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void notificationEndpointsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotMarkAnotherUsersNotificationRead() throws Exception {
        MockHttpSession ownerSession = loggedInJobSeeker();
        Integer ownerId = (Integer) ownerSession.getAttribute(AuthController.SESSION_USER_ID);
        Long notificationId = notifications.create("Job Seeker", ownerId, "Private", "Private message.",
                "APPLICATION_STATUS", null, null).getId();
        MockHttpSession anotherSession = loggedInJobSeeker();

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId).session(anotherSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void savedPostingNearDeadlineGetsOneReminderEvenWhenSchedulerRunsTwice() throws Exception {
        MockHttpSession seeker = loggedInJobSeeker();
        Integer personId = (Integer) seeker.getAttribute(AuthController.SESSION_USER_ID);
        MockHttpSession recruiter = loggedInRecruiter();
        Integer employerId = (Integer) recruiter.getAttribute(AuthController.SESSION_USER_ID);
        int postId = NEXT_POST_ID.getAndIncrement();
        LocalDate deadline = LocalDate.now().plusDays(2);
        jdbc.update("INSERT INTO job_postings (employer_id, post_id, title, description, department, "
                        + "application_deadline, salary, required_skills, status) "
                        + "VALUES (?, ?, 'Saved posting', 'Description', 'Engineering', ?, '60000', 'Java', 'Open')",
                employerId, postId, Date.valueOf(deadline));
        jdbc.update("INSERT INTO fav_postings (person_id, post_id, status) VALUES (?, ?, TRUE)",
                personId, postId);

        deadlineReminders.createUpcomingDeadlineReminders();
        deadlineReminders.createUpcomingDeadlineReminders();

        var result = notifications.list("Job Seeker", personId, false);
        org.assertj.core.api.Assertions.assertThat(result)
                .filteredOn(notification -> notification.type().equals("JOB_DEADLINE"))
                .hasSize(1);
    }
}
```

## Validation and limitations

- The backend test suite passed: **86 tests, 0 failures**.
- The notification API is REST-based; the retained WebSocket configuration does not currently push notification events to clients.
- Deadline reminders cover saved, open job postings that close within three days. Interview and follow-up dates are not modeled yet, so reminders for those events need additional data and scheduling logic.
- Confirm that the frontend expects the routes and JSON field names listed above; its documented contract was not present in the inspected workspace.
