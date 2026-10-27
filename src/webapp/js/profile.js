/*  profile page.
   Headline, About, skills and location are loaded from and saved to /api/profile.
   Photo and cover are still kept in localStorage via CC.store (no image storage on the server yet).
   The CV card only READS the existing GET /api/resume endpoint; it never changes resume data. */
(function () {
  if (!window.CC) return;
  const { user, esc, icon, initials, store, toast, readImage, strength, applyAvatars, api } = CC;
  const $ = s => document.querySelector(s);

  $('#cover-btn').innerHTML = `${icon('camera')}Change cover`;
  $('#avatar-btn').innerHTML = icon('camera');
  $('#edit-btn').innerHTML = `${icon('pencil')}Edit profile`;
  $('#edit-about').innerHTML = icon('pencil');
  $('#edit-skills').innerHTML = icon('pencil');

  let profile = {};            // GET /api/profile response once loaded

  function paint() {
    const p = profile, photos = store.get(), s = strength(profile);
    $('#p-name').textContent = p.name ? `${p.name} ${p.surname}` : user;
    $('#p-headline').textContent = p.headline || 'Add a headline';
    $('#p-headline').classList.toggle('muted', !p.headline);
    const loc = [p.city, p.provinceState].filter(Boolean).join(', ');
    $('#p-loc').innerHTML = `${icon('pin')}${esc(loc || 'Add your location')}`;
    $('#p-conn').innerHTML = `${icon('users')}Start building your network`;
    $('#p-about').textContent = p.about || 'Write a short summary of your skills and goals so employers can get to know you.';
    $('#p-about').classList.toggle('muted', !p.about);
    const skills = p.skills || [];
    $('#p-skills').innerHTML = skills.length
      ? skills.map(k => `<span class="tag">${esc(k)}</span>`).join('')
      : '<p class="muted">Add the skills employers should know about.</p>';
    const cover = $('#cover');
    cover.style.backgroundImage = photos.cover ? 'url("' + photos.cover + '")' : '';
    $('#strength-bar').firstElementChild.style.setProperty('--pct', s.pct + '%');
    $('#strength-bar').setAttribute('aria-valuenow', s.pct);
    $('#strength-text').textContent = s.pct === 100 ? 'Your profile is complete.' : `${s.pct}% complete. Next: ${s.missing[0].toLowerCase()}.`;
    applyAvatars();
  }

  // ---- image uploads (cover + avatar) ----
  function wireUpload(btnSel, fileSel, key, maxW) {
    $(btnSel).onclick = () => $(fileSel).click();
    $(fileSel).onchange = async e => {
      const f = e.target.files[0]; e.target.value = ''; if (!f) return;
      if (!f.type.startsWith('image/') || f.size > 8 * 1024 * 1024) return toast('Choose an image under 8 MB.');
      try {
        const data = await readImage(f, maxW);
        if (!store.set({ [key]: data })) return toast('Could not save that image. Try a smaller one.');
        paint(); toast(key === 'cover' ? 'Cover photo updated.' : 'Profile photo updated.');
      } catch (err) { toast(err.message); }
    };
  }
  wireUpload('#cover-btn', '#cover-file', 'cover', 1800);
  wireUpload('#avatar-btn', '#avatar-file', 'avatar', 480);

  // ---- edit dialog: saved to the database through PUT /api/profile ----
  const dlg = $('#edit-dialog'), editError = $('#edit-error');
  function openEdit(focusId) {
    const p = profile;
    $('#f-headline').value = p.headline || ''; $('#f-about').value = p.about || '';
    $('#f-city').value = p.city || ''; $('#f-province').value = p.provinceState || '';
    $('#f-skills').value = (p.skills || []).join(', ');
    editError.classList.add('hidden');
    dlg.showModal(); $(focusId || '#f-headline').focus();
  }
  $('#edit-btn').onclick = () => openEdit();
  $('#edit-about').onclick = () => openEdit('#f-about');
  $('#edit-skills').onclick = () => openEdit('#f-skills');
  $('#cancel-edit').onclick = () => dlg.close();
  dlg.addEventListener('click', e => { if (e.target === dlg) dlg.close(); });   // click on backdrop
  $('#edit-form').addEventListener('submit', async e => {
    e.preventDefault();                          // stay open until the server accepts the changes
    const btn = e.submitter || $('#edit-form [type=submit]');
    btn.disabled = true;
    try {
      const data = await api('/api/profile', { method: 'PUT', body: {
        headline: $('#f-headline').value, about: $('#f-about').value,
        city: $('#f-city').value, provinceState: $('#f-province').value,
        skills: $('#f-skills').value.split(',')
      } });
      profile = data.profile;
      dlg.close(); paint(); toast('Profile updated.');
    } catch (err) {
      editError.textContent = err.message; editError.classList.remove('hidden');
    } finally { btn.disabled = false; }
  });

  // ---- CV card: read-only status from the existing resume endpoint ----
  (async function loadCv() {
    const box = $('#cv-state');
    const show = (title, sub, ok) => box.innerHTML = `<span class="ic" style="${ok ? '' : 'background:linear-gradient(135deg,#94a3b8,#64748b)'}">${icon('doc')}</span><div><b>${esc(title)}</b><small>${esc(sub)}</small></div>`;
    show('Checking your CV', 'One moment', false);
    try {
      const res = await fetch('/api/resume');
      const data = await res.json().catch(() => ({}));
      if (res.ok) show(data.fileName, 'Employers see this when you apply', true);
      else if (res.status === 403) show('CVs are for job seekers', 'Employer accounts do not upload a CV', false);
      else if (res.status === 401) show('Sign in again to see your CV', 'Your session has expired', false);
      else show('No CV yet', 'Upload one so you can apply in a click', false);
    } catch { show('CV status unavailable', 'Check your connection and refresh', false); }
  })();

  // ---- people you may know (sample) ----
  const PEOPLE = [['Sofia Marchetti', 'Talent partner at Lumen Labs', 'c2'], ['Dev Patel', 'Computer science student', 'c1'], ['Amélie Tremblay', 'Product designer at Atelier Pixel', 'c4']];
  $('#people').innerHTML = PEOPLE.map(([n, r, c]) => `<li><span class="av ${c}">${esc(initials(n))}</span>
    <div><b>${esc(n)}</b><small>${esc(r)}</small></div><button type="button" class="follow" aria-pressed="false">Connect</button></li>`).join('');
  $('#people').addEventListener('click', e => {
    const b = e.target.closest('.follow'); if (!b) return;
    const on = b.getAttribute('aria-pressed') !== 'true';
    b.setAttribute('aria-pressed', on); b.textContent = on ? 'Requested' : 'Connect';
  });

  paint();
  api('/api/profile').then(p => { profile = p; paint(); })
    .catch(err => toast(err.status === 403 ? 'Profiles are for job seeker accounts.' : 'Could not load your profile. ' + err.message));
})();