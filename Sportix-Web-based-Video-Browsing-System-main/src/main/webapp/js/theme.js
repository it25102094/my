/**
 * theme.js — Navbar behaviour shared by every page:
 *   1. Dark/Light Mode   -> client-side only, persisted in localStorage.
 *   2. Hide Scores Mode  -> persisted server-side in HttpSession & MySQL (ToggleSpoilerServlet).
 *   3. Mobile menu       -> shows/hides the navbar links on small screens.
 *
 * FIX: the context path used to be guessed from the URL ("/user/watchlater" was treated as the
 * context "/user"), so the Hide Scores toggle posted to a wrong URL on many pages. The navbar
 * now provides the real context path in data-ctx.
 */
(function () {
    const STORAGE_KEY = 'sportix-theme';
    const htmlEl = document.documentElement;

    function contextPath() {
        const nav = document.querySelector('nav.navbar[data-ctx]');
        return nav ? nav.getAttribute('data-ctx') : '';
    }

    function applyTheme(theme) {
        htmlEl.setAttribute('data-theme', theme);
        if (document.body) {
            document.body.setAttribute('data-theme', theme);
            document.body.classList.toggle('light-theme', theme === 'light');
        }
        const btn = document.getElementById('theme-toggle');
        if (btn) {
            btn.setAttribute('aria-checked', String(theme === 'dark'));
        }
    }

    // 1. Apply saved theme immediately
    const initialTheme = localStorage.getItem(STORAGE_KEY) || 'dark';
    applyTheme(initialTheme);

    // Sync button state once DOM is ready
    document.addEventListener('DOMContentLoaded', function () {
        applyTheme(localStorage.getItem(STORAGE_KEY) || 'dark');
    });

    // 2. Single global click handler (no duplicate listeners even if the script is included twice)
    if (!window.__sportixNavbarBound) {
        window.__sportixNavbarBound = true;

        document.addEventListener('click', function (e) {
            // A. Theme Toggle Click (Instant 0ms switch)
            const themeBtn = e.target.closest('#theme-toggle');
            if (themeBtn) {
                const current = htmlEl.getAttribute('data-theme') || 'dark';
                const next = (current === 'dark') ? 'light' : 'dark';
                localStorage.setItem(STORAGE_KEY, next);
                applyTheme(next);
                return;
            }

            // B. Hide Scores Toggle Click
            const spoilerBtn = e.target.closest('#spoiler-toggle');
            if (spoilerBtn) {
                const newState = spoilerBtn.getAttribute('aria-checked') !== 'true';
                spoilerBtn.setAttribute('aria-checked', String(newState));

                fetch(contextPath() + '/toggle-spoiler', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: 'spoilerFree=' + newState
                }).then(function () {
                    window.location.reload();
                });
                return;
            }

            // C. Mobile menu button
            const menuBtn = e.target.closest('#mobile-menu-toggle');
            if (menuBtn) {
                const nav = menuBtn.closest('.navbar');
                if (nav) nav.classList.toggle('open');
                return;
            }

            // D. Per-Card Reveal Button Click
            const revealBtn = e.target.closest('.reveal-btn');
            if (revealBtn && revealBtn.closest('.video-card')) {
                e.preventDefault();
                e.stopPropagation();
                const overlay = revealBtn.closest('.spoiler-overlay');
                if (overlay) overlay.remove();
            }
        });
    }
})();
