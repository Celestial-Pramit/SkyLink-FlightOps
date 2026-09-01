package com.skylink.app.service.impl;

import com.skylink.app.entity.Flight;
import com.skylink.app.enums.AircraftStatus;
import com.skylink.app.enums.FlightStatus;
import com.skylink.app.record.*;
import com.skylink.app.repository.AircraftRepository;
import com.skylink.app.repository.BookingRepository;
import com.skylink.app.repository.CustomerRepository;
import com.skylink.app.repository.FlightRepository;
import com.skylink.app.service.IBookingService;
import com.skylink.app.service.IDashboardService;
import com.skylink.app.service.IFlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements IDashboardService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final AircraftRepository aircraftRepository;

    @Override
    public DashboardStats getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        return new DashboardStats(
            flightRepository.count(),
            bookingRepository.countBookingsToday(startOfDay, startOfDay.plusDays(1)),
            customerRepository.count(),
            aircraftRepository.countByStatus(AircraftStatus.ACTIVE),
            bookingRepository.count(),
            totalRevenue != null ? totalRevenue : BigDecimal.ZERO
        );
    }

    @Override
    public List<RecentBookingRow> getRecentBookings(int limit) {
        return bookingRepository.findRecent(PageRequest.of(0, limit))
            .stream()
            .map(booking -> new RecentBookingRow(
                booking.getId(),
                booking.getBookingReference(),
                booking.getCustomer().getFullName(),
                booking.getFlight().getFlightNumber(),
                booking.getFlight().getOriginAirport().getIataCode(),
                booking.getFlight().getDestinationAirport().getIataCode(),
                booking.getFlight().getDepartureTime(),
                booking.getSeatClass(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getCreatedBy().getFullName()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<UpcomingFlightCard> getNextDeparture() {
        return flightRepository.findUpcoming(LocalDateTime.now())
            .stream()
            .findFirst()
            .map(this::toUpcomingFlightCard);
    }

    private UpcomingFlightCard toUpcomingFlightCard(Flight flight) {
        return new UpcomingFlightCard(
            flight.getId(),
            flight.getFlightNumber(),
            flight.getOriginAirport().getIataCode(),
            flight.getOriginAirport().getCity(),
            flight.getDestinationAirport().getIataCode(),
            flight.getDestinationAirport().getCity(),
            flight.getDepartureTime(),
            flight.getArrivalTime(),
            flight.getFormattedDuration(),
            flight.getAircraft().getModelName(),
            flight.getEconomyPrice(),
            flight.getAvailableEconomySeats(),
            flight.getAvailableBusinessSeats(),
            flight.getStatus()
        );
    }

    @Override
    public List<WeeklyBookingPoint> getWeeklyBookingTrend() {
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        for (Object[] row : bookingRepository.countBookingsPerDayLastWeek(sevenDaysAgo)) {
            counts.put(toLocalDate(row[0]), ((Number) row[1]).longValue());
        }

        List<WeeklyBookingPoint> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(new WeeklyBookingPoint(
                day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                counts.getOrDefault(day, 0L)
            ));
        }
        return result;
    }

    @Override
    public List<Flight> getUrgentFlights() {
        LocalDateTime now = LocalDateTime.now();
        return flightRepository
            .findByDepartureTimeAfterAndStatusOrderByDepartureTimeAsc(now, FlightStatus.SCHEDULED)
            .stream()
            .filter(flight -> flight.getDepartureTime().isBefore(now.plusHours(2)))
            .toList();
    }

    private LocalDate toLocalDate(Object raw) {
        if (raw instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(raw.toString());
    }
}
