/* Filtering, sorting and counting for the recruiter's applicants page (recruiterapplicants.html).
   No DOM code here so src/webapp/tests/applicantFilters.test.js can run it with Node.
   applicants is the list from GET /api/recruiter/postings/{id}/applications. */
(function (root, factory) {
  const lib = factory();
  if (typeof module === 'object' && module.exports) module.exports = lib;
  else root.CCApplicantFilters = lib;
})(typeof self !== 'undefined' ? self : this, function () {

  // Same stages and order as ApplicationStatus.java
  const STAGES = ['Applied', 'Under Review', 'Interview', 'Offered', 'Rejected'];

  const list = items => Array.isArray(items) ? items.filter(a => a && typeof a === 'object') : [];

  // The applicant's application day as YYYY-MM-DD in the browser's time zone (what the
  // recruiter sees in the table), or null if the timestamp is missing or invalid
  function appliedDay(applicant) {
    const d = new Date(applicant && applicant.appliedAt);
    if (!applicant || !applicant.appliedAt || Number.isNaN(d.getTime())) return null;
    const pad = n => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  }

  // filters: { status: 'All' | a stage, query: text, from: 'YYYY-MM-DD' | '', to: 'YYYY-MM-DD' | '' }
  // Both dates are inclusive. Applicants without a valid date only match when no date is set.
  function filterApplicants(applicants, filters = {}) {
    const status = filters.status && filters.status !== 'All' ? filters.status : null;
    const query = String(filters.query || '').trim().toLowerCase();
    const from = filters.from || '';
    const to = filters.to || '';
    return list(applicants).filter(a => {
      if (status && a.status !== status) return false;
      if (query && ![a.name, a.surname, a.email, a.city].join(' ').toLowerCase().includes(query)) return false;
      if (from || to) {
        const day = appliedDay(a);
        if (!day || (from && day < from) || (to && day > to)) return false;
      }
      return true;
    });
  }

  // 'newest' (default) or 'oldest' by application time; undated applicants go last. Stable.
  function sortApplicants(applicants, order) {
    const dir = order === 'oldest' ? 1 : -1;
    // new Date(null) is 1970, not invalid, so check for a missing value first
    const time = a => { const t = a.appliedAt ? new Date(a.appliedAt).getTime() : NaN; return Number.isNaN(t) ? null : t; };
    return list(applicants)
      .map((a, i) => ({ a, i, t: time(a) }))
      .sort((x, y) => {
        if (x.t === null || y.t === null) return (x.t === null) - (y.t === null) || x.i - y.i;
        return dir * (x.t - y.t) || x.i - y.i;
      })
      .map(x => x.a);
  }

  // Count per stage for the status filter's labels, from the full list
  function countByStatus(applicants) {
    const counts = Object.fromEntries(STAGES.map(s => [s, 0]));
    list(applicants).forEach(a => { if (a.status in counts) counts[a.status]++; });
    return counts;
  }

  // "from" after "to" can never match, so the page warns instead of showing an empty list
  function invalidRange(from, to) {
    return Boolean(from && to && from > to);
  }

  return { STAGES, appliedDay, filterApplicants, sortApplicants, countByStatus, invalidRange };
});
