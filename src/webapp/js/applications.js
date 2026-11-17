/* My Applications page: stats, charts and the application history.
   GET /api/applications only returns the logged-in job seeker's applications
   (the server gets the user from the session). Logic is in analytics.js and charts.js. */
(function () {
  if (!window.CC) return;                       // shell.js is redirecting to login
  const { esc, api, colorFor } = CC;
  const A = window.CCAnalytics, C = window.CCCharts;
  const $ = id => document.getElementById(id);

  const STEPS = ['Applied', 'Under Review', 'Interview', 'Offered'];
  let apps = [];                                 // every application, as loaded
  let range = '12';                              // months shown in the time chart

  // ---------- History cards (from the Sprint 2 tracker) ----------

  // One bar per stage. A rejection replaces the last stage and marks the stage it ended at.
  function steps(status) {
    const rejected = status === 'Rejected';
    return `<ol class="steps" aria-label="Application progress: ${esc(status)}">${STEPS.map((step, i) => {
      if (rejected && i === 3) return '<li class="no">Rejected</li>';
      const reached = !rejected && i <= STEPS.indexOf(status);
      const cls = reached ? (step === status ? 'done now' : 'done') : '';
      return `<li class="${cls}"${step === status ? ' aria-current="step"' : ''}>${step}</li>`;
    }).join('')}</ol>`;
  }

  const day = value => {
    const d = A.parseDate(value);
    return d ? d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' }) : 'date unknown';
  };

  function card(app) {
    const title = A.titleOf(app);
    const company = app.companyName || '';
    const status = A.normalizeStatus(app.status);
    const shownStatus = status === A.OTHER.label && app.status ? String(app.status) : status;
    const [c1, c2] = colorFor(company || title);
    return `<article class="card app">
        <div class="app-top">
            <div class="logo" aria-hidden="true" style="--c1:${c1};--c2:${c2};width:46px;height:46px;font-size:1.1rem">${esc(String(company || title)[0])}</div>
            <div class="who">
                <h3><a href="job.html?id=${encodeURIComponent(app.postId)}">${esc(title)}</a></h3>
                <small>${company ? esc(company) + ' · ' : ''}Applied ${day(app.appliedAt)} · Updated ${day(app.updatedAt)}</small>
            </div>
            <span class="status ${A.statusInfo(status).cls}">${esc(shownStatus)}</span>
        </div>
        ${steps(status)}
    </article>`;
  }

  // ---------- Dashboard ----------

  function renderStats() {
    const { total, counts } = A.summarize(apps);
    const tiles = [{ label: 'Total', cls: 'total', count: total }]
      .concat(A.STATUSES.map(s => ({ label: s.label, cls: s.cls, count: counts[s.label] })));
    if (counts[A.OTHER.label] > 0) tiles.push({ label: 'Other', cls: 'other', count: counts[A.OTHER.label] });
    $('stats').innerHTML = tiles.map(t =>
      `<li class="stat ${t.cls}"><b>${t.count}</b><span>${esc(t.label)}</span></li>`).join('');
  }

  function renderStatusChart() {
    const segments = A.statusBreakdown(apps);
    $('status-chart').innerHTML = C.donut(segments, apps.length) + C.legend(segments);
    $('status-table').innerHTML = C.dataTable('Applications by status', ['Status', 'Applications', 'Share'],
      segments.map(s => [s.label, s.count, s.percent + '%']));
  }

  function renderTimeChart() {
    const months = A.monthlyCounts(apps, range === 'all' ? 'all' : Number(range));
    $('time-chart').innerHTML = C.monthBars(months);
    $('time-table').innerHTML = C.dataTable('Applications submitted per month', ['Month', 'Applications'],
      months.map(m => [m.label, m.count]));
    const { undated } = A.summarize(apps);
    $('undated-note').textContent = undated
      ? `${undated} application${undated === 1 ? ' has' : 's have'} no valid date and ${undated === 1 ? "isn't" : "aren't"} shown in this chart.`
      : '';
    $('undated-note').classList.toggle('hidden', !undated);
  }

  // ---------- History list ----------

  function currentFilters() {
    return { status: $('status-filter').value, query: $('q').value, sort: $('sort').value };
  }

  function renderHistory() {
    const f = currentFilters();
    const shown = A.sortApplications(A.filterApplications(apps, f), f.sort);
    $('apps').innerHTML = shown.map(card).join('');
    $('no-match').classList.toggle('hidden', shown.length > 0);
    $('result-count').textContent = shown.length === apps.length
      ? (apps.length === 1 ? 'Showing 1 application' : `Showing all ${apps.length} applications`)
      : `Showing ${shown.length} of ${apps.length} applications`;
  }

  function fillStatusFilter() {
    const { counts } = A.summarize(apps);
    const keep = $('status-filter').value || 'All';
    const opts = [['All', 'All statuses (' + apps.length + ')']]
      .concat(A.STATUSES.map(s => [s.label, `${s.label} (${counts[s.label]})`]));
    if (counts[A.OTHER.label] > 0) opts.push(['Other', `Other (${counts[A.OTHER.label]})`]);
    $('status-filter').innerHTML = opts.map(([v, t]) => `<option value="${esc(v)}">${esc(t)}</option>`).join('');
    $('status-filter').value = opts.some(o => o[0] === keep) ? keep : 'All';
  }

  function clearFilters() {
    $('q').value = '';
    $('status-filter').value = 'All';
    $('sort').value = 'newest';
    renderHistory();
  }

  // ---------- Loading ----------

  function show(state) {                           // 'loading' | 'ready' | 'empty' | 'error'
    $('loading').classList.toggle('hidden', state !== 'loading');
    $('summary').classList.toggle('hidden', state !== 'ready' && state !== 'empty');
    $('charts').classList.toggle('hidden', state !== 'ready');
    $('history').classList.toggle('hidden', state !== 'ready');
    $('empty').classList.toggle('hidden', state !== 'empty');
    if (state !== 'error') $('list-message').classList.add('hidden');
  }

  function showError(err) {
    const el = $('list-message');
    el.innerHTML = err.status === 403
      ? 'This dashboard is for job seekers. Recruiters can review applicants from <a href="recruiterhome.html">the recruiter home page</a>.'
      : esc("Couldn't load your applications. " + (err.message || '')) + ' <button type="button" class="btn" id="retry">Try again</button>';
    el.classList.remove('hidden');
    const retry = $('retry');
    if (retry) retry.addEventListener('click', load);
  }

  async function load() {
    const btn = $('refresh');
    btn.disabled = true;
    show('loading');
    try {
      const data = await api('/api/applications');
      apps = Array.isArray(data) ? data : [];
      renderStats();
      if (apps.length === 0) { show('empty'); return; }
      fillStatusFilter();
      renderStatusChart();
      renderTimeChart();
      renderHistory();
      show('ready');
    } catch (err) {
      apps = [];
      show('error');
      showError(err);
    } finally {
      btn.disabled = false;
    }
  }

  // ---------- Events ----------

  $('refresh').addEventListener('click', load);
  $('q').addEventListener('input', renderHistory);
  $('status-filter').addEventListener('change', renderHistory);
  $('sort').addEventListener('change', renderHistory);
  $('clear').addEventListener('click', clearFilters);
  $('clear-2').addEventListener('click', clearFilters);
  $('range').addEventListener('click', e => {
    const b = e.target.closest('button[data-range]');
    if (!b) return;
    range = b.dataset.range;
    $('range').querySelectorAll('button').forEach(x => {
      x.classList.toggle('on', x === b);
      x.setAttribute('aria-pressed', x === b);
    });
    renderTimeChart();
  });

  load();
})();
