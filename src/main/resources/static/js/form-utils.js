function initPasswordToggle(inputId, toggleBtnId) {
    var input = document.getElementById(inputId);
    var button = document.getElementById(toggleBtnId);
    if (!input || !button) return;
    button.addEventListener('click', function () {
        var isPassword = input.type === 'password';
        input.type = isPassword ? 'text' : 'password';
        var icon = button.querySelector('.material-symbols-outlined');
        if (icon) icon.textContent = isPassword ? 'visibility_off' : 'visibility';
    });
}

function initStrengthMeter(inputId, barId, labelId) {
    var input = document.getElementById(inputId);
    var bar = document.getElementById(barId);
    var label = document.getElementById(labelId);
    if (!input || !bar) return;
    input.addEventListener('input', function () {
        var strength = getStrength(input.value);
        bar.className = 'strength-bar ' + strength.level;
        if (label) label.textContent = strength.label;
    });
}

function getStrength(password) {
    var score = 0;
    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    if (score <= 2) return { level: 'weak', label: 'Weak - add numbers and symbols' };
    if (score <= 3) return { level: 'medium', label: 'Medium - getting better' };
    return { level: 'strong', label: 'Strong password' };
}

function initCapsLockWarning(inputId, warningId) {
    var input = document.getElementById(inputId);
    var warning = document.getElementById(warningId);
    if (!input || !warning) return;
    input.addEventListener('keyup', function (event) {
        warning.style.display = event.getModifierState('CapsLock') ? 'block' : 'none';
    });
}

function initSubmitLoading(formId, btnId, loadingText) {
    var form = document.getElementById(formId);
    var button = document.getElementById(btnId);
    if (!form || !button) return;
    form.addEventListener('submit', function () {
        button.disabled = true;
        button.innerHTML = '<span class="material-symbols-outlined">progress_activity</span> ' + (loadingText || 'Please wait...');
    });
}
