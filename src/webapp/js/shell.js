/* Shared by the job seeker pages: top bar, icons, toasts and the CC.api() fetch helper.
   Headline, About, skills and location are saved through /api/profile; only the
   profile photo and cover photo still live in localStorage (no image storage yet). */
(function () {
  const user = localStorage.getItem('userName');
  if (!user) { location.replace('index.html'); return; }

  const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

  const PATHS = {
    search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>',
    doc: '<path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/>',
    chart: '<path d="M4 20v-8M10 20V4M16 20V8M22 20H2"/>',
    star: '<path d="m12 3 2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z"/>',
    thumb: '<path d="M7 11v9H4v-9zM7 11l4-8a2 2 0 0 1 2 2v4h6a2 2 0 0 1 2 2.3l-1.3 7A2 2 0 0 1 17.7 20H7"/>',
    comment: '<path d="M21 12a8 8 0 0 1-11.6 7.1L4 20.5l1.4-4.6A8 8 0 1 1 21 12z"/>',
    share: '<path d="M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7M16 6l-4-4-4 4M12 2v13"/>',
    bookmark: '<path d="M6 3h12v18l-6-4-6 4z"/>',
    image: '<rect x="3" y="3" width="18" height="18" rx="3"/><circle cx="9" cy="9" r="2"/><path d="m21 15-5-5L5 21"/>',
    pin: '<path d="M12 21s7-6.2 7-11.5a7 7 0 1 0-14 0C5 14.8 12 21 12 21z"/><circle cx="12" cy="9.5" r="2.5"/>',
    camera: '<path d="M4 8h3l2-3h6l2 3h3a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1z"/><circle cx="12" cy="13.5" r="3.5"/>',
    pencil: '<path d="M4 20h4L19 9a2.8 2.8 0 0 0-4-4L4 16z"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    check: '<path d="m5 12.5 4.5 4.5L19 7.5"/>',
    chev: '<path d="m6 9 6 6 6-6"/>',
    briefcase: '<rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2M3 13h18"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    cash: '<rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2.5"/><path d="M6 12h.01M18 12h.01"/>',
    calendar: '<rect x="3" y="5" width="18" height="16" rx="3"/><path d="M3 10h18M8 3v4M16 3v4"/>',
    users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0M16 4.6a3.5 3.5 0 0 1 0 6.8M18 14.2A6.5 6.5 0 0 1 21.5 20"/>',
    x: '<path d="M6 6l12 12M18 6 6 18"/>',
    send: '<path d="M22 2 11 13M22 2l-7 20-4-9-9-4z"/>'
  };
  const icon = (n, cls = '') => `<svg class="i ${cls}" viewBox="0 0 24 24" aria-hidden="true">${PATHS[n]}</svg>`;
  const initials = n => n.trim().split(/\s+/).slice(0, 2).map(w => w[0]).join('').toUpperCase() || '?';

  // ---- per-user photos (avatar, cover); the rest of the profile is in the database ----
  const KEY = 'cc_profile_' + user.toLowerCase();
  const store = {
    get() { try { return JSON.parse(localStorage.getItem(KEY)) || {}; } catch { return {}; } },
    set(patch) {
      try { localStorage.setItem(KEY, JSON.stringify({ ...store.get(), ...patch })); return true; }
      catch { return false; }                    // storage full or blocked
    }
  };
  function applyAvatars() {
    const p = store.get();
    document.querySelectorAll('[data-me]').forEach(el => {
      if (p.avatar) { el.style.backgroundImage = 'url("' + p.avatar + '")'; el.textContent = ''; }
      else { el.style.backgroundImage = ''; el.textContent = initials(user); }
    });
  }
  // Shared by the home and profile pages so both show the same completion score.
  // profile is the GET /api/profile response (or {} while it hasn't loaded).
  function strength(profile = {}) {
    const p = { ...store.get(), ...profile, skills: profile.skills && profile.skills.length };
    const steps = [
      ['avatar', 'Add a profile photo'], ['cover', 'Add a cover photo'],
      ['headline', 'Write a headline'], ['about', 'Write your About section'], ['skills', 'List your skills']
    ];
    const missing = steps.filter(([k]) => !p[k]);
    return { pct: Math.round((steps.length - missing.length) / steps.length * 100), missing: missing.map(m => m[1]) };
  }
  // Downscale an uploaded image so it fits comfortably in localStorage
  function readImage(file, maxW) {
    return new Promise((resolve, reject) => {
      const url = URL.createObjectURL(file), img = new Image();
      img.onload = () => {
        const s = Math.min(1, maxW / img.width), c = document.createElement('canvas');
        c.width = Math.round(img.width * s); c.height = Math.round(img.height * s);
        c.getContext('2d').drawImage(img, 0, 0, c.width, c.height);
        URL.revokeObjectURL(url);
        resolve(c.toDataURL('image/jpeg', 0.85));
      };
      img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('That file is not a readable image.')); };
      img.src = url;
    });
  }

  function toast(msg) {
    let box = document.querySelector('.toasts');
    if (!box) { box = document.createElement('div'); box.className = 'toasts'; box.setAttribute('role', 'status'); document.body.appendChild(box); }
    const t = document.createElement('div');
    t.className = 'toast'; t.textContent = msg; box.appendChild(t);
    setTimeout(() => t.remove(), 2800);
  }

  // fetch() for the JSON API: resolves with the parsed body, rejects with an Error carrying
  // the server's message and .status. A 401 means the session ended, so go back to login.
  async function api(url, options = {}) {
    const opts = { credentials: 'same-origin', ...options };
    if (opts.body && typeof opts.body !== 'string') {
      opts.headers = { 'Content-Type': 'application/json', ...opts.headers };
      opts.body = JSON.stringify(opts.body);
    }
    const res = await fetch(url, opts);
    const data = await res.json().catch(() => ({}));
    if (res.status === 401) {
      localStorage.removeItem('userName');
      location.replace('index.html');
    }
    if (!res.ok) {
      const err = new Error(data.message || 'Something went wrong (' + res.status + ').');
      err.status = res.status;
      throw err;
    }
    return data;
  }

  // The job search API may name a few fields differently; read them in one place
  const jobView = j => ({
    id: j.postId ?? j.id,
    title: j.title || 'Untitled role',
    company: j.companyName || 'Company',
    location: j.location || 'Location not specified',
    category: j.industry || j.category || j.department || '',
    jobType: j.jobType || '',
    salary: j.salary || '',
    skills: (j.requiredSkills || '').split(/[,\n]/).map(s => s.trim()).filter(Boolean),
    description: j.description || '',
    deadline: j.applicationDeadline || j.deadline || null
  });
  // "2026-11-30" -> "Nov 30, 2026" (parsed as a local date, not UTC midnight)
  const fmtDate = d => d ? new Date(String(d).slice(0, 10) + 'T00:00').toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' }) : '';
  const COLORS = [['#7c3aed', '#a78bfa'], ['#0891b2', '#6366f1'], ['#ea580c', '#f59e0b'], ['#db2777', '#fb7185']];
  const colorFor = key => COLORS[[...String(key)].reduce((n, c) => n + c.charCodeAt(0), 0) % COLORS.length];

  // ---- top bar ----
  const page = location.pathname.split('/').pop() || 'home.html';
  const cur = (...p) => p.includes(page) ? ' active' : '';
  document.body.insertAdjacentHTML('afterbegin', `
  <header class="topbar">
    <a class="brand" href="home.html">
      <span class="mark" aria-hidden="true">C</span>
      <span><b>CareerConnect</b><small>Welcome, <span id="username"></span></small></span>
    </a>
    <nav class="nav" aria-label="Main">
      <a href="home.html" class="${cur('home.html').trim()}">Home</a>
      <a href="jobs.html" class="${cur('jobs.html', 'job.html').trim()}">Postings</a>
      <div class="dd${cur('resume.html', 'applications.html')}">
        <button type="button" class="dd-btn" aria-haspopup="true" aria-expanded="false">My Tools ${icon('chev')}</button>
        <div class="dd-menu"><div class="dd-panel">
          <a href="resume.html"><span class="ic g1">${icon('doc')}</span>My CVs &amp; Cover Letters</a>
          <a href="#" data-soon="Favourited Postings"><span class="ic g3">${icon('star')}</span>Favourited Postings<em>Soon</em></a>
          <a href="applications.html"><span class="ic g2">${icon('chart')}</span>My Applications</a>
        </div></div>
      </div>
      <a href="#" data-soon="Messages">Messages</a>
      <a href="#" data-soon="Settings">Settings</a>
    </nav>
    <a class="me${cur('profile.html')}" href="profile.html" aria-label="Your profile" title="Your profile"><span class="av" data-me></span></a>
  </header>`);
  document.getElementById('username').textContent = user;

  const dd = document.querySelector('.dd'), ddBtn = dd.querySelector('.dd-btn');
  const setOpen = o => { dd.classList.toggle('open', o); ddBtn.setAttribute('aria-expanded', o); };
  ddBtn.addEventListener('click', e => { e.stopPropagation(); setOpen(!dd.classList.contains('open')); });
  document.addEventListener('click', e => { if (!dd.contains(e.target)) setOpen(false); });
  document.addEventListener('keydown', e => { if (e.key === 'Escape') setOpen(false); });
  document.addEventListener('click', e => {
    const a = e.target.closest('[data-soon]');
    if (a) { e.preventDefault(); setOpen(false); toast((a.dataset.soon || 'This') + ' is coming soon.'); }
  });

  window.CC = { user, esc, icon, initials, store, toast, applyAvatars, strength, readImage, api, jobView, fmtDate, colorFor };
  applyAvatars();
})();