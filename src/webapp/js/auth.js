// CareerConnect - login / register behavior (Sprint 1)
// NOTE: accounts live in the browser's localStorage as a stand-in until the Java backend exists.
// When the backend is ready, replace the two "BACKEND" blocks below with fetch('/api/register') and fetch('/api/login').

const $ = s => document.querySelector(s);
const form = $('#auth-form'), msg = $('#msg'), btn = $('#submit');
const COPY = {
  login:    { title: 'Welcome back',         sub: 'Sign in to pick up where you left off.',        cta: 'Sign in' },
  register: { title: 'Create your account',  sub: 'Pick your account type and get started in a minute.', cta: 'Create account' }
};
let mode = 'login', role = 'Job Seeker';

const getUsers = () => { try { return JSON.parse(localStorage.getItem('cc_users')) || {}; } catch { return {}; } };
const saveUsers = u => localStorage.setItem('cc_users', JSON.stringify(u));
async function sha256(text) {
  const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
  return [...new Uint8Array(buf)].map(b => b.toString(16).padStart(2, '0')).join('');
}

function show(text, ok = false) {
  msg.textContent = text;
  msg.className = 'msg show' + (ok ? ' ok' : '');
  if (!ok) { form.classList.remove('shake'); void form.offsetWidth; form.classList.add('shake'); }
}
function setMode(m) {
  mode = m;
  form.classList.toggle('mode-login', m === 'login');
  document.querySelectorAll('.seg button').forEach(b => b.classList.toggle('on', b.dataset.mode === m));
  $('#title').textContent = COPY[m].title;
  $('#sub').textContent = COPY[m].sub;
  btn.textContent = COPY[m].cta;
  $('#password').autocomplete = m === 'login' ? 'current-password' : 'new-password';
  msg.className = 'msg';
}

document.querySelectorAll('.seg button').forEach(b => b.onclick = () => setMode(b.dataset.mode));
document.querySelectorAll('.role').forEach(b => b.onclick = () => {
  role = b.dataset.role;
  document.querySelectorAll('.role').forEach(r => {
    r.classList.toggle('on', r === b);
    r.setAttribute('aria-checked', r === b);
  });
});
$('#eye').onclick = () => {
  const p = $('#password'), hidden = p.type === 'password';
  p.type = hidden ? 'text' : 'password';
  $('#eye').setAttribute('aria-label', hidden ? 'Hide password' : 'Show password');
};

// Password strength meter
const COLORS = ['#ef4444', '#f97316', '#eab308', '#22c55e'];
$('#password').addEventListener('input', e => {
  const v = e.target.value;
  const score = [v.length >= 8, /[A-Z]/.test(v) && /[a-z]/.test(v), /\d/.test(v), /[^A-Za-z0-9]/.test(v)].filter(Boolean).length;
  document.querySelectorAll('#meter i').forEach((bar, i) => bar.style.background = i < score ? COLORS[score - 1] : '');
});

function validate(u, p) {
  if (u.length < 3) return 'Username needs at least 3 characters.';
  if (mode === 'login') return p ? '' : 'Enter your password.';
  if (!/^\S+@\S+\.\S+$/.test($('#email').value.trim())) return 'Enter a valid email address.';
  if (p.length < 8) return 'Password needs at least 8 characters.';
  if (p !== $('#confirm').value) return 'Passwords do not match.';
  return '';
}

form.addEventListener('submit', async e => {
  e.preventDefault();
  const u = $('#username').value.trim(), p = $('#password').value;
  const err = validate(u, p);
  if (err) return show(err);

  btn.disabled = true;
  const users = getUsers(), key = u.toLowerCase(), hash = await sha256(p);
  let account;

  if (mode === 'register') {
    // BACKEND: POST /api/register {username, email, password, role}
    if (users[key]) { btn.disabled = false; return show('That username is taken. Try another one.'); }
    account = { username: u, email: $('#email').value.trim(), role, hash };
    users[key] = account; saveUsers(users);
  } else {
    // BACKEND: POST /api/login {username, password}
    account = users[key];
    if (!account || account.hash !== hash) { btn.disabled = false; return show('Username or password is incorrect.'); }
  }

  sessionStorage.setItem('cc_session', JSON.stringify({ username: account.username, role: account.role }));
  show(mode === 'register' ? 'Account created! Taking you in...' : 'Signed in! Taking you in...', true);
  setTimeout(() => location.href = 'dashboard.html', 700);
});