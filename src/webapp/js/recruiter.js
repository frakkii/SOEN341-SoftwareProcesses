/* Shared by every recruiter page. Load it with a plain <script src="js/recruiter.js"></script>
   right after <body> opens. It
   1) checks with the server that the recruiter really is logged in (localStorage alone can outlive
      the server session, which is what caused "User is not logged in" when creating a posting),
   2) renders the top bar, and 3) exposes RC.* helpers (api, esc, toast, prefs, ready). */
(function () {
  const PREF_KEY = 'rc_prefs';
  const defaults = { dark: false, defaultJobType: 'Full-time', notifyApplicants: true, notifyWeekly: false };
  const prefs = {
    get() { try { return { ...defaults, ...JSON.parse(localStorage.getItem(PREF_KEY)) }; } catch { return { ...defaults }; } },
    set(patch) { try { localStorage.setItem(PREF_KEY, JSON.stringify({ ...prefs.get(), ...patch })); } catch { /* storage blocked */ } apply(); }
  };
  function apply() { document.documentElement.classList.toggle('rc-dark', !!prefs.get().dark); }
  apply();
  document.body.classList.add('rc');

  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  function toast(msg) {
    let box = document.querySelector('.toasts');
    if (!box) { box = document.createElement('div'); box.className = 'toasts'; box.setAttribute('role', 'status'); document.body.appendChild(box); }
    const t = document.createElement('div'); t.className = 'toast'; t.textContent = msg; box.appendChild(t);
    setTimeout(() => t.remove(), 2600);
  }
  function toLogin() {
    localStorage.removeItem('userName'); localStorage.removeItem('userRole');
    location.replace('index.html?expired=1');
  }
  // fetch() for the JSON API; a 401 means the session ended, so go back to the login page
  async function api(url, options = {}) {
    const opts = { credentials: 'same-origin', ...options };
    if (opts.body && typeof opts.body !== 'string') {
      opts.headers = { 'Content-Type': 'application/json', ...opts.headers };
      opts.body = JSON.stringify(opts.body);
    }
    const res = await fetch(url, opts);
    const data = await res.json().catch(() => ({}));
    if (res.status === 401) { toLogin(); throw new Error('Your session has expired. Please log in again.'); }
    if (!res.ok) { const e = new Error(data.message || 'Something went wrong (' + res.status + ').'); e.status = res.status; throw e; }
    return data;
  }

  const page = location.pathname.split('/').pop() || 'recruiterhome.html';
  const cur = (...p) => p.includes(page) ? ' class="active"' : '';
  const name = (localStorage.getItem('userName') || 'Recruiter').trim();
  document.body.insertAdjacentHTML('afterbegin', `
  <header class="rc-nav"><div class="rc-nav-in">
    <a class="rc-brand" href="recruiterhome.html" aria-label="CareerConnect Recruiter home">
      <span class="rc-mark">C</span><span><b>CareerConnect</b><small>Recruiter</small></span></a>
    <nav class="rc-links" aria-label="Primary">
      <a href="recruiterhome.html"${cur('recruiterhome.html')}>Home</a>
      <a href="recruiterpostings.html"${cur('recruiterpostings.html', 'recruiterapplicants.html', 'createposting.html')}>Jobs</a>
      <a href="#" data-soon="Messages">Messages</a>
      <a href="recruitersettings.html"${cur('recruitersettings.html')}>Settings</a>
    </nav>
    <div class="rc-right">
      <a class="rc-cta" href="createposting.html">Post a job</a>
      <div class="rc-me">
        <button type="button" aria-haspopup="true" aria-expanded="false" aria-label="Account menu">
          <span class="rc-av" id="profile-badge">${esc(name.charAt(0).toUpperCase())}</span>&#9662;</button>
        <div class="rc-menu">
          <div class="who"><b id="rc-name">${esc(name)}</b><span id="rc-company"></span></div>
          <a href="recruitersettings.html">Settings</a>
          <a href="#" id="logout">Sign out</a>
        </div>
      </div>
    </div>
  </div></header>`);

  const me = document.querySelector('.rc-me'), btn = me.querySelector('button');
  btn.addEventListener('click', e => { e.stopPropagation(); me.classList.toggle('open'); btn.setAttribute('aria-expanded', me.classList.contains('open')); });
  document.addEventListener('click', e => { if (!me.contains(e.target)) me.classList.remove('open'); });
  document.addEventListener('click', e => { const a = e.target.closest('[data-soon]'); if (a) { e.preventDefault(); toast(a.dataset.soon + ' is coming soon.'); } });
  document.head.appendChild(Object.assign(document.createElement('script'), { src: 'js/logout.js' }));

  // Resolves with the logged-in recruiter, or sends the browser to the login page
  const ready = fetch('/api/auth/me', { credentials: 'same-origin' })
    .then(async res => {
      const data = await res.json().catch(() => ({}));
      if (res.status === 401 || (res.ok && data.role !== 'Recruiter')) { toLogin(); return new Promise(() => {}); }
      if (!res.ok) { return { name: localStorage.getItem('userName') || 'Recruiter', surname: '', email: '', companyName: '' }; }
      const full = (data.name + ' ' + data.surname).trim();
      localStorage.setItem('userName', data.name);
      document.getElementById('rc-name').textContent = full;
      document.getElementById('rc-company').textContent = data.companyName || '';
      document.getElementById('profile-badge').textContent = (data.name || 'R').charAt(0).toUpperCase();
      return data;
    });

  window.RC = { api, esc, toast, prefs, ready };
})();