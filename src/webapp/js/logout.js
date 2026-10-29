/* Logout for every logged-in page: any link with id="logout" or data-logout ends the server
   session, forgets the user in this browser and goes back to the login page.
   It works alongside a page's own logout handler (the recruiter pages have one that only
   clears the browser and redirects): keepalive lets the request finish after the page leaves. */
(function () {
  function logout() {
    fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin', keepalive: true })
      .catch(() => { /* server unreachable: still log out in this browser */ });
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    location.replace('index.html');
  }

  document.addEventListener('click', e => {
    const link = e.target.closest('#logout, [data-logout]');
    if (link) { e.preventDefault(); logout(); }
  });
})();
