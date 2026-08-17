package com.skylink.app.record;

import com.skylink.app.enums.BookingStatus;
import com.skylink.app.enums.SeatClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only projection for one row in the dashboard's recent bookings table. */
public record RecentBookingRow(
    Long bookingId,
    String bookingReference,
    String customerName,
    String flightNumber,
    String originIata,
    String destinationIata,
    LocalDateTime departureTime,
    SeatClass seatClass,
    BigDecimal totalAmount,
    BookingStatus status,
    String createdByName
) {
    public String getRoute() {
        return originIata + " -> " + destinationIata;
    }

    public String getStatusCssClass() {
        return "badge-" + status.name().toLowerCase();
    }
}
