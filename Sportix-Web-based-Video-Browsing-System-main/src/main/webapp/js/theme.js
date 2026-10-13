/**
 * theme.js — Two independent toggles:
 *   1. Dark/Light Mode   → client-side only, persisted in localStorage.
 *   2. Spoiler-Free Mode → persisted server-side in HttpSession, because
 *      the JSP conditionally OMITS score data from the HTML at render
 *      time (JSTL), not just hides it with CSS. A fetch() updates the
 *      session, then the page reloads so JSTL re-evaluates with the
 *      new value.
 */
/**
 * theme.js — Two independent toggles:
 *   1. Dark/Light Mode   -> client-side only, persisted in localStorage.
 *   2. Hide Scores Mode  -> persisted server-side in HttpSession & MySQL.
 */
(function () {
    const STORAGE_KEY = 'sportix-theme';
    const htmlEl = document.documentElement;

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

    // 2. Single global click handler (Fast, zero duplicate listeners!)
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
                const currentlyOn = spoilerBtn.getAttribute('aria-checked') === 'true';
                const newState = !currentlyOn;
                spoilerBtn.setAttribute('aria-checked', String(newState));

                const pathParts = window.location.pathname.split('/');
                const contextPath = (pathParts.length > 2 && pathParts[1]) ? '/' + pathParts[1] : '';

                fetch(contextPath + '/toggle-spoiler', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: 'spoilerFree=' + newState
                }).then(function () {
                    window.location.reload();
                });
                return;
            }

            // C. Per-Card Reveal Button Click
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