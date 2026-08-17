package com.skylink.app.record;

import com.skylink.app.entity.Flight;

import java.time.LocalDate;
import java.util.List;

/** Read-only day projection used by the flight schedule calendar. */
public record FlightCalendarDay(
    LocalDate date,
    List<Flight> flights,
    boolean isToday,
    boolean isCurrentMonth
) {
    public boolean hasFlights() {
        return flights != null && !flights.isEmpty();
    }

    public int flightCount() {
        return flights == null ? 0 : flights.size();
    }

    public int dayOfMonth() {
        return date.getDayOfMonth();
    }
}
