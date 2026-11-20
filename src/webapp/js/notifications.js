/* Notification list. The API contract is documented in documentation/notifications/frontend-api-contract.md. */
(function () {
  if (!window.CC) return;
  const { esc, fmtDate, icon, notificationData } = CC;
  const list = document.getElementById('notifications');
  const empty = document.getElementById('notification-empty');
  const error = document.getElementById('notification-error');
  const demoEmpty = new URLSearchParams(location.search).get('demoNotifications') === 'empty';

  function normalize(data) {
    return notificationData.visible(data);
  }

  // What a card shows comes from js/notificationLogic.js (unit tested); dates are formatted here
  function render(raw) {
    const n = CCNotifications.view(raw);
    const deadline = n.deadline ? fmtDate(n.deadline) : '';
    const date = n.date ? fmtDate(n.date) : '';
    return `<article class="card notification${n.unread ? ' unread' : ''}" data-notification-id="${esc(n.id ?? '')}">
      <span class="notification-icon">${icon(n.icon)}</span>
      <div class="notification-copy">
        <div class="notification-heading"><h2>${notificationData.demo ? 'DEMO · ' : ''}${esc(n.title)}</h2>${n.unread ? '<span class="unread-dot" aria-label="Unread"></span>' : ''}</div>
        <p>${esc(n.message)}</p>
        ${deadline ? `<small>${icon('calendar')} Deadline: ${esc(deadline)}</small>` : ''}
        ${date ? `<time datetime="${esc(n.date)}">${esc(date)}</time>` : ''}
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
