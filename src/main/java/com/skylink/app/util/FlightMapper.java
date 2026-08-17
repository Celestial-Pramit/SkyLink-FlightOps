package com.skylink.app.util;

import com.skylink.app.dto.FlightDto;
import com.skylink.app.entity.Aircraft;
import com.skylink.app.entity.Airport;
import com.skylink.app.entity.Flight;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AircraftRepository;
import com.skylink.app.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maps flight form data to and from its JPA entity. */
@Component
@RequiredArgsConstructor
public class FlightMapper {

    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;

    public Flight fromDto(FlightDto dto) {
        Airport origin = airportRepository.findById(dto.getOriginAirportId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Origin airport not found: " + dto.getOriginAirportId()));
        Airport destination = airportRepository.findById(dto.getDestinationAirportId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Destination airport not found: " + dto.getDestinationAirportId()));
        Aircraft aircraft = aircraftRepository.findById(dto.getAircraftId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Aircraft not found: " + dto.getAircraftId()));

        return Flight.builder()
            .id(dto.getId())
            .flightNumber(dto.getFlightNumber())
            .originAirport(origin)
            .destinationAirport(destination)
            .departureTime(dto.getDepartureTime())
            .arrivalTime(dto.getArrivalTime())
            .aircraft(aircraft)
            .status(dto.getStatus())
            .economyPrice(dto.getEconomyPrice())
            .businessPrice(dto.getBusinessPrice())
            .firstClassPrice(dto.getFirstClassPrice())
            .build();
    }

    public FlightDto toDto(Flight flight) {
        FlightDto dto = new FlightDto();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setOriginAirportId(flight.getOriginAirport().getId());
        dto.setDestinationAirportId(flight.getDestinationAirport().getId());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setAircraftId(flight.getAircraft().getId());
        dto.setStatus(flight.getStatus());
        dto.setEconomyPrice(flight.getEconomyPrice());
        dto.setBusinessPrice(flight.getBusinessPrice());
        dto.setFirstClassPrice(flight.getFirstClassPrice());
        return dto;
    }
}
