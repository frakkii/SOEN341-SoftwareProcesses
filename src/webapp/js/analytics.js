/* Counting, grouping, filtering and sorting for the My Applications dashboard.
   No DOM code here so the tests in src/webapp/tests can run it with Node.
   apps is the list from GET /api/applications. */
(function (root, factory) {
  const lib = factory();
  if (typeof module === 'object' && module.exports) module.exports = lib;
  else root.CCAnalytics = lib;
})(typeof self !== 'undefined' ? self : this, function () {

  // Same stages and order as ApplicationStatus.java. cls matches the .status pill classes in app.css
  const STATUSES = [
    { label: 'Applied',      cls: 'applied',      color: '#7c3aed' },
    { label: 'Under Review', cls: 'under-review', color: '#0284c7' },
    { label: 'Interview',    cls: 'interview',    color: '#d97706' },
    { label: 'Offered',      cls: 'offered',      color: '#059669' },
    { label: 'Rejected',     cls: 'rejected',     color: '#dc2626' }
  ];
  // Unknown statuses go here so they still count (the database shouldn't allow any)
  const OTHER = { label: 'Other', cls: 'other', color: '#94a3b8' };
  const LABELS = STATUSES.map(s => s.label);
  const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

  const list = apps => Array.isArray(apps) ? apps.filter(a => a && typeof a === 'object') : [];

  // "Under Review", "UNDER_REVIEW", " under-review " -> "Under Review"; unknown -> "Other"
  function normalizeStatus(value) {
    if (typeof value !== 'string') return OTHER.label;
    const key = value.trim().toLowerCase().replace(/[\s_-]+/g, ' ');
    return LABELS.find(l => l.toLowerCase() === key) || OTHER.label;
  }

  function statusInfo(label) {
    return STATUSES.find(s => s.label === label) || OTHER;
  }

  // A Date for an ISO timestamp (or Date / epoch ms); null if missing or invalid
  function parseDate(value) {
    if (value === null || value === undefined || value === '') return null;
    if (typeof value !== 'string' && typeof value !== 'number' && !(value instanceof Date)) return null;
    const d = new Date(value);
    return Number.isNaN(d.getTime()) ? null : d;
  }

  // Totals for the stat tiles, always from the full list (not the filtered one)
  function summarize(apps) {
    const all = list(apps);
    const counts = {};
    LABELS.forEach(l => { counts[l] = 0; });
    counts[OTHER.label] = 0;
    let undated = 0;
    all.forEach(a => {
      counts[normalizeStatus(a.status)]++;
      if (!parseDate(a.appliedAt)) undated++;
    });
    return { total: all.length, counts, undated };
  }

  // Rounded to whole percents, so 1 of 3 is 33% (the total can be 99 or 101)
  function percentages(counts, total) {
    return Object.fromEntries(Object.keys(counts).map(k => [k, total ? Math.round(counts[k] * 100 / total) : 0]));
  }

  // Donut segments: the known stages in pipeline order, plus Other only if used
  function statusBreakdown(apps) {
    const { total, counts } = summarize(apps);
    const pct = percentages(counts, total);
    return [...STATUSES, OTHER]
      .filter(s => s !== OTHER || counts[OTHER.label] > 0)
      .map(s => ({ ...s, count: counts[s.label], percent: pct[s.label] }));
  }

  // Months use local time, same as the dates shown on the cards
  const monthIndex = d => d.getFullYear() * 12 + d.getMonth();
  const monthOf = i => ({
    key: Math.floor(i / 12) + '-' + String(i % 12 + 1).padStart(2, '0'),
    label: MONTHS[i % 12] + ' ' + Math.floor(i / 12),
    month: MONTHS[i % 12],
    year: Math.floor(i / 12)
  });

  // Applications per month, oldest first, empty months = 0. range is 'all' or a
  // number of months ending this month (6, 12). Apps without a valid date are skipped.
  function monthlyCounts(apps, range = 'all', now = new Date()) {
    const byMonth = new Map();
    list(apps).forEach(a => {
      const d = parseDate(a.appliedAt);
      if (d) byMonth.set(monthIndex(d), (byMonth.get(monthIndex(d)) || 0) + 1);
    });
    const current = monthIndex(now);
    let from, to;
    const n = Number(range);
    if (range !== 'all' && Number.isInteger(n) && n > 0) {
      from = current - n + 1;
      to = current;
    } else {
      if (byMonth.size === 0) return [];
      from = Math.min(...byMonth.keys());
      to = Math.max(current, ...byMonth.keys());
    }
    const months = [];
    for (let i = from; i <= to; i++) months.push({ ...monthOf(i), count: byMonth.get(i) || 0 });
    return months;
  }

  // What the history list shows for the title (same fallback as the cards)
  const titleOf = a => a.jobTitle || ('Posting #' + a.postId);

  // status: 'All' or a status label; query: matched against job title and company
  function filterApplications(apps, { status = 'All', query = '' } = {}) {
    const q = String(query || '').trim().toLowerCase();
    return list(apps).filter(a =>
      (status === 'All' || normalizeStatus(a.status) === status) &&
      (!q || (titleOf(a) + ' ' + (a.companyName || '')).toLowerCase().includes(q)));
  }

  // 'newest' or 'oldest'. Returns a copy, apps without a date go last
  function sortApplications(apps, order = 'newest') {
    const dir = order === 'oldest' ? 1 : -1;
    return list(apps)
      .map((a, i) => ({ a, i, t: parseDate(a.appliedAt) }))
      .sort((x, y) => {
        if (!x.t || !y.t) return (x.t ? 0 : 1) - (y.t ? 0 : 1) || x.i - y.i;
        return dir * (x.t - y.t) || x.i - y.i;
      })
      .map(x => x.a);
  }

  return {
    STATUSES, OTHER, normalizeStatus, statusInfo, parseDate, summarize, percentages,
    statusBreakdown, monthlyCounts, filterApplications, sortApplications, titleOf
  };
});
