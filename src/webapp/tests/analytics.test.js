// Tests for js/analytics.js and js/charts.js. Run from the repo root with:
//   node --test src/webapp/tests/analytics.test.js
const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');

const A = require(path.join(__dirname, '..', 'js', 'analytics.js'));
const C = require(path.join(__dirname, '..', 'js', 'charts.js'));

// Mid-month dates so the month is the same in any timezone
const app = (id, status, appliedAt, extra = {}) => ({
  id, postId: 100 + id, jobTitle: 'Job ' + id, companyName: 'Company ' + id,
  status, appliedAt, updatedAt: appliedAt, ...extra
});
const SAMPLE = [
  app(1, 'Applied', '2026-03-15T12:00:00Z'),
  app(2, 'Interview', '2026-03-16T12:00:00Z'),
  app(3, 'Interview', '2026-05-14T12:00:00Z'),
  app(4, 'Offered', '2026-06-15T12:00:00Z'),
  app(5, 'Rejected', '2026-06-17T12:00:00Z'),
  app(6, 'Under Review', '2026-01-15T12:00:00Z')
];

// ---------- Status normalization ----------

test('normalizeStatus accepts labels, constant names and odd spacing/case', () => {
  assert.equal(A.normalizeStatus('Applied'), 'Applied');
  assert.equal(A.normalizeStatus('UNDER_REVIEW'), 'Under Review');
  assert.equal(A.normalizeStatus(' under-review '), 'Under Review');
  assert.equal(A.normalizeStatus('interview'), 'Interview');
  assert.equal(A.normalizeStatus('OFFERED'), 'Offered');
  assert.equal(A.normalizeStatus('Rejected'), 'Rejected');
});

test('normalizeStatus puts unknown, empty and non-string values in Other', () => {
  for (const v of ['On Hold', '', '   ', null, undefined, 42, {}]) {
    assert.equal(A.normalizeStatus(v), 'Other', `value ${JSON.stringify(v)}`);
  }
});

// ---------- Totals and per-status counts ----------

test('summarize counts the total and every status', () => {
  const s = A.summarize(SAMPLE);
  assert.equal(s.total, 6);
  assert.deepEqual(s.counts, { Applied: 1, 'Under Review': 1, Interview: 2, Offered: 1, Rejected: 1, Other: 0 });
  assert.equal(s.undated, 0);
});

test('per-status counts always add up to the total, even with unexpected statuses', () => {
  const s = A.summarize([...SAMPLE, app(7, 'Withdrawn', '2026-06-15T12:00:00Z'), app(8, null, null)]);
  assert.equal(s.total, 8);
  assert.equal(s.counts.Other, 2);
  assert.equal(Object.values(s.counts).reduce((a, b) => a + b, 0), s.total);
});

test('summarize handles no applications', () => {
  const s = A.summarize([]);
  assert.equal(s.total, 0);
  assert.ok(Object.values(s.counts).every(n => n === 0));
});

test('summarize handles a single application', () => {
  const s = A.summarize([app(1, 'Offered', '2026-06-15T12:00:00Z')]);
  assert.equal(s.total, 1);
  assert.equal(s.counts.Offered, 1);
  assert.equal(s.counts.Applied, 0);
});

test('summarize ignores a non-array response and null entries instead of crashing', () => {
  assert.equal(A.summarize(undefined).total, 0);
  assert.equal(A.summarize({ message: 'error' }).total, 0);
  assert.equal(A.summarize([null, app(1, 'Applied', '2026-01-15T12:00:00Z')]).total, 1);
});

// ---------- Percentages ----------

test('percentages round each share to the nearest whole number', () => {
  assert.deepEqual(A.percentages({ a: 1, b: 1, c: 1 }, 3), { a: 33, b: 33, c: 33 });
  assert.deepEqual(A.percentages({ a: 1, b: 3 }, 4), { a: 25, b: 75 });
  assert.deepEqual(A.percentages({ a: 1, b: 5 }, 6), { a: 17, b: 83 });
});

test('percentages are all 0 when there are no applications', () => {
  assert.deepEqual(A.percentages({ a: 0, b: 0 }, 0), { a: 0, b: 0 });
});

test('statusBreakdown lists every stage in order and only shows Other when used', () => {
  const b = A.statusBreakdown(SAMPLE);
  assert.deepEqual(b.map(s => s.label), ['Applied', 'Under Review', 'Interview', 'Offered', 'Rejected']);
  assert.equal(b.find(s => s.label === 'Interview').percent, 33);
  assert.equal(b.reduce((n, s) => n + s.count, 0), SAMPLE.length);

  const withOther = A.statusBreakdown([...SAMPLE, app(9, 'Paused', '2026-06-15T12:00:00Z')]);
  assert.equal(withOther[withOther.length - 1].label, 'Other');
  assert.equal(withOther[withOther.length - 1].count, 1);
});

test('statusBreakdown gives 100% to the only status in a single-status dataset', () => {
  const b = A.statusBreakdown([app(1, 'Applied', '2026-06-15T12:00:00Z'), app(2, 'Applied', '2026-06-16T12:00:00Z')]);
  assert.equal(b.find(s => s.label === 'Applied').percent, 100);
  assert.ok(b.filter(s => s.label !== 'Applied').every(s => s.percent === 0));
});

// ---------- Dates ----------

test('parseDate accepts ISO timestamps and rejects missing or invalid values', () => {
  assert.equal(A.parseDate('2026-03-15T12:00:00Z').toISOString(), '2026-03-15T12:00:00.000Z');
  assert.equal(A.parseDate('2026-03-15T12:00:00.123456Z').getUTCMonth(), 2); // Java Instant precision
  for (const v of [null, undefined, '', 'not a date', '2026-13-45', {}, []]) {
    assert.equal(A.parseDate(v), null, `value ${JSON.stringify(v)}`);
  }
});

test('summarize counts applications without a valid date', () => {
  const s = A.summarize([app(1, 'Applied', 'garbage'), app(2, 'Applied', null), app(3, 'Applied', '2026-01-15T12:00:00Z')]);
  assert.equal(s.total, 3);
  assert.equal(s.undated, 2);
});

// ---------- Monthly aggregation ----------

const NOW = new Date(2026, 10, 16, 12); // 16 Nov 2026, local time

test('monthlyCounts (all time) is chronological, totals each month and fills empty months', () => {
  const m = A.monthlyCounts(SAMPLE, 'all', NOW);
  assert.equal(m[0].key, '2026-01');
  assert.equal(m[m.length - 1].key, '2026-11');          // runs up to the current month
  assert.equal(m.length, 11);
  const by = Object.fromEntries(m.map(x => [x.key, x.count]));
  assert.equal(by['2026-01'], 1);
  assert.equal(by['2026-02'], 0);                         // month without applications
  assert.equal(by['2026-03'], 2);
  assert.equal(by['2026-05'], 1);
  assert.equal(by['2026-06'], 2);
  assert.equal(m.reduce((n, x) => n + x.count, 0), SAMPLE.length);
  assert.equal(m[2].label, 'Mar 2026');
});

test('monthlyCounts spans several years with correct labels and order', () => {
  const m = A.monthlyCounts([app(1, 'Applied', '2024-12-15T12:00:00Z'), app(2, 'Applied', '2026-02-15T12:00:00Z')], 'all', NOW);
  assert.equal(m[0].label, 'Dec 2024');
  assert.equal(m[1].label, 'Jan 2025');
  assert.equal(m.find(x => x.key === '2026-02').count, 1);
  const keys = m.map(x => x.key);
  assert.deepEqual(keys, [...keys].sort());               // chronological
  assert.equal(m.length, 24);                             // Dec 2024 .. Nov 2026
});

test('monthlyCounts with a range returns exactly that many months ending this month', () => {
  const six = A.monthlyCounts(SAMPLE, 6, NOW);
  assert.equal(six.length, 6);
  assert.deepEqual(six.map(x => x.key), ['2026-06', '2026-07', '2026-08', '2026-09', '2026-10', '2026-11']);
  assert.equal(six[0].count, 2);                          // June only; earlier months are outside the range
  assert.equal(A.monthlyCounts(SAMPLE, 12, NOW).length, 12);
  assert.equal(A.monthlyCounts(SAMPLE, 12, NOW).reduce((n, x) => n + x.count, 0), 6);
});

test('monthlyCounts skips invalid dates and handles empty input', () => {
  assert.deepEqual(A.monthlyCounts([], 'all', NOW), []);
  assert.deepEqual(A.monthlyCounts([app(1, 'Applied', 'bad')], 'all', NOW), []);
  const m = A.monthlyCounts([app(1, 'Applied', 'bad'), app(2, 'Applied', '2026-11-02T12:00:00Z')], 'all', NOW);
  assert.deepEqual(m.map(x => [x.key, x.count]), [['2026-11', 1]]);
  const empty = A.monthlyCounts([], 6, NOW);
  assert.equal(empty.length, 6);
  assert.ok(empty.every(x => x.count === 0));
});

// ---------- Filtering ----------

test('filterApplications by status', () => {
  assert.deepEqual(A.filterApplications(SAMPLE, { status: 'Interview' }).map(a => a.id), [2, 3]);
  assert.equal(A.filterApplications(SAMPLE, { status: 'All' }).length, 6);
  assert.equal(A.filterApplications(SAMPLE, { status: 'Other' }).length, 0);
});

test('filterApplications by title or company, case-insensitive, combined with status', () => {
  const apps = [
    app(1, 'Applied', '2026-01-15T12:00:00Z', { jobTitle: 'Java Developer', companyName: 'Acme Corp' }),
    app(2, 'Interview', '2026-02-15T12:00:00Z', { jobTitle: 'Data Analyst', companyName: 'Globex' }),
    app(3, 'Interview', '2026-03-15T12:00:00Z', { jobTitle: 'Junior Developer', companyName: 'Initech' }),
    app(4, 'Applied', '2026-03-15T12:00:00Z', { jobTitle: null, companyName: null, postId: 77 })
  ];
  assert.deepEqual(A.filterApplications(apps, { query: 'developer' }).map(a => a.id), [1, 3]);
  assert.deepEqual(A.filterApplications(apps, { query: '  GLOBEX ' }).map(a => a.id), [2]);
  assert.deepEqual(A.filterApplications(apps, { query: 'developer', status: 'Interview' }).map(a => a.id), [3]);
  assert.deepEqual(A.filterApplications(apps, { query: 'posting #77' }).map(a => a.id), [4]);
  assert.deepEqual(A.filterApplications(apps, { query: 'nothing matches' }), []);
});

test('filtering never changes the dashboard totals', () => {
  const before = JSON.stringify(A.summarize(SAMPLE));
  A.filterApplications(SAMPLE, { status: 'Offered', query: 'Job 4' });
  A.sortApplications(SAMPLE, 'oldest');
  assert.equal(JSON.stringify(A.summarize(SAMPLE)), before);
  assert.deepEqual(SAMPLE.map(a => a.id), [1, 2, 3, 4, 5, 6]);   // original array not reordered
});

// ---------- Sorting ----------

test('sortApplications newest first and oldest first', () => {
  assert.deepEqual(A.sortApplications(SAMPLE, 'newest').map(a => a.id), [5, 4, 3, 2, 1, 6]);
  assert.deepEqual(A.sortApplications(SAMPLE, 'oldest').map(a => a.id), [6, 1, 2, 3, 4, 5]);
});

test('sortApplications puts undated applications last in both orders', () => {
  const apps = [app(1, 'Applied', 'bad'), app(2, 'Applied', '2026-01-15T12:00:00Z'), app(3, 'Applied', '2026-02-15T12:00:00Z')];
  assert.deepEqual(A.sortApplications(apps, 'newest').map(a => a.id), [3, 2, 1]);
  assert.deepEqual(A.sortApplications(apps, 'oldest').map(a => a.id), [2, 3, 1]);
});

test('sortApplications handles empty and single-item lists', () => {
  assert.deepEqual(A.sortApplications([], 'newest'), []);
  assert.deepEqual(A.sortApplications([SAMPLE[0]], 'oldest'), [SAMPLE[0]]);
});

// ---------- Chart markup ----------

test('donut chart has an accessible text summary with counts and percentages', () => {
  const svg = C.donut(A.statusBreakdown(SAMPLE), SAMPLE.length);
  assert.match(svg, /role="img"/);
  assert.match(svg, /aria-label="Status breakdown of 6 applications: Applied 1 \(17%\), Under Review 1 \(17%\), Interview 2 \(33%\)/);
  assert.equal((svg.match(/class="seg"/g) || []).length, 5);  // one segment per non-zero status
});

test('donut chart for no applications shows only the empty ring', () => {
  const svg = C.donut(A.statusBreakdown([]), 0);
  assert.match(svg, /no applications yet/);
  assert.equal((svg.match(/class="seg"/g) || []).length, 0);
});

test('donut chart for a single status draws one full ring without a gap', () => {
  const svg = C.donut(A.statusBreakdown([app(1, 'Rejected', '2026-06-15T12:00:00Z')]), 1);
  assert.equal((svg.match(/class="seg"/g) || []).length, 1);
  assert.match(svg, /stroke-dasharray="100\.000 0\.000"/);
  assert.match(svg, /1 application:|Rejected 1 \(100%\)/);
});

test('month bars label each month, show the year under the first bar and January, and escape text', () => {
  const html = C.monthBars(A.monthlyCounts([app(1, 'Applied', '2025-11-15T12:00:00Z')], 'all', NOW));
  assert.match(html, /aria-label="Nov 2025: 1 application"/);
  assert.match(html, /aria-label="Jan 2026: 0 applications"/);
  assert.equal((html.match(/<small>2025<\/small>/g) || []).length, 1);
  assert.equal((html.match(/<small>2026<\/small>/g) || []).length, 1);
  assert.match(C.monthBars([]), /No dated applications/);
});

test('legend and data table escape user-controlled text', () => {
  const html = C.dataTable('<b>x</b>', ['Status'], [['<script>']]) + C.legend([{ label: '<img>', color: '#000', count: 1, percent: 100 }]);
  assert.ok(!html.includes('<script>'));
  assert.ok(!html.includes('<img>'));
  assert.match(html, /&lt;script&gt;/);
});
