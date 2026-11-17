// Tests for js/applicantFilters.js (recruiter applicants page). Run from the repo root with:
//   node --test src/webapp/tests/
const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');

const F = require(path.join(__dirname, '..', 'js', 'applicantFilters.js'));

// Midday UTC so the local day is the same in every time zone from UTC-11 to UTC+11
const applicant = (id, name, status, day, extra = {}) => ({
  applicationId: id, name, surname: 'Test', email: name.toLowerCase() + '@mail.test', city: 'Montreal',
  status, appliedAt: day ? day + 'T12:00:00Z' : null, ...extra
});
const ALL = [
  applicant(1, 'Ana', 'Applied', '2026-11-02'),
  applicant(2, 'Ben', 'Interview', '2026-11-05', { city: 'Toronto' }),
  applicant(3, 'Cleo', 'Rejected', '2026-11-10'),
  applicant(4, 'Dev', 'Interview', '2026-11-15'),
  applicant(5, 'Eli', 'Offered', '2026-10-28')
];
const ids = list => list.map(a => a.applicationId);

test('no filters returns every applicant', () => {
  assert.deepEqual(ids(F.filterApplicants(ALL)), [1, 2, 3, 4, 5]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { status: 'All', query: '  ', from: '', to: '' })), [1, 2, 3, 4, 5]);
});

test('status filter keeps only that stage', () => {
  assert.deepEqual(ids(F.filterApplicants(ALL, { status: 'Interview' })), [2, 4]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { status: 'Under Review' })), []);
});

test('search matches name, surname, email or city, ignoring case', () => {
  assert.deepEqual(ids(F.filterApplicants(ALL, { query: 'BEN' })), [2]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { query: 'cleo@mail' })), [3]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { query: 'toronto' })), [2]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { query: 'test' })).length, 5);
});

test('date range is inclusive at both ends', () => {
  assert.deepEqual(ids(F.filterApplicants(ALL, { from: '2026-11-05', to: '2026-11-10' })), [2, 3]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { from: '2026-11-10' })), [3, 4]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { to: '2026-11-02' })), [1, 5]);
});

test('filters combine', () => {
  assert.deepEqual(ids(F.filterApplicants(ALL, { status: 'Interview', from: '2026-11-10' })), [4]);
  assert.deepEqual(ids(F.filterApplicants(ALL, { status: 'Interview', query: 'ana' })), []);
});

test('applicants without a valid date only match when no date filter is set', () => {
  const list = [...ALL, applicant(6, 'Fay', 'Applied', null), applicant(7, 'Gus', 'Applied', 'not-a-date')];
  assert.deepEqual(ids(F.filterApplicants(list, { status: 'Applied' })), [1, 6, 7]);
  assert.deepEqual(ids(F.filterApplicants(list, { status: 'Applied', from: '2026-01-01' })), [1]);
});

test('a reversed date range is flagged and matches nothing', () => {
  assert.equal(F.invalidRange('2026-11-10', '2026-11-01'), true);
  assert.equal(F.invalidRange('2026-11-01', '2026-11-10'), false);
  assert.equal(F.invalidRange('2026-11-01', ''), false);
  assert.equal(F.invalidRange('', ''), false);
  assert.deepEqual(F.filterApplicants(ALL, { from: '2026-11-10', to: '2026-11-01' }), []);
});

test('sort newest first by default and oldest first on request', () => {
  assert.deepEqual(ids(F.sortApplicants(ALL)), [4, 3, 2, 1, 5]);
  assert.deepEqual(ids(F.sortApplicants(ALL, 'oldest')), [5, 1, 2, 3, 4]);
});

test('sorting puts undated applicants last and does not change the input', () => {
  const list = [applicant(8, 'Hal', 'Applied', null), ...ALL];
  assert.deepEqual(ids(F.sortApplicants(list, 'newest')).at(-1), 8);
  assert.deepEqual(ids(F.sortApplicants(list, 'oldest')).at(-1), 8);
  assert.equal(list[0].applicationId, 8);
});

test('countByStatus counts every stage, including empty ones', () => {
  assert.deepEqual(F.countByStatus(ALL),
    { Applied: 1, 'Under Review': 0, Interview: 2, Offered: 1, Rejected: 1 });
  assert.deepEqual(F.countByStatus([]),
    { Applied: 0, 'Under Review': 0, Interview: 0, Offered: 0, Rejected: 0 });
});

test('bad input does not crash', () => {
  assert.deepEqual(F.filterApplicants(null, { status: 'Applied' }), []);
  assert.deepEqual(F.filterApplicants([null, 3, ALL[0]]).length, 1);
  assert.deepEqual(F.sortApplicants(undefined), []);
  assert.equal(F.appliedDay({}), null);
  assert.equal(F.appliedDay({ appliedAt: '2026-11-02T12:00:00Z' }), '2026-11-02');
});
