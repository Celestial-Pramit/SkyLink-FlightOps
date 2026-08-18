package com.skylink.app.dto;

import com.skylink.app.enums.BookingStatus;
import com.skylink.app.enums.SeatClass;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BookingDto {

    private Long id;
    private String bookingReference;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String flightNumber;
    private String originIata;
    private String destinationIata;
    private String originCity;
    private String destinationCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String formattedDuration;
    private String aircraftModel;
    private SeatClass seatClass;
    private int passengerCount;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private String notes;
    private BookingStatus newStatus;

    public String getRoute() {
        if (originIata == null || destinationIata == null) {
            return "";
        }
        return originIata + " -> " + destinationIata;
    }

    public String getStatusCssClass() {
        return status == null ? "" : "badge-" + status.name().toLowerCase();
    }

    public boolean isCancellable() {
        if (status == null || status == BookingStatus.BOARDED
                || status == BookingStatus.COMPLETED
                || status == BookingStatus.CANCELLED) {
            return false;
        }
        return departureTime == null
                || LocalDateTime.now().isBefore(departureTime.minusHours(2));
    }
}
