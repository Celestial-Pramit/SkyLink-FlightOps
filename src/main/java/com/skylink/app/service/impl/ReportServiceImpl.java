package com.skylink.app.service.impl;

import com.skylink.app.enums.BookingStatus;
import com.skylink.app.record.BookingsByDay;
import com.skylink.app.record.RevenueByRoute;
import com.skylink.app.record.StatusBreakdown;
import com.skylink.app.record.TopRoute;
import com.skylink.app.repository.BookingRepository;
import com.skylink.app.repository.CustomerRepository;
import com.skylink.app.repository.FlightRepository;
import com.skylink.app.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements IReportService {

    private static final List<BookingStatus> STATUS_ORDER = List.of(
        BookingStatus.CONFIRMED, BookingStatus.PENDING,
        BookingStatus.CANCELLED, BookingStatus.BOARDED,
        BookingStatus.COMPLETED
    );

    private static final Map<BookingStatus, String> STATUS_COLORS = Map.of(
        BookingStatus.CONFIRMED, "#137333",
        BookingStatus.PENDING, "#b06000",
        BookingStatus.CANCELLED, "#ba1a1a",
        BookingStatus.BOARDED, "#3949ab",
        BookingStatus.COMPLETED, "#2e7d32"
    );

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final FlightRepository flightRepository;

    @Override
    public List<BookingsByDay> getBookingsByDay(int days) {
        LocalDate end = LocalDate.now().plusDays(1);
        LocalDate start = end.minusDays(Math.max(1, days));
        Map<LocalDate, Long> counts = new java.util.HashMap<>();

        for (Object[] row : bookingRepository.countBookingsPerDay(
                start.atStartOfDay(), end.atStartOfDay())) {
            counts.put(toDate(row[0]), toLong(row[1]));
        }

        List<BookingsByDay> result = new ArrayList<>();
        for (LocalDate day = start; day.isBefore(end); day = day.plusDays(1)) {
            result.add(new BookingsByDay(
                day.getMonthValue() + "/" + day.getDayOfMonth(),
                counts.getOrDefault(day, 0L)));
        }
        return result;
    }

    @Override
    public List<RevenueByRoute> getRevenueByRoute(int limit) {
        return bookingRepository.findRevenueByRoute(PageRequest.of(0, Math.max(1, limit)))
            .stream()
            .map(row -> new RevenueByRoute(
                route(row[0], row[2]), toMoney(row[4]), toLong(row[5])))
            .toList();
    }

    @Override
    public List<StatusBreakdown> getStatusBreakdown() {
        Map<BookingStatus, Long> counts = new EnumMap<>(BookingStatus.class);
        for (Object[] row : bookingRepository.countByStatus()) {
            BookingStatus status = parseStatus(row[0]);
            if (status != null) counts.put(status, toLong(row[1]));
        }
        return STATUS_ORDER.stream()
            .map(status -> new StatusBreakdown(
                status.name(), counts.getOrDefault(status, 0L), STATUS_COLORS.get(status)))
            .toList();
    }

    @Override
    public List<TopRoute> getTopRoutes(int limit) {
        return bookingRepository.findRevenueByRoute(PageRequest.of(0, Math.max(1, limit)))
            .stream()
            .map(row -> new TopRoute(
                text(row[0]), text(row[1]), text(row[2]), text(row[3]),
                toLong(row[5]), toMoney(row[4])))
            .toList();
    }

    @Override
    public long getTotalBookings() {
        return bookingRepository.count();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return toMoney(bookingRepository.calculateTotalRevenue());
    }

    @Override
    public long getTotalCustomers() {
        return customerRepository.count();
    }

    @Override
    public long getTotalFlights() {
        return flightRepository.count();
    }

    private LocalDate toDate(Object value) {
        if (value instanceof Date date) return date.toLocalDate();
        if (value instanceof LocalDate date) return date;
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate();
        }
        return LocalDate.parse(String.valueOf(value));
    }

    private long toLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    private BigDecimal toMoney(Object value) {
        if (value == null) return BigDecimal.ZERO;
        return value instanceof BigDecimal money
            ? money : new BigDecimal(value.toString());
    }

    private BookingStatus parseStatus(Object value) {
        if (value instanceof BookingStatus status) return status;
        try {
            return BookingStatus.valueOf(String.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String route(Object origin, Object destination) {
        return text(origin) + " -> " + text(destination);
    }

    private String text(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
