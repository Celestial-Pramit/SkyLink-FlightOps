/* ============================================================
   seat-map.js — SkyLink Interactive Cabin Seat Map
   ============================================================ */

(function () {
    'use strict';

    // ── Config injected from Thymeleaf via data attributes ──
    var mapEl = document.getElementById('seatMapRoot');
    if (!mapEl) return;

    var totalEconomy    = parseInt(mapEl.dataset.economyTotal    || '0', 10);
    var totalBusiness   = parseInt(mapEl.dataset.businessTotal   || '0', 10);
    var totalFirst      = parseInt(mapEl.dataset.firstTotal      || '0', 10);
    var takenEconomy    = parseInt(mapEl.dataset.economyTaken    || '0', 10);
    var takenBusiness   = parseInt(mapEl.dataset.businessTaken   || '0', 10);
    var takenFirst      = parseInt(mapEl.dataset.firstTaken      || '0', 10);
    var maxPassengers   = parseInt(mapEl.dataset.maxPassengers   || '1', 10);
    var priceEconomy    = parseFloat(mapEl.dataset.priceEconomy  || '0');
    var priceBusiness   = parseFloat(mapEl.dataset.priceBusiness || '0');
    var priceFirst      = parseFloat(mapEl.dataset.priceFirst    || '0');

    // ── State ──
    var selectedSeats  = [];   // array of {id, seatClass, label}
    var currentClass   = null; // 'ECONOMY' | 'BUSINESS' | 'FIRST_CLASS'

    // ── DOM refs ──
    var hiddenSeatClass   = document.getElementById('seatClassInput');
    var selectedDisplay   = document.getElementById('selectedSeatsDisplay');
    var selectedChipsWrap = document.getElementById('selectedChips');
    var selectedCountEl   = document.getElementById('selectedCount');
    var seatErrorEl       = document.getElementById('seatMapError');

    // ── Render the cabin ──
    function renderCabin() {
        var cabin = document.getElementById('cabinBody');
        if (!cabin) return;
        cabin.innerHTML = '';

        // Legend
        cabin.innerHTML += buildLegend();

        // First Class zone
        if (totalFirst > 0) {
            cabin.appendChild(buildZone(
                'FIRST_CLASS', 'First Class',
                'star', totalFirst, takenFirst,
                4, 2, priceFirst, 'seat--first'
            ));
        }

        // Business zone
        if (totalBusiness > 0) {
            cabin.appendChild(buildZone(
                'BUSINESS', 'Business',
                'airline_seat_flat', totalBusiness, takenBusiness,
                4, 2, priceBusiness, 'seat--business'
            ));
        }

        // Economy zone
        if (totalEconomy > 0) {
            cabin.appendChild(buildZone(
                'ECONOMY', 'Economy',
                'airline_seat_recline_normal', totalEconomy, takenEconomy,
                6, 3, priceEconomy, 'seat--economy'
            ));
        }
    }

    function buildLegend() {
        return '<div class="seat-legend">'
            + '<div class="seat-legend-item"><div class="seat-legend-dot seat-legend-dot--available"></div>Available</div>'
            + '<div class="seat-legend-item"><div class="seat-legend-dot seat-legend-dot--taken"></div>Booked</div>'
            + '<div class="seat-legend-item"><div class="seat-legend-dot seat-legend-dot--selected"></div>Selected</div>'
            + '</div>';
    }

    function buildZone(seatClass, label, icon, total, taken, seatsPerRow, leftCount, price, extraClass) {
        var zone = document.createElement('div');
        zone.className = 'cabin-zone';

        var priceLabel = price > 0
            ? 'BDT ' + new Intl.NumberFormat('en-IN', {maximumFractionDigits: 0}).format(price)
            : '';

        zone.innerHTML = '<div class="cabin-zone-label">'
            + '<span class="material-symbols-outlined zone-icon">' + icon + '</span>'
            + label
            + (priceLabel ? '<span class="zone-price">' + priceLabel + '</span>' : '')
            + '</div>';

        var rows = Math.ceil(total / seatsPerRow);
        var rightCount = seatsPerRow - leftCount;
        var seatIndex  = 0;
        var colLabels  = 'ABCDEFGHJK'.split('');

        for (var r = 0; r < rows; r++) {
            var rowEl  = document.createElement('div');
            rowEl.className = 'seat-row';

            // Row number
            var numEl = document.createElement('div');
            numEl.className = 'seat-row-num';
            numEl.textContent = r + 1;
            rowEl.appendChild(numEl);

            // Left block
            for (var c = 0; c < leftCount; c++) {
                if (seatIndex >= total) break;
                var seatId = seatClass + '_' + (r + 1) + colLabels[c];
                var isTaken = seatIndex < taken;
                rowEl.appendChild(buildSeat(
                    seatId, (r + 1) + colLabels[c],
                    seatClass, isTaken, extraClass));
                seatIndex++;
            }

            // Aisle
            var aisle = document.createElement('div');
            aisle.className = 'seat-aisle';
            rowEl.appendChild(aisle);

            // Right block
            for (var d = 0; d < rightCount; d++) {
                if (seatIndex >= total) break;
                var seatIdR = seatClass + '_' + (r + 1) + colLabels[leftCount + d];
                var isTakenR = seatIndex < taken;
                rowEl.appendChild(buildSeat(
                    seatIdR, (r + 1) + colLabels[leftCount + d],
                    seatClass, isTakenR, extraClass));
                seatIndex++;
            }

            zone.appendChild(rowEl);
        }

        return zone;
    }

    function buildSeat(id, label, seatClass, isTaken, extraClass) {
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.dataset.seatId    = id;
        btn.dataset.seatClass = seatClass;
        btn.dataset.label     = label;
        btn.textContent       = label;
        btn.className = 'seat ' + extraClass + (isTaken ? ' seat--taken' : ' seat--available');
        btn.disabled  = isTaken;
        btn.setAttribute('aria-label', (isTaken ? 'Booked' : 'Available') + ' seat ' + label);

        if (!isTaken) {
            btn.addEventListener('click', function () {
                onSeatClick(this);
            });
        }
        return btn;
    }

    // ── Seat click handler ──
    function onSeatClick(btn) {
        var id        = btn.dataset.seatId;
        var seatClass = btn.dataset.seatClass;
        var label     = btn.dataset.label;

        // Deselect if already selected
        var existing = selectedSeats.findIndex(function (s) { return s.id === id; });
        if (existing !== -1) {
            selectedSeats.splice(existing, 1);
            btn.classList.remove('seat--selected');
            btn.classList.add('seat--available');
            updateState();
            return;
        }

        // If switching class — deselect all previous
        if (currentClass && currentClass !== seatClass) {
            deselectAll();
        }

        // Max passengers limit
        if (selectedSeats.length >= maxPassengers) {
            // Deselect the first selected seat to allow re-selection
            var oldest = selectedSeats.shift();
            var oldBtn = document.querySelector('[data-seat-id="' + oldest.id + '"]');
            if (oldBtn) {
                oldBtn.classList.remove('seat--selected');
                oldBtn.classList.add('seat--available');
            }
        }

        // Select this seat
        selectedSeats.push({ id: id, seatClass: seatClass, label: label });
        currentClass = seatClass;
        btn.classList.remove('seat--available');
        btn.classList.add('seat--selected');

        updateState();
    }

    function deselectAll() {
        selectedSeats.forEach(function (s) {
            var btn = document.querySelector('[data-seat-id="' + s.id + '"]');
            if (btn) {
                btn.classList.remove('seat--selected');
                btn.classList.add('seat--available');
            }
        });
        selectedSeats = [];
        currentClass  = null;
    }

    // ── Update hidden input + summary ──
    function updateState() {
        if (selectedSeats.length === 0) {
            hiddenSeatClass.value = '';
            currentClass = null;
        } else {
            hiddenSeatClass.value = currentClass;
        }

        // Update passenger slider to match seats selected
        var slider = document.getElementById('passengerCount');
        if (slider && selectedSeats.length > 0) {
            slider.value = selectedSeats.length;
            updatePassengerDisplay(selectedSeats.length);
        }

        // Update selected chips display
        if (selectedSeats.length > 0) {
            selectedDisplay.classList.add('visible');
            selectedChipsWrap.innerHTML = selectedSeats.map(function (s) {
                return '<span class="selected-seat-chip">'
                    + '<span class="material-symbols-outlined" style="font-size:12px">event_seat</span>'
                    + s.label + '</span>';
            }).join('');
            selectedCountEl.textContent =
                selectedSeats.length + ' of ' + maxPassengers + ' seats';
        } else {
            selectedDisplay.classList.remove('visible');
            selectedChipsWrap.innerHTML = '';
            selectedCountEl.textContent = '';
        }

        // Update booking summary class
        var classLabels = {
            ECONOMY: 'Economy',
            BUSINESS: 'Business',
            FIRST_CLASS: 'First Class'
        };
        setSummaryValue('summaryClass',
            currentClass ? classLabels[currentClass] : '-');

        // Update summary price
        updateSummaryPrice();

        // Hide error if seats selected
        if (selectedSeats.length > 0 && seatErrorEl) {
            seatErrorEl.classList.remove('visible');
        }
    }

    // ── Validate on form submit ──
    var form = document.getElementById('createBookingForm');
    if (form) {
        form.addEventListener('submit', function (e) {
            if (!hiddenSeatClass.value) {
                e.preventDefault();
                if (seatErrorEl) seatErrorEl.classList.add('visible');
                document.getElementById('cabinBody')
                    .scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    // ── Init ──
    renderCabin();

    // Sync passenger slider max with selected class availability
    var slider = document.getElementById('passengerCount');
    if (slider) {
        slider.addEventListener('input', function () {
            // If more passengers than selected seats, deselect won't happen
            // Just update display
            updatePassengerDisplay(this.value);
            updateSummaryPrice();
        });
    }

})();