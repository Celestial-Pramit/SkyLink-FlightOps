document.addEventListener('DOMContentLoaded', function () {
    initLiveClock();
    initGreeting();
    initMobileSidebar();
    initActiveNavItem();
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
    greeting.textContent = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';
}

function initMobileSidebar() {
    var hamburger = document.getElementById('hamburgerBtn');
    var sidebar = document.getElementById('mainSidebar');
    var overlay = document.getElementById('sidebarOverlay');
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
    var currentPath = window.location.pathname;
    document.querySelectorAll('.nav-item, .nav-subitem').forEach(function (item) {
        var href = item.getAttribute('href');
        if (href && href !== '/' && currentPath.indexOf(href) === 0) {
            item.classList.add('active');
            var parent = item.closest('.collapse');
            if (parent) {
                parent.classList.add('show');
                var trigger = document.querySelector('[data-bs-target="#' + parent.id + '"]');
                if (trigger) trigger.setAttribute('aria-expanded', 'true');
            }
        }
    });
}
