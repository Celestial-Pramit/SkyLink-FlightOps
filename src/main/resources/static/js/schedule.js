var selectedDay = null;

document.addEventListener('DOMContentLoaded', function () {
    var detail = document.getElementById('calDetail');
    if (detail) detail.style.display = 'none';
});

function selectDay(dateValue) {
    document.querySelectorAll('.cal-cell--selected').forEach(function (cell) {
        cell.classList.remove('cal-cell--selected');
    });
    var cell = document.querySelector('[data-date="' + dateValue + '"]');
    if (cell) cell.classList.add('cal-cell--selected');
    selectedDay = dateValue;
    showDetailPanel(dateValue);
}

function showDetailPanel(dateValue) {
    var panel = document.getElementById('calDetail');
    var dateElement = document.getElementById('detailDate');
    var countElement = document.getElementById('detailCount');
    var listElement = document.getElementById('detailList');
    if (!panel || !listElement) return;

    var dayFlights = (window.flightData || []).filter(function (flight) {
        return flight.departureTime && localDate(flight.departureTime) === dateValue;
    });
    var date = new Date(dateValue + 'T00:00:00');
    if (dateElement) dateElement.textContent = date.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' });
    if (countElement) countElement.textContent = dayFlights.length + ' flight' + (dayFlights.length === 1 ? '' : 's');

    listElement.innerHTML = '';
    if (!dayFlights.length) {
        listElement.innerHTML = '<div class="calendar-detail-empty">No flights on this day.</div>';
    } else {
        dayFlights.forEach(function (flight) {
            var origin = flight.originAirport && flight.originAirport.iataCode || '???';
            var destination = flight.destinationAirport && flight.destinationAirport.iataCode || '???';
            var status = (flight.status || 'SCHEDULED').toLowerCase();
            var row = document.createElement('div');
            row.className = 'detail-flight-row';
            row.innerHTML = '<div class="detail-flight-route"><span class="detail-iata">' + escapeHtml(origin) + '</span>' +
                '<span class="material-symbols-outlined detail-plane">flight</span><span class="detail-iata">' + escapeHtml(destination) + '</span></div>' +
                '<div class="detail-flight-info"><span class="detail-flight-num">' + escapeHtml(flight.flightNumber || '-') + '</span>' +
                '<span class="detail-time">' + timeOf(flight.departureTime) + ' - ' + timeOf(flight.arrivalTime) + '</span></div>' +
                '<span class="badge-status badge-' + status + '">' + status.toUpperCase() + '</span>' +
                '<a class="btn-icon" href="/flights/' + encodeURIComponent(flight.id) + '" title="View"><span class="material-symbols-outlined">open_in_new</span></a>';
            listElement.appendChild(row);
        });
    }
    panel.style.display = 'flex';
}

function closeDetail() {
    var panel = document.getElementById('calDetail');
    if (panel) panel.style.display = 'none';
    document.querySelectorAll('.cal-cell--selected').forEach(function (cell) { cell.classList.remove('cal-cell--selected'); });
    selectedDay = null;
}

function localDate(value) {
    var date = new Date(value);
    return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
}

function timeOf(value) {
    if (!value) return '--:--';
    return new Date(value).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, function (character) {
        return {'&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#039;'}[character];
    });
}
