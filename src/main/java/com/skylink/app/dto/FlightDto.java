package com.skylink.app.dto;

import com.skylink.app.enums.FlightStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/** Form binding and validation object for adding or editing a flight. */
@Data
@NoArgsConstructor
public class FlightDto {

    private Long id;

    @NotBlank(message = "Flight number is required")
    @Pattern(regexp = "^[A-Z]{2,3}-\\d{2,4}$",
             message = "Flight number format must be like BS-141 or BG-0221")
    private String flightNumber;

    @NotNull(message = "Origin airport is required")
    private Long originAirportId;

    @NotNull(message = "Destination airport is required")
    private Long destinationAirportId;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime arrivalTime;

    @NotNull(message = "Aircraft is required")
    private Long aircraftId;

    @NotNull(message = "Status is required")
    private FlightStatus status;

    @NotNull(message = "Economy price is required")
    @DecimalMin(value = "0.01", message = "Economy price must be greater than 0")
    private BigDecimal economyPrice;

    @NotNull(message = "Business price is required")
    @DecimalMin(value = "0.01", message = "Business price must be greater than 0")
    private BigDecimal businessPrice;

    @NotNull(message = "First class price is required")
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal firstClassPrice;

    public boolean isArrivalAfterDeparture() {
        return departureTime == null || arrivalTime == null
            || arrivalTime.isAfter(departureTime);
    }

    public boolean isDifferentAirports() {
        return originAirportId == null || destinationAirportId == null
            || !originAirportId.equals(destinationAirportId);
    }

    public String getFormattedDuration() {
        if (departureTime == null || arrivalTime == null) {
            return null;
        }
        long minutes = Duration.between(departureTime, arrivalTime).toMinutes();
        return minutes <= 0 ? null : (minutes / 60) + "h " + (minutes % 60) + "m";
    }
}
