package com.skylink.app.record;

import com.skylink.app.enums.FlightStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only projection for the dashboard's upcoming departure card. */
public record UpcomingFlightCard(
    Long flightId,
    String flightNumber,
    String originIata,
    String originCity,
    String destinationIata,
    String destinationCity,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    String formattedDuration,
    String aircraftModel,
    BigDecimal economyPrice,
    int availableEconomySeats,
    int availableBusinessSeats,
    FlightStatus status
) {
    public boolean isAlmostFull() {
        return availableEconomySeats < 10 && availableEconomySeats > 0;
    }
}
