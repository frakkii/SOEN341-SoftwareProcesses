/*  home feed.
   Job cards come from GET /api/jobs (the newest open postings). POSTS below is sample
   social content, and posts created in the composer, likes and comments are not saved yet. */
(function () {
  if (!window.CC) return;                       // redirecting to login
  const { user, esc, icon, initials, store, toast, readImage, strength, api, jobView, fmtDate, colorFor } = CC;
  const $ = s => document.querySelector(s);
  const feed = $('#feed');

  const POSTS = [
    { id: 2, kind: 'post', who: 'Sofia Marchetti', role: 'Talent partner at Lumen Labs', av: 'c2', ago: '3h',
      text: 'Three things that make a junior application stand out:\n1. A project you can demo in two minutes.\n2. A CV that fits on one page.\n3. A short note on why this team.',
      likes: 112, comments: 18 },
    { id: 4, kind: 'post', who: 'Dev Patel', role: 'Computer science student', av: 'c1', ago: '8h',
      text: 'I just accepted my first co-op offer! Huge thanks to everyone who reviewed my CV and ran mock interviews with me. Paying it forward this Friday:',
      event: { title: 'Free CV review night', when: 'Friday, 6 PM, online' }, likes: 341, comments: 57 }
  ];
  const MAX_JOBS = 6;

  // A GET /api/jobs result in the shape render() uses
  function feedJob(raw) {
    const j = jobView(raw);
    return { id: 'job-' + j.id, postId: j.id, kind: 'job', co: j.company, c: colorFor(j.company), title: j.title,
      loc: j.location, type: j.category || j.jobType, pay: j.salary, sub: j.deadline ? 'apply by ' + fmtDate(j.deadline) : 'now hiring',
      text: j.description, tags: j.skills.slice(0, 5), likes: 0, comments: 0 };
  }

  const plural = (n, w) => `${n} ${w}${n === 1 ? '' : 's'}`;
  const actBtn = (act, ic, label, extra = '') =>
    `<button type="button" class="act ${extra}" data-act="${act}"${act === 'like' || act === 'save' ? ' aria-pressed="false"' : ''}>${icon(ic)}<span>${label}</span></button>`;

  function render(it) {
    const isJob = it.kind === 'job';
    const head = isJob
      ? `<div class="logo" aria-hidden="true">${esc(it.co[0])}</div>
         <div class="who"><h3>${esc(it.title)}</h3><small>${esc(it.co)}, ${esc(it.sub)}</small></div>`
      : `<span class="av ${it.me ? '' : esc(it.av || 'c1')}" ${it.me ? 'data-me' : ''}>${it.me ? '' : esc(initials(it.who))}</span>
         <div class="who"><span class="name">${esc(it.who)}</span>${it.lead ? '<span class="badge">Job lead</span>' : ''}<small>${esc(it.role)}, ${esc(it.ago === 'Just now' ? it.ago : it.ago + ' ago')}</small></div>`;
    const chips = isJob ? `<div class="chips">
        <span class="chip">${icon('pin')}${esc(it.loc)}</span>${it.type ? `<span class="chip">${icon('briefcase')}${esc(it.type)}</span>` : ''}
        ${it.pay ? `<span class="chip pay">${icon('cash')}${esc(it.pay)}</span>` : ''}</div>` : '';
    const tags = isJob ? `<div class="tags">${it.tags.map(t => `<span class="tag">${esc(t)}</span>`).join('')}</div>` : '';
    const event = it.event ? `<div class="event">${icon('calendar')}<div><b>${esc(it.event.title)}</b><small>${esc(it.event.when)}</small></div></div>` : '';
    const img = it.img ? `<img class="p-img" src="${it.img}" alt="Photo attached to the post">` : '';
    return `<article class="card post ${isJob ? 'job' : ''}" id="post-${esc(it.id)}" data-kind="${it.kind}"${isJob ? ` data-post-id="${esc(it.postId)}"` : ''}
        style="${isJob ? `--c1:${it.c[0]};--c2:${it.c[1]}` : ''}">
      <div class="p-top">${head}</div>
      ${chips}<p class="p-body">${esc(it.text)}</p>${tags}${event}${img}
      <div class="counts"><span class="n-likes" data-base="${it.likes || 0}">${plural(it.likes || 0, 'like')}</span>
        <span class="n-cm" data-base="${it.comments || 0}">${plural(it.comments || 0, 'comment')}</span></div>
      <div class="actions">
        ${actBtn('like', 'thumb', 'Like')}${actBtn('comment', 'comment', 'Comment')}${actBtn('share', 'share', 'Share')}
        ${isJob ? actBtn('save', 'bookmark', 'Save', 'save') : ''}
        ${isJob ? `<button type="button" class="btn primary apply" data-act="apply">${icon('send')}Apply</button>` : ''}
      </div>
      <div class="cm-box hidden">
        <form class="cm-form"><input name="c" maxlength="400" placeholder="Write a comment" aria-label="Write a comment" autocomplete="off">
          <button class="btn" type="submit">Send</button></form>
      </div>
    </article>`;
  }

  feed.innerHTML = POSTS.map(render).join('');

  // Real jobs first, with the sample posts between them
  api('/api/jobs').then(jobs => {
    const items = jobs.slice(0, MAX_JOBS).map(feedJob);
    const mixed = items.flatMap((job, i) => POSTS[i] ? [job, POSTS[i]] : [job]).concat(POSTS.slice(items.length));
    feed.innerHTML = mixed.map(render).join('');
    if (!items.length) feed.insertAdjacentHTML('afterbegin', '<p class="card empty">No open jobs right now. <a href="jobs.html">Search all postings</a></p>');
    paintCompanies(jobs);
    CC.applyAvatars();
  }).catch(() => {
    feed.insertAdjacentHTML('afterbegin', '<p class="card empty">Job listings could not be loaded. <a href="jobs.html">Try the job search</a></p>');
    paintCompanies([]);
  });

  // ---- left + right rails ----
  const TILES = [
    ['t1', 'search', 'Find jobs', 'Search open postings', 'jobs.html'],
    ['t2', 'doc', 'My CVs', 'Manage your resumes', 'resume.html'],
    ['t3', 'chart', 'Applications', 'Track your status', 'applications.html'],
    ['t4', 'star', 'Favourites', 'Coming soon', null, 'Favourites']
  ];
  $('#tiles').innerHTML = TILES.map(([cls, ic, t, s, href, soon]) => href
    ? `<a class="tile ${cls}" href="${href}">${icon(ic)}<span><b>${t}</b><small>${s}</small></span></a>`
    : `<button type="button" class="tile ${cls}" data-soon="${soon}">${icon(ic)}<span><b>${t}</b><small>${s}</small></span></button>`).join('');

  // Companies with the most open postings
  function paintCompanies(jobs) {
    const byCompany = new Map();
    jobs.map(jobView).forEach(j => byCompany.set(j.company, (byCompany.get(j.company) || 0) + 1));
    const top = [...byCompany].sort((a, b) => b[1] - a[1]).slice(0, 3);
    $('#companies').innerHTML = top.length ? top.map(([n, count]) => {
      const [c1, c2] = colorFor(n);
      return `<li><span class="logo" style="--c1:${c1};--c2:${c2}">${esc(n[0])}</span>
        <div><b>${esc(n)}</b><small>${count === 1 ? '1 open role' : count + ' open roles'}</small></div>
        <a class="follow" href="jobs.html?keyword=${encodeURIComponent(n)}">View</a></li>`;
    }).join('') : '<li class="muted">No companies are hiring yet.</li>';
  }
  $('#companies').addEventListener('click', e => {
    const b = e.target.closest('.follow'); if (!b) return;
    const on = b.getAttribute('aria-pressed') !== 'true';
    b.setAttribute('aria-pressed', on); b.textContent = on ? 'Following' : 'Follow';
  });

  let profile = {};            // GET /api/profile response once loaded
  function paintProfile() {
    const p = profile, s = strength(profile);
    $('#me-name').textContent = user;
    $('#me-headline').textContent = p.headline || 'Add a headline so recruiters know what you do';
    $('#strength-bar').firstElementChild.style.setProperty('--pct', s.pct + '%');
    $('#strength-bar').setAttribute('aria-valuenow', s.pct);
    $('#strength-text').textContent = s.pct === 100 ? 'Your profile is complete.' : `${s.pct}% complete. Finish it to get noticed.`;
    const todo = [['Upload your CV', 'resume.html', 'doc'], ...s.missing.map(m => [m, 'profile.html', 'plus'])];
    $('#todo').innerHTML = todo.map(([t, h, ic]) => `<li><a href="${h}">${icon(ic)}${esc(t)}</a></li>`).join('');
  }
  paintProfile();
  api('/api/profile').then(p => { profile = p; paintProfile(); }).catch(() => {});
  $('#pick-photo').innerHTML = `${icon('image')}Photo`;
  $('#pick-lead').innerHTML = `${icon('briefcase')}Job lead`;
  $('#post-btn').innerHTML = `${icon('send')}Post`;

  // ---- feed interactions (event delegation) ----
  feed.addEventListener('click', e => {
    const b = e.target.closest('[data-act]'); if (!b) return;
    const post = b.closest('.post'), act = b.dataset.act;
    if (act === 'like') {
      const on = b.classList.toggle('on'); b.setAttribute('aria-pressed', on);
      const n = post.querySelector('.n-likes'); n.textContent = plural(+n.dataset.base + (on ? 1 : 0), 'like');
    } else if (act === 'save') {
      const on = b.classList.toggle('on'); b.setAttribute('aria-pressed', on);
      b.lastElementChild.textContent = on ? 'Saved' : 'Save';
      toast(on ? 'Saved to your favourites.' : 'Removed from your favourites.');
    } else if (act === 'comment') {
      const box = post.querySelector('.cm-box'); box.classList.toggle('hidden');
      if (!box.classList.contains('hidden')) box.querySelector('input').focus();
    } else if (act === 'share') {
      const link = location.origin + location.pathname + '#' + post.id;
      (navigator.clipboard ? navigator.clipboard.writeText(link) : Promise.reject())
        .then(() => toast('Link copied.'), () => toast('Copy this link: ' + link));
    } else if (act === 'apply') {
      location.href = 'job.html?id=' + encodeURIComponent(post.dataset.postId);
    }
  });
  feed.addEventListener('submit', e => {
    e.preventDefault();
    const input = e.target.elements.c, text = input.value.trim(); if (!text) return;
    const box = e.target.closest('.cm-box');
    box.insertAdjacentHTML('afterbegin', `<div class="cm"><span class="av sm" data-me></span><p><b>${esc(user)}</b>${esc(text)}</p></div>`);
    CC.applyAvatars(); input.value = '';
    const n = box.closest('.post').querySelector('.n-cm'); n.dataset.base = +n.dataset.base + 1; n.textContent = plural(+n.dataset.base, 'comment');
  });

  // ---- filter tabs ----
  $('#filters').addEventListener('click', e => {
    const b = e.target.closest('button'); if (!b) return;
    document.querySelectorAll('#filters button').forEach(x => x.classList.toggle('on', x === b));
    let shown = 0;
    feed.querySelectorAll('.post').forEach(p => {
      const ok = b.dataset.f === 'all' || p.dataset.kind === b.dataset.f;
      p.classList.toggle('hidden', !ok); if (ok) shown++;
    });
    $('#feed-empty').classList.toggle('hidden', shown > 0);
  });

  // ---- composer ----
  const ta = $('#post-text'), postBtn = $('#post-btn'), preview = $('#preview');
  let photo = null, lead = false;
  const sync = () => { postBtn.disabled = !ta.value.trim(); };
  ta.addEventListener('input', () => { ta.style.height = 'auto'; ta.style.height = Math.min(ta.scrollHeight, 240) + 'px'; sync(); });
  $('#pick-photo').onclick = () => $('#post-photo').click();
  $('#pick-lead').onclick = e => { lead = !lead; e.currentTarget.setAttribute('aria-pressed', lead); };
  $('#post-photo').onchange = async e => {
    const f = e.target.files[0]; if (!f) return;
    if (!f.type.startsWith('image/') || f.size > 8 * 1024 * 1024) { toast('Choose an image under 8 MB.'); e.target.value = ''; return; }
    try {
      photo = await readImage(f, 1000);
      preview.innerHTML = `<img src="${photo}" alt="Selected photo"><button type="button" aria-label="Remove photo">${icon('x')}</button>`;
      preview.classList.remove('hidden');
    } catch (err) { toast(err.message); }
  };
  preview.onclick = e => { if (e.target.closest('button')) { photo = null; preview.classList.add('hidden'); preview.innerHTML = ''; $('#post-photo').value = ''; } };
  $('#composer').addEventListener('submit', e => {
    e.preventDefault();
    const text = ta.value.trim(); if (!text) return;
    feed.insertAdjacentHTML('afterbegin', render({
      id: 'new' + Date.now(), kind: 'post', me: true, who: user, role: profile.headline || 'Job seeker',
      ago: 'Just now', text, img: photo, lead, likes: 0, comments: 0
    }));
    document.querySelector('#filters [data-f=all]').click();
    CC.applyAvatars();
    ta.value = ''; ta.style.height = ''; sync();
    photo = null; preview.classList.add('hidden'); preview.innerHTML = ''; $('#post-photo').value = '';
    lead = false; $('#pick-lead').setAttribute('aria-pressed', 'false');
    $('#feed-empty').classList.add('hidden');
    toast('Posted to your network.');
  });
})();