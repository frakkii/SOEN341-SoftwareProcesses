
# AI Log sprint 3
**Task:** Notification and reminder backend

**Purpose of AI Use:** Review the existing backend that i made, add more functionality and test it

**Chat Link or Prompt/Response:** Added the prompts and responces below  
**Prompt:** see below
**Response:** see below

**AI-Suggested Content:** Generated code to make real time notification related to the job postings

**Validation:** reviewed using test cases

**Decision:** Accepted after multiple prompts

**Reflection:** Deeper understanding of the Spring Boot technologie and aspects of the backend for a notification function

**Responsible Person:** Shanza Riasat

## Initial recommendations

Your current code is a **WebSocket broadcast prototype**, not a complete notification backend. [`NotificationController.java`](../src/backend/src/main/java/com/example/demo/controller/NotificationController.java) sends a message to everyone subscribed to `/topic/notifications`; it doesn’t save notifications, identify the recipient, or support unread/mark-as-read. The frontend’s notification page and documented API contract aren’t present in the workspace snapshot I inspected, so confirm endpoint names and JSON fields against your teammate’s contract.

I’d build this in this order:

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

## Implementation summary

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

## Step-by-step walkthrough

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
