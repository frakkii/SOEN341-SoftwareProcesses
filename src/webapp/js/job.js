/*  job detail page (job.html?id=42). Loads GET /api/jobs/{id}; Apply sends
   POST /api/applications and shows the confirmation. If the job seeker already
   applied, the button is replaced by their current status. */
(function () {
  if (!window.CC) return;                       // redirecting to login
  const { esc, icon, api, jobView, fmtDate, colorFor } = CC;
  const $ = s => document.querySelector(s);
  const id = new URLSearchParams(location.search).get('id');
  const btn = $('#apply-btn'), msg = $('#apply-message');

  // Back to the search the user came from, if they came from one
  if (document.referrer.includes('/jobs.html')) $('#back').href = document.referrer;

  function missing(text) {
    $('#missing').textContent = text;
    $('#missing').classList.remove('hidden');
  }

  function show(type, html) {
    msg.className = 'message ' + type;
    msg.innerHTML = html;
  }

  function applied(application) {
    btn.disabled = true;
    btn.innerHTML = `${icon('check')}Applied`;
    show('success', `Status: <b>${esc(application.status)}</b>. <a href="applications.html">Track your applications</a>`);
  }

  function paint(raw) {
    const j = jobView(raw), [c1, c2] = colorFor(j.company);
    document.title = `CareerConnect - ${j.title}`;
    $('#job-card').style.cssText = `--c1:${c1};--c2:${c2}`;
    $('#j-logo').textContent = j.company[0];
    $('#j-title').textContent = j.title;
    $('#j-company').textContent = j.company;
    $('#j-chips').innerHTML = [
      `<span class="chip">${icon('pin')}${esc(j.location)}</span>`,
      j.category && `<span class="chip">${icon('briefcase')}${esc(j.category)}</span>`,
      j.jobType && `<span class="chip">${icon('clock')}${esc(j.jobType)}</span>`,
      j.salary && `<span class="chip pay">${icon('cash')}${esc(j.salary)}</span>`
    ].filter(Boolean).join('');
    $('#j-description').textContent = j.description || 'No description provided.';
    $('#j-skills').innerHTML = j.skills.map(t => `<span class="tag">${esc(t)}</span>`).join('');
    $('#j-skills-sec').classList.toggle('hidden', !j.skills.length);
    $('#j-deadline').innerHTML = j.deadline ? `${icon('calendar')} Apply by ${esc(fmtDate(j.deadline))}` : '';
    btn.innerHTML = `${icon('send')}Submit application`;
    $('#job').classList.remove('hidden');
  }

  btn.addEventListener('click', async () => {
    btn.disabled = true;
    msg.className = 'message hidden';
    try {
      const data = await api('/api/applications', { method: 'POST', body: { postId: Number(id) } });
      applied(data.application);
      show('success', `${esc(data.message)} Status: <b>${esc(data.application.status)}</b>. <a href="applications.html">Track it here</a>`);
    } catch (err) {
      btn.disabled = false;
      if (err.status === 409) { btn.disabled = true; btn.innerHTML = `${icon('check')}Already applied`; }
      const fix = /resume/i.test(err.message) ? ' <a href="resume.html">Upload your CV</a>' : '';
      show('error', esc(err.message) + fix);
    }
  });

  (async function load() {
    if (!id || !/^\d+$/.test(id)) return missing('This job link is missing its job number.');
    try {
      paint(await api('/api/jobs/' + id));
    } catch (err) {
      return missing(err.status === 404
        ? 'This job doesn’t exist or is no longer accepting applications.'
        : 'Couldn’t load this job. ' + err.message);
    }
    // Already applied? Show the status instead of the button.
    try {
      const mine = await api('/api/applications');
      const existing = mine.find(a => String(a.postId) === id);
      if (existing) applied(existing);
    } catch (err) {
      if (err.status === 403) { btn.disabled = true; show('error', 'Only job seeker accounts can apply.'); }
    }
  })();
})();
