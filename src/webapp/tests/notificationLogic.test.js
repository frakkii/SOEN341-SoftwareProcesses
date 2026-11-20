// Tests for js/notificationLogic.js (top bar badge and notifications page). Run from the repo root with:
//   node --test src/webapp/tests/
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const N = require(path.join(__dirname, '..', 'js', 'notificationLogic.js'));

const note = (id, type, extra = {}) => ({
  id, type, title: 'Junior Developer', message: 'Message ' + id,
  createdAt: '2026-11-19T14:00:00Z', read: false, ...extra
});
const FOR_SEEKER = [
  note(1, 'APPLICATION_STATUS_UPDATED'),
  note(2, 'INTERVIEW_INVITATION'),
  note(3, 'DEADLINE_REMINDER', { read: true }),
  note(6, 'NEW_JOB_POSTING')
];
const FOR_RECRUITER = [note(4, 'NEW_APPLICATION'), note(5, 'NEW_APPLICATION', { read: true })];

// ---- The page and the backend use the same types ----

test('every notification type the backend sends is one the page shows', () => {
  // The constants in NotificationService.java; a type missing here would be hidden from users
  const java = fs.readFileSync(path.join(__dirname, '..', '..', 'backend', 'src', 'main', 'java',
    'com', 'example', 'demo', 'service', 'NotificationService.java'), 'utf8');
  const backendTypes = [...java.matchAll(/public static final String (\w+) = "(\w+)";/g)].map(m => m[2]);
  assert.ok(backendTypes.length >= 5, 'found the type constants in NotificationService.java');
  assert.deepEqual([...backendTypes].sort(), Object.keys(N.TYPES).sort());
});

test('each type goes to exactly one role', () => {
  assert.deepEqual(N.allowedTypes(false), [
    'APPLICATION_STATUS_UPDATED', 'INTERVIEW_INVITATION', 'DEADLINE_REMINDER', 'NEW_JOB_POSTING'
  ]);
  assert.deepEqual(N.allowedTypes(true), ['NEW_APPLICATION']);
});

// ---- Which notifications are shown ----

test('a job seeker sees seeker notifications but not recruiter ones', () => {
  const shown = N.visible([...FOR_SEEKER, ...FOR_RECRUITER], false);
  assert.deepEqual(shown.map(n => n.id), [1, 2, 3, 6]);
});

test('a recruiter sees only new application notifications', () => {
  const shown = N.visible([...FOR_SEEKER, ...FOR_RECRUITER], true);
  assert.deepEqual(shown.map(n => n.id), [4, 5]);
});

test('unknown or missing types are not shown', () => {
  // These are the names the backend used before #186: the page hid them
  const old = [note(6, 'APPLICATION_STATUS'), note(7, 'JOB_DEADLINE'), note(8, undefined)];
  assert.deepEqual(N.visible(old, false), []);
});

test('types are matched without caring about case, spaces or the field name', () => {
  const shown = N.visible([
    note(9, ' interview_invitation '),
    { id: 10, notificationType: 'deadline_reminder' }
  ], false);
  assert.deepEqual(shown.map(n => n.id), [9, 10]);
});

test('accepts an array or a { notifications } envelope, and anything else is empty', () => {
  assert.equal(N.visible(FOR_SEEKER, false).length, 4);
  assert.equal(N.visible({ notifications: FOR_SEEKER, unreadCount: 3 }, false).length, 4);
  for (const bad of [null, undefined, {}, { notifications: 'x' }, 'oops']) {
    assert.deepEqual(N.visible(bad, false), [], String(bad));
  }
});

// ---- Unread badge ----

test('the badge counts only unread notifications the user can see', () => {
  const all = [...FOR_SEEKER, ...FOR_RECRUITER];
  assert.equal(N.unreadCount(all, false), 3);   // 1, 2 and 6; 3 is read; 4 is for recruiters
  assert.equal(N.unreadCount(all, true), 1);    // 4; 5 is read
  assert.equal(N.unreadCount([], false), 0);
});

test('read can come as read or isRead', () => {
  assert.equal(N.isUnread({ read: false }), true);
  assert.equal(N.isUnread({}), true);
  assert.equal(N.isUnread({ read: true }), false);
  assert.equal(N.isUnread({ isRead: true }), false);
});

test('the badge shows 99+ above 99', () => {
  assert.equal(N.badgeText(1), '1');
  assert.equal(N.badgeText(99), '99');
  assert.equal(N.badgeText(100), '99+');
});

// ---- What a card shows ----

test('a card shows the title, message, icon and dates from the API', () => {
  const card = N.view(note(2, 'INTERVIEW_INVITATION', { applicationDeadline: '2026-11-30' }));
  assert.deepEqual(card, {
    id: 2, unread: true, icon: 'calendar', title: 'Junior Developer', message: 'Message 2',
    deadline: '2026-11-30', date: '2026-11-19T14:00:00Z'
  });
});

test('each type has its own icon', () => {
  const icons = Object.keys(N.TYPES).map(type => N.view(note(1, type)).icon);
  assert.deepEqual(icons, ['chart', 'calendar', 'bell', 'briefcase', 'users']);
  assert.equal(new Set(icons).size, icons.length);
});

test('a card with missing fields falls back to readable text', () => {
  const card = N.view({ id: 3, type: 'NEW_APPLICATION' });
  assert.equal(card.title, 'CareerConnect notification');
  assert.equal(card.message, 'New application');
  assert.equal(card.deadline, '');
  assert.equal(card.date, '');
  assert.equal(N.view({ id: 4, type: 'SOMETHING_ELSE' }).icon, 'bell');
});
