document.addEventListener('DOMContentLoaded', function () {
    initLiveClock();
    initGreeting();
    initMobileSidebar();
    initActiveNavItem();
    initDarkMode();
    initKeyboardShortcuts();
    initSkeletonRemoval();
});

function initLiveClock() {
    var clock = document.getElementById('liveClock');
    if (!clock) return;
    function tick() {
        clock.textContent = new Date().toLocaleTimeString('en-US', {
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    }
    tick();
    window.setInterval(tick, 1000);
}

function initGreeting() {
    var greeting = document.getElementById('greeting');
    if (!greeting) return;
    var hour = new Date().getHours();
    greeting.textContent = hour < 12 ? 'Good morning'
        : hour < 17 ? 'Good afternoon' : 'Good evening';
}

function initMobileSidebar() {
    var hamburger = document.getElementById('hamburgerBtn');
    var sidebar   = document.getElementById('mainSidebar');
    var overlay   = document.getElementById('sidebarOverlay');
    if (!hamburger || !sidebar) return;
    hamburger.addEventListener('click', function () {
        sidebar.classList.toggle('open');
        if (overlay) overlay.classList.toggle('active');
    });
    if (overlay) overlay.addEventListener('click', function () {
        sidebar.classList.remove('open');
        overlay.classList.remove('active');
    });
}

function initActiveNavItem() {
    var path = window.location.pathname;

    // Explicit route -> nav-target overrides. Preserve the server-side activePage
    // intent for the Find & Book flow: /bookings/create has no dedicated nav item,
    // so both search and create map to the "Find & Book" nav target (/bookings/search).
    var overrides = {
        '/bookings/search': '/bookings/search',
        '/bookings/create': '/bookings/search'
    };
    var matchPath = overrides[path] || path;

    var matches = [];
    document.querySelectorAll('.nav-item, .nav-subitem').forEach(function (item) {
        var href = item.getAttribute('href');
        if (!href || href === '/') return;
        var target = href.split('?')[0];
        var isMatch = matchPath === target || matchPath.indexOf(target + '/') === 0;
        if (isMatch) matches.push({ item: item, target: target });
    });

    // Most specific route wins (longest matching path prefix).
    matches.sort(function (a, b) { return b.target.length - a.target.length; });

    // Normalize: clear any server-rendered or previously-set active state first,
    // then apply to exactly one item.
    document.querySelectorAll('.nav-item, .nav-subitem').forEach(function (item) {
        item.classList.remove('active');
    });

    var best = matches[0];
    if (best) {
        best.item.classList.add('active');
        var parent = best.item.closest('.collapse');
        if (parent) {
            parent.classList.add('show');
            var trigger = document.querySelector('[data-bs-target="#' + parent.id + '"]');
            if (trigger) trigger.setAttribute('aria-expanded', 'true');
        }
    }
}

function initDarkMode() {
    var btn  = document.getElementById('darkModeToggle');
    var icon = document.getElementById('darkModeIcon');
    if (!btn) return;
    var isDark = false;
    try { isDark = localStorage.getItem('skylink-dark-mode') === 'true'; } catch(e) {}
    if (isDark) {
        document.body.classList.add('dark-mode');
        if (icon) icon.textContent = 'light_mode';
        btn.title = 'Switch to light mode';
    }
    btn.addEventListener('click', function () {
        var dark = document.body.classList.toggle('dark-mode');
        if (icon) icon.textContent = dark ? 'light_mode' : 'dark_mode';
        btn.title = dark ? 'Switch to light mode' : 'Switch to dark mode';
        try { localStorage.setItem('skylink-dark-mode', String(dark)); } catch(e) {}
    });
}

function initKeyboardShortcuts() {
    document.addEventListener('keydown', function (e) {
        var el = document.activeElement;
        var tag = el && el.tagName;
        if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;
        if (el && el.isContentEditable) return;
        if (document.querySelector('.swal2-container')) return;
        if (e.ctrlKey || e.metaKey || e.altKey) return;
        switch (e.key) {
            case 'n': case 'N': window.location.href = '/bookings/search'; break;
            case 'b': case 'B': window.location.href = '/bookings'; break;
            case 'f': case 'F': window.location.href = '/flights'; break;
            case 'c': case 'C': window.location.href = '/customers'; break;
            case 'd': case 'D': window.location.href = '/dashboard'; break;
            case '?': showKeyboardHelp(); break;
        }
    });
}

function showKeyboardHelp() {
    if (typeof Swal === 'undefined') return;
    Swal.fire({
        title: 'Keyboard Shortcuts',
        html: '<div style="text-align:left;display:grid;grid-template-columns:auto 1fr;gap:8px 16px;align-items:center">'
            + '<kbd style="padding:4px 8px;background:#f2ecf2;border:1px solid #cac4d0;border-radius:4px;font-family:monospace">N</kbd><span>New Booking</span>'
            + '<kbd style="padding:4px 8px;background:#f2ecf2;border:1px solid #cac4d0;border-radius:4px;font-family:monospace">B</kbd><span>Bookings</span>'
            + '<kbd style="padding:4px 8px;background:#f2ecf2;border:1px solid #cac4d0;border-radius:4px;font-family:monospace">F</kbd><span>Flights</span>'
            + '<kbd style="padding:4px 8px;background:#f2ecf2;border:1px solid #cac4d0;border-radius:4px;font-family:monospace">C</kbd><span>Customers</span>'
            + '<kbd style="padding:4px 8px;background:#f2ecf2;border:1px solid #cac4d0;border-radius:4px;font-family:monospace">D</kbd><span>Dashboard</span>'
            + '<kbd style="padding:4px 8px;background:#f2ecf2;border:1px solid #cac4d0;border-radius:4px;font-family:monospace">?</kbd><span>Show shortcuts</span>'
            + '</div>',
        confirmButtonColor: '#68519d',
        confirmButtonText: 'Got it'
    });
}

function initSkeletonRemoval() {
    document.body.classList.remove('skeleton-loading');
    document.querySelectorAll('.skeleton-row').forEach(function(row) {
        row.remove();
    });
}

function initLiveSearch(tableId, inputId, columnIndexes) {
    var input = document.getElementById(inputId);
    var table = document.getElementById(tableId);
    if (!input || !table) return;
    var noResultsRow = table.querySelector('.search-no-results');
    var debounceTimer;
    input.addEventListener('input', function () {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(function () {
            var query = input.value.trim().toLowerCase();
            var rows  = table.querySelectorAll('tbody tr:not(.skeleton-row):not(.search-no-results)');
            var visibleCount = 0;
            rows.forEach(function (row) {
                var text = '';
                if (columnIndexes && columnIndexes.length > 0) {
                    columnIndexes.forEach(function (idx) {
                        var cell = row.cells[idx];
                        if (cell) text += ' ' + cell.textContent;
                    });
                } else {
                    text = row.textContent;
                }
                var matches = !query || text.toLowerCase().includes(query);
                row.style.display = matches ? '' : 'none';
                if (matches) visibleCount++;
            });
            if (noResultsRow) {
                noResultsRow.style.display = visibleCount === 0 && query ? '' : 'none';
            }
        }, 200);
    });
}
