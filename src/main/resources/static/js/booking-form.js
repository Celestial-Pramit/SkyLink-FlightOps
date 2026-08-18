function updateCustomerSummary(select) {
    const option = select && select.selectedOptions ? select.selectedOptions[0] : null;
    const chip = document.getElementById('selectedCustomerChip');
    if (!option || !option.value) {
        if (chip) chip.hidden = true;
        setSummaryValue('summaryCustomer', '-');
        return;
    }
    const name = option.dataset.name || option.textContent.trim();
    const email = option.dataset.email || '';
    const avatar = document.getElementById('sccAvatar');
    if (avatar) {
        avatar.textContent = (name.charAt(0) || '?').toUpperCase();
        avatar.style.background = ['#68519d','#3b82f6','#22c55e','#f59e0b','#ef4444','#06b6d4'][name.charCodeAt(0) % 6];
    }
    setSummaryValue('sccName', name);
    setSummaryValue('sccEmail', email);
    if (chip) chip.hidden = false;
    setSummaryValue('summaryCustomer', name);
}

function updateClassSelection() {
    document.querySelectorAll('.seat-class-card').forEach(function (card) {
        const radio = card.querySelector('input[type="radio"]');
        card.classList.toggle('seat-class-card--selected', Boolean(radio && radio.checked));
    });
    const selected = document.querySelector('.seat-class-card input[type="radio"]:checked');
    const labels = {ECONOMY: 'Economy', BUSINESS: 'Business', FIRST_CLASS: 'First Class'};
    setSummaryValue('summaryClass', selected ? labels[selected.value] || selected.value : '-');
}

function updatePassengerDisplay(value) {
    const count = Number(value) || 1;
    setSummaryValue('passengerDisplay', count + (count === 1 ? ' passenger' : ' passengers'));
    setSummaryValue('summaryPassengers', count + ' pax');
}

function updateSummaryPrice() {
    const selected = document.querySelector('.seat-class-card input[type="radio"]:checked');
    const passengers = Number(document.getElementById('passengerCount')?.value || 1);
    if (!selected || typeof flightPrices === 'undefined') {
        setSummaryValue('summaryUnitPrice', '-');
        setSummaryValue('summaryTotal', '-');
        return;
    }
    const prices = {ECONOMY: flightPrices.economy, BUSINESS: flightPrices.business, FIRST_CLASS: flightPrices.firstClass};
    const unitPrice = Number(prices[selected.value] || 0);
    const formatter = new Intl.NumberFormat('en-IN', {maximumFractionDigits: 0});
    setSummaryValue('summaryUnitPrice', 'BDT ' + formatter.format(unitPrice));
    setSummaryValue('summaryTotal', 'BDT ' + formatter.format(unitPrice * passengers));
}

function setSummaryValue(id, value) {
    const element = document.getElementById(id);
    if (element) element.textContent = value;
}
