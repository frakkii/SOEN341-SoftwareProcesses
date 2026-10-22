/*  profile page (frontend only).
   Photo, cover, headline, location and About are kept in localStorage via CC.store until a profile API exists.
   The CV card only READS the existing GET /api/resume endpoint; it never changes resume data. */
(function () {
  if (!window.CC) return;
  const { user, esc, icon, initials, store, toast, readImage, strength, applyAvatars } = CC;
  const $ = s => document.querySelector(s);

  $('#cover-btn').innerHTML = `${icon('camera')}Change cover`;
  $('#avatar-btn').innerHTML = icon('camera');
  $('#edit-btn').innerHTML = `${icon('pencil')}Edit profile`;
  $('#edit-about').innerHTML = icon('pencil');
  $('#add-exp').innerHTML = icon('plus');
  $('#add-edu').innerHTML = icon('plus');

  function paint() {
    const p = store.get(), s = strength();
    $('#p-name').textContent = user;
    $('#p-headline').textContent = p.headline || 'Add a headline';
    $('#p-headline').classList.toggle('muted', !p.headline);
    $('#p-loc').innerHTML = `${icon('pin')}${esc(p.location || 'Add your location')}`;
    $('#p-conn').innerHTML = `${icon('users')}Start building your network`;
    $('#p-about').textContent = p.about || 'Write a short summary of your skills and goals so employers can get to know you.';
    $('#p-about').classList.toggle('muted', !p.about);
    const cover = $('#cover');
    cover.style.backgroundImage = p.cover ? 'url("' + p.cover + '")' : '';
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

  // ---- edit dialog ----
  const dlg = $('#edit-dialog');
  function openEdit(focusId) {
    const p = store.get();
    $('#f-headline').value = p.headline || ''; $('#f-location').value = p.location || ''; $('#f-about').value = p.about || '';
    dlg.showModal(); $(focusId || '#f-headline').focus();
  }
  $('#edit-btn').onclick = () => openEdit();
  $('#edit-about').onclick = () => openEdit('#f-about');
  $('#cancel-edit').onclick = () => dlg.close();
  dlg.addEventListener('click', e => { if (e.target === dlg) dlg.close(); });   // click on backdrop
  $('#edit-form').addEventListener('submit', () => {
    if (!store.set({ headline: $('#f-headline').value.trim(), location: $('#f-location').value.trim(), about: $('#f-about').value.trim() })) toast('Could not save your changes.');
    else toast('Profile updated.');
    paint();
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
})();