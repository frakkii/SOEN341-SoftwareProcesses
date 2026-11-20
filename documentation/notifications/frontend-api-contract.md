# Notifications frontend API contract

The notification page uses the existing same-origin session cookie through
`CC.api()`. The backend should provide `GET /api/notifications`, returning an
array or an envelope with `notifications` and `unreadCount`.

Each notification should include:

```json
{
  "id": 42,
  "type": "APPLICATION_STATUS_UPDATED",
  "title": "Junior Developer",
  "message": "Your application status changed to Interview.",
  "applicationDeadline": "2026-11-30",
  "createdAt": "2026-11-12T14:00:00Z",
  "read": false
}
```

Supported types are `APPLICATION_STATUS_UPDATED`,
`INTERVIEW_INVITATION`, `DEADLINE_REMINDER`, and `NEW_APPLICATION`.
The endpoint must return only the authenticated user's notifications.

The frontend uses `PATCH /api/notifications/{id}/read` when a real item is
marked as read. Demo mode does not send this request.

## Temporary demo mode

Open `notifications.html?demoNotifications=1` to render four in-memory demo
notifications, including both read and unread items. Open
`notifications.html?demoNotifications=empty` to verify the empty state.
Demo changes disappear on refresh. Without the parameter, the frontend keeps
using the backend API.
