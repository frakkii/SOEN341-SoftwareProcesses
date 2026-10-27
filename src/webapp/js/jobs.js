/*  job search page. Results come from GET /api/jobs?keyword=&location=&industry=
   (open postings whose deadline hasn't passed). The search is kept in the URL so it
   survives a refresh and can be shared. */
(function () {
  if (!window.CC) return;                       // redirecting to login
  const { esc, icon, api, jobView, fmtDate, colorFor } = CC;
  const $ = s => document.querySelector(s);
  const form = $('#search'), results = $('#results'), empty = $('#empty'), count = $('#count');
  const FIELDS = ['keyword', 'location', 'industry'];

  $('#search-btn').innerHTML = `${icon('search')}Search`;

  function card(raw) {
    const j = jobView(raw), [c1, c2] = colorFor(j.company);
    const chips = [
      `<span class="chip">${icon('pin')}${esc(j.location)}</span>`,
      j.category && `<span class="chip">${icon('briefcase')}${esc(j.category)}</span>`,
      j.jobType && `<span class="chip">${icon('clock')}${esc(j.jobType)}</span>`,
      j.salary && `<span class="chip pay">${icon('cash')}${esc(j.salary)}</span>`
    ].filter(Boolean).join('');
    const summary = j.description.length > 220 ? j.description.slice(0, 220).trimEnd() + '…' : j.description;
    return `<article class="card post job" style="--c1:${c1};--c2:${c2}">
      <div class="p-top"><div class="logo" aria-hidden="true">${esc(j.company[0])}</div>
        <div class="who"><h3>${esc(j.title)}</h3><small>${esc(j.company)}${j.deadline ? ' · Apply by ' + esc(fmtDate(j.deadline)) : ''}</small></div></div>
      <div class="chips">${chips}</div>
      ${summary ? `<p class="p-body">${esc(summary)}</p>` : ''}
      ${j.skills.length ? `<div class="tags">${j.skills.slice(0, 6).map(t => `<span class="tag">${esc(t)}</span>`).join('')}</div>` : ''}
      <div class="actions"><a class="btn primary" href="job.html?id=${encodeURIComponent(j.id)}">View details</a></div>
    </article>`;
  }

  async function search() {
    const params = new URLSearchParams();
    FIELDS.forEach(f => { const v = form.elements[f].value.trim(); if (v) params.set(f, v); });
    history.replaceState(null, '', params.toString() ? '?' + params : location.pathname);
    $('#clear').classList.toggle('hidden', !params.toString());
    count.textContent = 'Searching…';

    try {
      const jobs = await api('/api/jobs' + (params.toString() ? '?' + params : ''));
      results.innerHTML = jobs.map(card).join('');
      count.textContent = jobs.length === 1 ? '1 open job' : `${jobs.length} open jobs`;
      empty.textContent = params.toString()
        ? 'No open jobs match your search. Try fewer words or clear the filters.'
        : 'There are no open jobs right now. Check back soon.';
      empty.classList.toggle('hidden', jobs.length > 0);
    } catch (err) {
      results.innerHTML = '';
      count.textContent = 'Jobs unavailable';
      empty.textContent = err.status === 404
        ? 'Job search isn’t available on this server yet.'
        : 'Couldn’t load jobs. ' + err.message;
      empty.classList.remove('hidden');
    }
  }

  // Start from the URL so a refreshed or shared search shows the same results
  const start = new URLSearchParams(location.search);
  FIELDS.forEach(f => { form.elements[f].value = start.get(f) || ''; });

  form.addEventListener('submit', e => { e.preventDefault(); search(); });
  $('#clear').onclick = () => { form.reset(); search(); };
  search();
})();
