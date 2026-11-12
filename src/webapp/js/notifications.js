/* Notification list. The API contract is documented in documentation/notifications/frontend-api-contract.md. */
(function () {
  if (!window.CC) return;
  const { esc, fmtDate, icon, notificationData } = CC;
  const list = document.getElementById('notifications');
  const empty = document.getElementById('notification-empty');
  const error = document.getElementById('notification-error');
  const demoEmpty = new URLSearchParams(location.search).get('demoNotifications') === 'empty';

  const typeInfo = {
    APPLICATION_STATUS_UPDATED: ['chart', 'Application update'],
    INTERVIEW_INVITATION: ['calendar', 'Interview invitation'],
    DEADLINE_REMINDER: ['bell', 'Deadline reminder'],
    NEW_APPLICATION: ['users', 'New application']
  };

  function normalize(data) {
    return notificationData.visible(data);
  }

  function notificationView(n) {
    const type = String(n.type || n.notificationType || '').toUpperCase();
    const [symbol, fallbackTitle] = typeInfo[type] || ['bell', 'Notification'];
    const title = n.title || n.jobTitle || 'CareerConnect notification';
    const message = n.message || n.body || fallbackTitle;
    const deadline = n.deadline || n.applicationDeadline;
    const date = n.createdAt || n.createdDate || n.timestamp;
    return {
      id: n.id,
      unread: n.read !== true && n.isRead !== true,
      symbol,
      title,
      message,
      deadline: deadline ? fmtDate(deadline) : '',
      date: date ? fmtDate(date) : ''
    };
  }

  function render(raw) {
    const n = notificationView(raw);
    return `<article class="card notification${n.unread ? ' unread' : ''}" data-notification-id="${esc(n.id ?? '')}">
      <span class="notification-icon">${icon(n.symbol)}</span>
      <div class="notification-copy">
        <div class="notification-heading"><h2>${notificationData.demo ? 'DEMO · ' : ''}${esc(n.title)}</h2>${n.unread ? '<span class="unread-dot" aria-label="Unread"></span>' : ''}</div>
        <p>${esc(n.message)}</p>
        ${n.deadline ? `<small>${icon('calendar')} Deadline: ${esc(n.deadline)}</small>` : ''}
        ${n.date ? `<time datetime="${esc(raw.createdAt || raw.createdDate || raw.timestamp)}">${esc(n.date)}</time>` : ''}
        ${n.unread ? '<button type="button" class="mark-read" data-mark-read>Mark as read</button>' : ''}
      </div>
    </article>`;
  }

  function show(data) {
    const notifications = normalize(data);
    list.innerHTML = '';
    if (!notifications.length) {
      list.classList.add('hidden');
      empty.classList.remove('hidden');
      return;
    }
    list.innerHTML = notifications.map(render).join('');
  }

  list.addEventListener('click', async event => {
    const button = event.target.closest('[data-mark-read]');
    if (!button) return;
    const card = button.closest('[data-notification-id]');
    if (!card) return;
    button.disabled = true;
    try {
      await notificationData.markRead(card.dataset.notificationId);
      const current = await notificationData.get();
      notificationData.updateBadge(current);
      show(current);
    } catch (err) {
      button.disabled = false;
      error.textContent = 'Could not mark this notification as read. Please try again.';
      error.classList.remove('hidden');
    }
  });

  (demoEmpty ? Promise.resolve([]) : notificationData.get()).then(show).catch(() => {
    list.innerHTML = '';
    list.classList.add('hidden');
    error.classList.remove('hidden');
  });
})();
