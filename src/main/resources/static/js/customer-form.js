function previewCustomerPhoto(input) {
    const warning = document.getElementById('photoSizeWarning');
    const file = input.files && input.files[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) { input.value = ''; if (warning) warning.hidden = false; return; }
    if (warning) warning.hidden = true;
    const reader = new FileReader();
    reader.onload = function (event) {
        let image = document.getElementById('photoPreviewImg');
        const initials = document.getElementById('photoPreviewInitials');
        if (!image) { image = document.createElement('img'); image.id = 'photoPreviewImg'; image.className = 'photo-preview-img'; document.querySelector('.photo-preview-wrap').prepend(image); }
        image.src = event.target.result; image.style.display = 'block'; if (initials) initials.style.display = 'none';
    };
    reader.readAsDataURL(file);
}

function updateAvatarPreview(fullName) {
    const initials = document.getElementById('photoPreviewInitials');
    const image = document.getElementById('photoPreviewImg');
    if (!initials || (image && image.style.display !== 'none')) return;
    const parts = (fullName || '').trim().split(/\s+/).filter(Boolean);
    initials.textContent = parts.length > 1 ? (parts[0][0] + parts[parts.length - 1][0]).toUpperCase() : (parts[0] || '?')[0].toUpperCase();
}

async function checkEmailAvailability(input) {
    const email = input.value.trim();
    const original = input.dataset.original || '';
    const indicator = document.getElementById('emailCheckIndicator');
    const error = document.getElementById('emailAvailError');
    if (!email || email === original || !email.includes('@')) return;
    if (indicator) { indicator.textContent = '...'; indicator.style.color = 'var(--text-muted)'; }
    try {
        const id = document.querySelector('input[name="id"]')?.value || '';
        const response = await fetch('/customers/api/check-email?email=' + encodeURIComponent(email) + '&excludeId=' + encodeURIComponent(id));
        if (!response.ok) throw new Error('availability request failed');
        const data = await response.json();
        if (indicator) { indicator.textContent = data.available ? '✓' : '✗'; indicator.style.color = data.available ? 'var(--badge-confirmed-txt)' : 'var(--error)'; }
        if (error) error.hidden = data.available;
        input.classList.toggle('is-invalid', !data.available);
    } catch (e) { if (indicator) indicator.textContent = ''; }
}

document.addEventListener('DOMContentLoaded', function () {
    const name = document.getElementById('fullName');
    const photo = document.getElementById('photoFile');
    const email = document.getElementById('email');
    if (name) name.addEventListener('input', function () { updateAvatarPreview(this.value); });
    if (photo) photo.addEventListener('change', function () { previewCustomerPhoto(this); });
    if (email) email.addEventListener('blur', function () { checkEmailAvailability(this); });
});
