const SkyAlert = {
    toast(icon, title, timer) {
        return Swal.fire({
            toast: true, position: 'top-end', icon: icon, title: title,
            showConfirmButton: false, timer: timer || 3000, timerProgressBar: true,
            customClass: { popup: 'sky-toast' }
        });
    },
    success(message) {
        var result = this.toast('success', message, 3000);
        if (message && (message.includes('SKY-') || message.includes('confirmed'))) {
            fireBookingConfetti();
        }
        return result;
    },
    error(message) { return this.toast('error', message, 4000); },
    warning(message) { return this.toast('warning', message, 4000); },
    info(message) { return this.toast('info', message, 3000); },
    confirmDelete(itemName, deleteUrl) {
        return Swal.fire({
            title: 'Delete ' + itemName + '?', text: 'This action cannot be undone.', icon: 'warning',
            showCancelButton: true, confirmButtonColor: '#ba1a1a', cancelButtonColor: '#68519d',
            confirmButtonText: 'Yes, delete it', cancelButtonText: 'Cancel', reverseButtons: true
        }).then(function (result) {
            if (result.isConfirmed) {
                var form = document.getElementById('deleteForm');
                if (form) { form.action = deleteUrl; form.submit(); }
            }
        });
    },
    confirmCancel(bookingRef, cancelUrl) {
        return Swal.fire({
            title: 'Cancel Booking?', html: 'Booking <strong>' + bookingRef + '</strong> will be cancelled.',
            icon: 'warning', showCancelButton: true, confirmButtonColor: '#ba1a1a',
            cancelButtonColor: '#68519d', confirmButtonText: 'Yes, cancel it', cancelButtonText: 'Keep booking', reverseButtons: true
        }).then(function (result) { if (result.isConfirmed) window.location.href = cancelUrl; });
    }
};

function showSuccess(message) { return SkyAlert.success(message); }
function showError(message) { return SkyAlert.error(message); }

function triggerFlash(type, message) {
    if (!message || typeof Swal === 'undefined') return;
    if (type === 'success') SkyAlert.success(message);
    else if (type === 'error') SkyAlert.error(message);
    else if (type === 'warning') SkyAlert.warning(message);
    else SkyAlert.info(message);
}

function showTerms() {
    return Swal.fire({
        title: 'Terms of Use',
        html: '<p>SkyLink Ops is for authorized airline operations staff. Booking activity is logged and audited.</p>',
        confirmButtonColor: '#68519d', confirmButtonText: 'I understand'
    });
}

function showPrivacy() {
    return Swal.fire({
        title: 'Privacy Policy',
        html: '<p>Customer data is confidential and may only be used for legitimate booking operations.</p>',
        confirmButtonColor: '#68519d', confirmButtonText: 'Got it'
    });
}

function fireBookingConfetti() {
    if (typeof confetti !== 'undefined') {
        runConfetti();
        return;
    }
    var script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/canvas-confetti@1.9.3/dist/confetti.browser.min.js';
    script.onload = runConfetti;
    document.head.appendChild(script);
}

function runConfetti() {
    if (typeof confetti === 'undefined') return;
    confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.6 },
        colors: ['#68519d', '#cfbcff', '#1d0947', '#f59e0b', '#ffffff']
    });
    window.setTimeout(function () {
        confetti({
            particleCount: 40,
            angle: 60,
            spread: 55,
            origin: { x: 0, y: 0.65 },
            colors: ['#68519d', '#cfbcff', '#22c55e']
        });
    }, 200);
    window.setTimeout(function () {
        confetti({
            particleCount: 40,
            angle: 120,
            spread: 55,
            origin: { x: 1, y: 0.65 },
            colors: ['#1d0947', '#f59e0b', '#cfbcff']
        });
    }, 400);
}
