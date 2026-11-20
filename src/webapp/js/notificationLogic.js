/* Notification rules shared by the top bar badge (shell.js) and the notifications page (notifications.js):
   which types each role sees, what counts as unread, and what a card shows.
   No DOM code here so src/webapp/tests/notificationLogic.test.js can run it with Node.
   The API is documented in documentation/notifications/frontend-api-contract.md. */
(function (root, factory) {
  const lib = factory();
  if (typeof module === 'object' && module.exports) module.exports = lib;
  else root.CCNotifications = lib;
})(typeof self !== 'undefined' ? self : this, function () {

  // Same names as the constants in NotificationService.java; any other type is not shown
  const TYPES = {
    APPLICATION_STATUS_UPDATED: { icon: 'chart', label: 'Application update', forRecruiter: false },
    INTERVIEW_INVITATION: { icon: 'calendar', label: 'Interview invitation', forRecruiter: false },
    DEADLINE_REMINDER: { icon: 'bell', label: 'Deadline reminder', forRecruiter: false },
    NEW_APPLICATION: { icon: 'users', label: 'New application', forRecruiter: true }
  };

  const typeOf = n => String((n && (n.type || n.notificationType)) || '').trim().toUpperCase();

  // The types a recruiter or a job seeker sees
  function allowedTypes(isRecruiter) {
    return Object.keys(TYPES).filter(type => TYPES[type].forRecruiter === Boolean(isRecruiter));
  }

  // The API returns an array, or { notifications: [...] }; anything else is no notifications
  function listOf(data) {
    if (Array.isArray(data)) return data;
    return (data && Array.isArray(data.notifications)) ? data.notifications : [];
  }

  function visible(data, isRecruiter) {
    const allowed = allowedTypes(isRecruiter);
    return listOf(data).filter(n => allowed.includes(typeOf(n)));
  }

  const isUnread = n => n.read !== true && n.isRead !== true;

  // Unread notifications the user can actually see, as the badge shows them
  function unreadCount(data, isRecruiter) {
    return visible(data, isRecruiter).filter(isUnread).length;
  }

  const badgeText = count => count > 99 ? '99+' : String(count);

  // What one card shows; dates stay as the API sent them so the page can format them
  function view(n) {
    const info = TYPES[typeOf(n)] || { icon: 'bell', label: 'Notification' };
    return {
      id: n.id,
      unread: isUnread(n),
      icon: info.icon,
      title: n.title || n.jobTitle || 'CareerConnect notification',
      message: n.message || n.body || info.label,
      deadline: n.deadline || n.applicationDeadline || '',
      date: n.createdAt || n.createdDate || n.timestamp || ''
    };
  }

  return { TYPES, typeOf, allowedTypes, visible, isUnread, unreadCount, badgeText, view };
});
