function updateRoutePreview() {
    var origin = document.getElementById('originAirportId');
    var destination = document.getElementById('destinationAirportId');
    var originOption = origin && origin.selectedOptions[0];
    var destinationOption = destination && destination.selectedOptions[0];
    var originIata = originOption && originOption.dataset.iata || '???';
    var destinationIata = destinationOption && destinationOption.dataset.iata || '???';

    setText('originIata', originIata);
    setText('originCity', originOption && originOption.dataset.city || 'Select origin');
    setText('destIata', destinationIata);
    setText('destCity', destinationOption && destinationOption.dataset.city || 'Select destination');
    setText('summaryOrigin', originIata);
    setText('summaryDest', destinationIata);

    var same = origin && destination && origin.value && origin.value === destination.value;
    toggle('sameAirportWarning', same);
    if (destination) destination.classList.toggle('is-invalid', Boolean(same));
}

function updateDuration() {
    var departure = document.getElementById('departureTime');
    var arrival = document.getElementById('arrivalTime');
    var display = document.getElementById('durationDisplay');
    if (!departure || !arrival || !departure.value || !arrival.value) {
        toggle('durationDisplay', false);
        setText('summaryDuration', '-');
        return;
    }

    var minutes = (new Date(arrival.value) - new Date(departure.value)) / 60000;
    if (minutes <= 0) {
        toggle('durationDisplay', false);
        setText('summaryDuration', '-');
        return;
    }
    var text = Math.floor(minutes / 60) + 'h ' + Math.round(minutes % 60) + 'm';
    if (display) display.style.display = 'inline';
    setText('durationValue', text);
    setText('summaryDuration', text);
}

function validateDepartureOrder() {
    var departure = document.getElementById('departureTime');
    var arrival = document.getElementById('arrivalTime');
    if (!departure || !arrival) return;
    var invalid = departure.value && arrival.value && new Date(arrival.value) <= new Date(departure.value);
    toggle('timeOrderWarning', invalid);
    arrival.classList.toggle('is-invalid', Boolean(invalid));
}

function showAircraftInfo() {
    var select = document.getElementById('aircraftId');
    var card = document.getElementById('aircraftInfoCard');
    if (!select || !card) return;
    var option = select.selectedOptions[0];
    if (!select.value || !option.dataset.total) {
        card.style.display = 'none';
        setText('summaryAircraft', '-');
        return;
    }
    setText('infoTotal', option.dataset.total + ' seats');
    setText('infoEco', option.dataset.eco + ' seats');
    setText('infoBiz', option.dataset.biz + ' seats');
    setText('infoFirst', option.dataset.first + ' seats');
    var firstRow = document.getElementById('firstRow');
    if (firstRow) firstRow.style.display = option.dataset.first === '0' ? 'none' : 'flex';
    card.style.display = 'block';
    setText('summaryAircraft', option.text.split(' - ')[0]);
}

function updateSummary() {
    var flightNumber = document.getElementById('flightNumber');
    var economy = document.getElementById('economyPrice');
    var business = document.getElementById('businessPrice');
    function updatePrice(input, target) {
        var value = parseFloat(input.value);
        setText(target, Number.isNaN(value) ? 'BDT -' : 'BDT ' + value.toLocaleString('en-IN'));
    }
    if (flightNumber) {
        var syncFlight = function () { setText('summaryFlightNum', flightNumber.value || '-'); };
        flightNumber.addEventListener('input', syncFlight);
        syncFlight();
    }
    if (economy) {
        var syncEconomy = function () { updatePrice(economy, 'summaryEcoPrice'); };
        economy.addEventListener('input', syncEconomy);
        syncEconomy();
    }
    if (business) {
        var syncBusiness = function () { updatePrice(business, 'summaryBizPrice'); };
        business.addEventListener('input', syncBusiness);
        syncBusiness();
    }
}

function setText(id, value) {
    var element = document.getElementById(id);
    if (element) element.textContent = value;
}

function toggle(id, visible) {
    var element = document.getElementById(id);
    if (element) element.style.display = visible ? 'block' : 'none';
}
