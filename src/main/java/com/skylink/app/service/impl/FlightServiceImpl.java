package com.skylink.app.service.impl;

import com.skylink.app.entity.Flight;
import com.skylink.app.enums.FlightStatus;
import com.skylink.app.enums.SeatClass;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.BookingRepository;
import com.skylink.app.repository.FlightRepository;
import com.skylink.app.service.IFlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FlightServiceImpl implements IFlightService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findAll() {
        return flightRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Flight findById(Long id) {
        return flightRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> search(Long originId, Long destId,
                               LocalDateTime from, LocalDateTime to,
                               FlightStatus status) {
        return flightRepository.searchFlights(originId, destId, status, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> search(Long originId, Long destId,
                               LocalDate date, FlightStatus status) {
        return searchFull(originId, destId, status, date, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> searchFull(Long originId, Long destId,
                                   FlightStatus status, LocalDate date,
                                   String flightNumber) {
        LocalDateTime from = date == null ? null : date.atStartOfDay();
        LocalDateTime to = date == null ? null : date.plusDays(1).atStartOfDay();
        String normalizedNumber = flightNumber == null || flightNumber.isBlank()
            ? null : flightNumber.trim();
        return flightRepository.search(originId, destId, status, from, to, normalizedNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> findUpcoming(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return flightRepository
            .findByDepartureTimeAfterAndStatusOrderByDepartureTimeAsc(
                LocalDateTime.now(), FlightStatus.SCHEDULED)
            .stream()
            .limit(limit)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Flight> findInMonth(int year, int month) {
        LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
        return flightRepository.findByDepartureBetween(from, from.plusMonths(1));
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableSeats(Long flightId, SeatClass seatClass) {
        Flight flight = findById(flightId);
        return switch (seatClass) {
            case ECONOMY -> flight.getAvailableEconomySeats();
            case BUSINESS -> flight.getAvailableBusinessSeats();
            case FIRST_CLASS -> flight.getAvailableFirstClassSeats();
        };
    }

    @Override
    public Flight save(Flight flight) {
        validateFlightRules(flight, null);
        flight.setAvailableEconomySeats(flight.getAircraft().getEconomySeats());
        flight.setAvailableBusinessSeats(flight.getAircraft().getBusinessSeats());
        flight.setAvailableFirstClassSeats(flight.getAircraft().getFirstClassSeats());
        if (flight.getStatus() == null) {
            flight.setStatus(FlightStatus.SCHEDULED);
        }
        Flight saved = flightRepository.save(flight);
        log.info("Created flight: {} ({} -> {})", saved.getFlightNumber(),
            saved.getOriginAirport().getIataCode(), saved.getDestinationAirport().getIataCode());
        return saved;
    }

    @Override
    public Flight update(Long id, Flight updatedData) {
        Flight existing = findById(id);
        validateFlightRules(updatedData, id);

        boolean aircraftChanged = !existing.getAircraft().getId()
            .equals(updatedData.getAircraft().getId());
        if (aircraftChanged) {
            int bookedEconomy = existing.getAircraft().getEconomySeats()
                - existing.getAvailableEconomySeats();
            int bookedBusiness = existing.getAircraft().getBusinessSeats()
                - existing.getAvailableBusinessSeats();
            int bookedFirst = existing.getAircraft().getFirstClassSeats()
                - existing.getAvailableFirstClassSeats();
            if (bookedEconomy > updatedData.getAircraft().getEconomySeats()
                || bookedBusiness > updatedData.getAircraft().getBusinessSeats()
                || bookedFirst > updatedData.getAircraft().getFirstClassSeats()) {
                throw new BusinessRuleException(
                    "The new aircraft does not have enough seats for existing bookings.");
            }
            existing.setAvailableEconomySeats(
                updatedData.getAircraft().getEconomySeats() - bookedEconomy);
            existing.setAvailableBusinessSeats(
                updatedData.getAircraft().getBusinessSeats() - bookedBusiness);
            existing.setAvailableFirstClassSeats(
                updatedData.getAircraft().getFirstClassSeats() - bookedFirst);
        }

        existing.setFlightNumber(updatedData.getFlightNumber());
        existing.setOriginAirport(updatedData.getOriginAirport());
        existing.setDestinationAirport(updatedData.getDestinationAirport());
        existing.setDepartureTime(updatedData.getDepartureTime());
        existing.setArrivalTime(updatedData.getArrivalTime());
        existing.setAircraft(updatedData.getAircraft());
        existing.setStatus(updatedData.getStatus());
        existing.setEconomyPrice(updatedData.getEconomyPrice());
        existing.setBusinessPrice(updatedData.getBusinessPrice());
        existing.setFirstClassPrice(updatedData.getFirstClassPrice());

        Flight saved = flightRepository.save(existing);
        log.info("Updated flight: {}", saved.getFlightNumber());
        return saved;
    }

    @Override
    public void delete(Long id) {
        Flight flight = findById(id);
        long bookingCount = bookingRepository.findByFlight(flight).size();
        if (bookingCount > 0) {
            throw new BusinessRuleException("Cannot delete flight '" + flight.getFlightNumber()
                + "' - it has " + bookingCount + " existing booking(s).");
        }
        flightRepository.delete(flight);
        log.info("Deleted flight: {}", flight.getFlightNumber());
    }

    @Override
    public void decrementSeats(Long flightId, SeatClass seatClass, int count) {
        validateSeatChange(count);
        Flight flight = findById(flightId);
        switch (seatClass) {
            case ECONOMY -> flight.setAvailableEconomySeats(
                decrease(flight.getAvailableEconomySeats(), count, "economy", flight));
            case BUSINESS -> flight.setAvailableBusinessSeats(
                decrease(flight.getAvailableBusinessSeats(), count, "business", flight));
            case FIRST_CLASS -> flight.setAvailableFirstClassSeats(
                decrease(flight.getAvailableFirstClassSeats(), count, "first class", flight));
        }
        log.info("Decremented {} {} seats on flight {}", count, seatClass, flight.getFlightNumber());
    }

    @Override
    public void incrementSeats(Long flightId, SeatClass seatClass, int count) {
        validateSeatChange(count);
        Flight flight = findById(flightId);
        switch (seatClass) {
            case ECONOMY -> flight.setAvailableEconomySeats(
                increase(flight.getAvailableEconomySeats(), count, flight.getAircraft().getEconomySeats()));
            case BUSINESS -> flight.setAvailableBusinessSeats(
                increase(flight.getAvailableBusinessSeats(), count, flight.getAircraft().getBusinessSeats()));
            case FIRST_CLASS -> flight.setAvailableFirstClassSeats(
                increase(flight.getAvailableFirstClassSeats(), count, flight.getAircraft().getFirstClassSeats()));
        }
        log.info("Incremented {} {} seats on flight {}", count, seatClass, flight.getFlightNumber());
    }

    private int decrease(int available, int count, String seatName, Flight flight) {
        if (available < count) {
            throw new BusinessRuleException("Not enough " + seatName
                + " seats available on flight " + flight.getFlightNumber());
        }
        return available - count;
    }

    private int increase(int available, int count, int capacity) {
        if (available > capacity - count) {
            throw new BusinessRuleException("Available seats cannot exceed aircraft capacity.");
        }
        return available + count;
    }

    private void validateSeatChange(int count) {
        if (count <= 0) {
            throw new BusinessRuleException("Seat count must be greater than zero.");
        }
    }

    private void validateFlightRules(Flight flight, Long excludeId) {
        if (flight.getFlightNumber() == null || flight.getFlightNumber().isBlank()) {
            throw new BusinessRuleException("Flight number is required.");
        }
        boolean duplicate = excludeId == null
            ? flightRepository.existsByFlightNumber(flight.getFlightNumber())
            : flightRepository.existsByFlightNumberAndIdNot(flight.getFlightNumber(), excludeId);
        if (duplicate) {
            throw new BusinessRuleException("Flight number '" + flight.getFlightNumber()
                + "' is already in use.");
        }
        if (flight.getDepartureTime() == null || flight.getArrivalTime() == null) {
            throw new BusinessRuleException("Departure and arrival times are required.");
        }
        if (!flight.getArrivalTime().isAfter(flight.getDepartureTime())) {
            throw new BusinessRuleException("Arrival time must be after departure time.");
        }
        if (flight.getOriginAirport() == null || flight.getDestinationAirport() == null
            || flight.getOriginAirport().getId().equals(flight.getDestinationAirport().getId())) {
            throw new BusinessRuleException(
                "Origin and destination airports cannot be the same.");
        }
        if (flight.getAircraft() == null) {
            throw new BusinessRuleException("Aircraft is required.");
        }
    }
}
