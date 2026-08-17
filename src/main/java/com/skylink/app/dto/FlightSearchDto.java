package com.skylink.app.dto;

import com.skylink.app.enums.FlightStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Optional filter parameters for the flight list page. */
@Data
@NoArgsConstructor
public class FlightSearchDto {

    private Long originAirportId;
    private Long destinationAirportId;
    private FlightStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    private String flightNumber;

    public boolean isEmpty() {
        return originAirportId == null
            && destinationAirportId == null
            && status == null
            && departureDate == null
            && (flightNumber == null || flightNumber.isBlank());
    }
}
