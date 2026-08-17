function previewImage(input) {
    const warning = document.getElementById('fileSizeWarning');
    const buttonText = document.getElementById('uploadBtnText');
    const file = input.files && input.files[0];
    if (!file) return;
    if (file.size > 2 * 1024 * 1024) {
        warning.style.display = 'flex';
        input.value = '';
        return;
    }
    warning.style.display = 'none';
    const reader = new FileReader();
    reader.onload = function (event) {
        let image = document.getElementById('previewImg');
        if (!image) {
            image = document.createElement('img');
            image.id = 'previewImg';
            image.className = 'image-preview__img';
            document.getElementById('imagePreview').prepend(image);
        }
        image.src = event.target.result;
        image.style.display = 'block';
        const placeholder = document.getElementById('imagePlaceholder');
        if (placeholder) placeholder.style.display = 'none';
        if (buttonText) buttonText.textContent = 'Change photo';
    };
    reader.readAsDataURL(file);
}

function updateSeatBar() {
    const total = Number(document.getElementById('totalSeats')?.value) || 0;
    const eco = Number(document.getElementById('ecoSeats')?.value) || 0;
    const biz = Number(document.getElementById('bizSeats')?.value) || 0;
    const first = Number(document.getElementById('firstSeats')?.value) || 0;
    const parts = [['previewEco', eco], ['previewBiz', biz], ['previewFirst', first]];
    parts.forEach(function ([id, value]) {
        const element = document.getElementById(id);
        if (element) element.style.width = total > 0 ? (value * 100 / total) + '%' : '0%';
    });
    const difference = total - eco - biz - first;
    const warning = document.getElementById('seatMismatchWarning');
    const diff = document.getElementById('seatDiff');
    if (warning && diff) {
        warning.style.display = total > 0 && difference !== 0 ? 'flex' : 'none';
        diff.textContent = Math.abs(difference) + (difference > 0 ? ' unassigned' : ' over by');
    }
}

function handleManufacturerChange(select) {
    const custom = document.getElementById('manufacturerCustom');
    if (!custom) return;
    custom.style.display = select.value === 'Other' ? 'block' : 'none';
    custom.required = select.value === 'Other';
    if (select.value !== 'Other') custom.value = '';
}

function updateStatusCard() {
    document.querySelectorAll('.status-radio-card').forEach(function (card) {
        const radio = card.querySelector('input[type="radio"]');
        card.classList.toggle('status-radio-card--selected', Boolean(radio && radio.checked));
    });
}

document.addEventListener('DOMContentLoaded', function () {
    updateSeatBar();
    updateStatusCard();
    const manufacturer = document.getElementById('manufacturer');
    if (manufacturer) handleManufacturerChange(manufacturer);
    const form = document.getElementById('aircraftForm');
    const custom = document.getElementById('manufacturerCustom');
    if (form && manufacturer && custom) {
        form.addEventListener('submit', function () {
            if (manufacturer.value === 'Other' && custom.value.trim()) {
                const option = new Option(custom.value.trim(), custom.value.trim(), true, true);
                manufacturer.add(option);
                manufacturer.value = custom.value.trim();
            }
        });
    }
});
