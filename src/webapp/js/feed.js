/*  home feed (frontend only).
   ITEMS below is sample content. When the postings backend exists, replace it with a fetch()
   that returns the same shape and keep render(). Posts created in the composer are not saved yet. */
(function () {
  if (!window.CC) return;                       // redirecting to login
  const { user, esc, icon, initials, store, toast, readImage, strength } = CC;
  const $ = s => document.querySelector(s);
  const feed = $('#feed');

  const ITEMS = [
    { id: 1, kind: 'job', co: 'Lumen Labs', c: ['#7c3aed', '#a78bfa'], title: 'Junior Frontend Developer', loc: 'Montréal, QC (hybrid)', type: 'Full-time', pay: '$62k to $74k', ago: '2h',
      text: 'Help us ship the interface 40,000 people use every day. You will pair with designers, own small features end to end, and get a mentor from week one.',
      tags: ['React', 'TypeScript', 'CSS'], likes: 24, comments: 6 },
    { id: 2, kind: 'post', who: 'Sofia Marchetti', role: 'Talent partner at Lumen Labs', av: 'c2', ago: '3h',
      text: 'Three things that make a junior application stand out:\n1. A project you can demo in two minutes.\n2. A CV that fits on one page.\n3. A short note on why this team.',
      likes: 112, comments: 18 },
    { id: 3, kind: 'job', co: 'Mont-Royal Analytics', c: ['#0891b2', '#6366f1'], title: 'Data Analyst Intern', loc: 'Remote, Canada', type: 'Internship', pay: '$24/hr', ago: '5h',
      text: 'Turn messy retail data into dashboards the whole company reads. Four-month placement with a path to a full-time offer.',
      tags: ['SQL', 'Python', 'Tableau'], likes: 41, comments: 9 },
    { id: 4, kind: 'post', who: 'Dev Patel', role: 'Computer science student', av: 'c1', ago: '8h',
      text: 'I just accepted my first co-op offer! Huge thanks to everyone who reviewed my CV and ran mock interviews with me. Paying it forward this Friday:',
      event: { title: 'Free CV review night', when: 'Friday, 6 PM, online' }, likes: 341, comments: 57 },
    { id: 5, kind: 'job', co: 'Atelier Pixel', c: ['#ea580c', '#f59e0b'], title: 'Product Designer', loc: 'Laval, QC', type: 'Contract', pay: '$45 to $55/hr', ago: '1d',
      text: 'Redesign onboarding for a booking app used by independent studios. You will run research sessions and hand off in Figma.',
      tags: ['Figma', 'Prototyping', 'User research'], likes: 18, comments: 3 },
    { id: 6, kind: 'job', co: 'Northwind Robotics', c: ['#db2777', '#fb7185'], title: 'Java Backend Developer', loc: 'Montréal, QC', type: 'Full-time', pay: '$78k to $95k', ago: '2d',
      text: 'Build the services that coordinate fleets of warehouse robots. Spring Boot, PostgreSQL, and a team that writes tests first.',
      tags: ['Java', 'Spring Boot', 'PostgreSQL'], likes: 57, comments: 12 }
  ];
  const COMPANIES = [
    { n: 'Lumen Labs', c: ['#7c3aed', '#a78bfa'], s: '3 open roles' },
    { n: 'Northwind Robotics', c: ['#db2777', '#fb7185'], s: '2 open roles' },
    { n: 'Atelier Pixel', c: ['#ea580c', '#f59e0b'], s: '1 open role' }
  ];

  const plural = (n, w) => `${n} ${w}${n === 1 ? '' : 's'}`;
  const actBtn = (act, ic, label, extra = '') =>
    `<button type="button" class="act ${extra}" data-act="${act}"${act === 'like' || act === 'save' ? ' aria-pressed="false"' : ''}>${icon(ic)}<span>${label}</span></button>`;

  function render(it) {
    const isJob = it.kind === 'job';
    const head = isJob
      ? `<div class="logo" aria-hidden="true">${esc(it.co[0])}</div>
         <div class="who"><h3>${esc(it.title)}</h3><small>${esc(it.co)} posted ${esc(it.ago)} ago</small></div>`
      : `<span class="av ${it.me ? '' : esc(it.av || 'c1')}" ${it.me ? 'data-me' : ''}>${it.me ? '' : esc(initials(it.who))}</span>
         <div class="who"><span class="name">${esc(it.who)}</span>${it.lead ? '<span class="badge">Job lead</span>' : ''}<small>${esc(it.role)}, ${esc(it.ago === 'Just now' ? it.ago : it.ago + ' ago')}</small></div>`;
    const chips = isJob ? `<div class="chips">
        <span class="chip">${icon('pin')}${esc(it.loc)}</span><span class="chip">${icon('briefcase')}${esc(it.type)}</span>
        <span class="chip pay">${icon('cash')}${esc(it.pay)}</span></div>` : '';
    const tags = isJob ? `<div class="tags">${it.tags.map(t => `<span class="tag">${esc(t)}</span>`).join('')}</div>` : '';
    const event = it.event ? `<div class="event">${icon('calendar')}<div><b>${esc(it.event.title)}</b><small>${esc(it.event.when)}</small></div></div>` : '';
    const img = it.img ? `<img class="p-img" src="${it.img}" alt="Photo attached to the post">` : '';
    return `<article class="card post ${isJob ? 'job' : ''}" id="post-${esc(it.id)}" data-kind="${it.kind}"
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

  feed.innerHTML = ITEMS.map(render).join('');

  // ---- left + right rails ----
  const TILES = [
    ['t1', 'search', 'Find jobs', 'Coming soon', 'button', 'Find jobs'],
    ['t2', 'doc', 'My CVs', 'Manage your resumes', 'a'],
    ['t3', 'chart', 'Applications', 'Coming soon', 'button', 'Applications'],
    ['t4', 'star', 'Favourites', 'Coming soon', 'button', 'Favourites']
  ];
  $('#tiles').innerHTML = TILES.map(([cls, ic, t, s, tag, soon]) => tag === 'a'
    ? `<a class="tile ${cls}" href="resume.html">${icon(ic)}<span><b>${t}</b><small>${s}</small></span></a>`
    : `<button type="button" class="tile ${cls}" data-soon="${soon}">${icon(ic)}<span><b>${t}</b><small>${s}</small></span></button>`).join('');

  $('#companies').innerHTML = COMPANIES.map(c => `<li>
      <span class="logo" style="--c1:${c.c[0]};--c2:${c.c[1]}">${c.n[0]}</span>
      <div><b>${c.n}</b><small>${c.s}</small></div>
      <button type="button" class="follow" aria-pressed="false">Follow</button></li>`).join('');
  $('#companies').addEventListener('click', e => {
    const b = e.target.closest('.follow'); if (!b) return;
    const on = b.getAttribute('aria-pressed') !== 'true';
    b.setAttribute('aria-pressed', on); b.textContent = on ? 'Following' : 'Follow';
  });

  function paintProfile() {
    const p = store.get(), s = strength();
    $('#me-name').textContent = user;
    $('#me-headline').textContent = p.headline || 'Add a headline so recruiters know what you do';
    $('#strength-bar').firstElementChild.style.setProperty('--pct', s.pct + '%');
    $('#strength-bar').setAttribute('aria-valuenow', s.pct);
    $('#strength-text').textContent = s.pct === 100 ? 'Your profile is complete.' : `${s.pct}% complete. Finish it to get noticed.`;
    const todo = [['Upload your CV', 'resume.html', 'doc'], ...s.missing.map(m => [m, 'profile.html', 'plus'])];
    $('#todo').innerHTML = todo.map(([t, h, ic]) => `<li><a href="${h}">${icon(ic)}${esc(t)}</a></li>`).join('');
  }
  paintProfile();
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
      toast('Applications open soon. Keep your CV up to date.');
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
      id: 'new' + Date.now(), kind: 'post', me: true, who: user, role: store.get().headline || 'Job seeker',
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