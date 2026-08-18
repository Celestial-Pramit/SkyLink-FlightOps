package com.skylink.app.service.impl;

import com.skylink.app.dto.BookingCreateDto;
import com.skylink.app.entity.AppUser;
import com.skylink.app.entity.Booking;
import com.skylink.app.entity.Customer;
import com.skylink.app.entity.Flight;
import com.skylink.app.enums.BookingStatus;
import com.skylink.app.enums.FlightStatus;
import com.skylink.app.enums.SeatClass;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.BookingRepository;
import com.skylink.app.repository.CustomerRepository;
import com.skylink.app.repository.FlightRepository;
import com.skylink.app.service.IBookingService;
import com.skylink.app.service.IFlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final CustomerRepository customerRepository;
    private final IFlightService flightService;

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByCreatedBy(AppUser user) {
        return bookingRepository.findByCreatedBy(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findByReference(String reference) {
        return bookingRepository.findByBookingReference(reference);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByCustomer(Customer customer) {
        return bookingRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByFlight(Flight flight) {
        return bookingRepository.findByFlight(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findRecent(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return bookingRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodaysBookings() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return bookingRepository.countBookingsToday(start, start.plusDays(1));
    }

    @Override
    public Booking createBooking(BookingCreateDto dto, AppUser createdBy) {
        if (dto.getPassengerCount() < 1 || dto.getPassengerCount() > 9) {
            throw new BusinessRuleException("Passenger count must be between 1 and 9.");
        }

        Flight flight = flightRepository.findById(dto.getFlightId())
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + dto.getFlightId()));
        Customer customer = customerRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + dto.getCustomerId()));

        if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.ARRIVED
                || flight.getStatus() == FlightStatus.DEPARTED) {
            throw new BusinessRuleException("Cannot book a flight that is no longer schedulable.");
        }

        int available = flightService.getAvailableSeats(flight.getId(), dto.getSeatClass());
        if (available < dto.getPassengerCount()) {
            throw new BusinessRuleException("Not enough " + dto.getSeatClass().name().toLowerCase()
                + " seats available. Only " + available + " seat(s) left.");
        }

        BigDecimal unitPrice = getPriceForClass(flight, dto.getSeatClass());
        if (unitPrice == null) {
            throw new BusinessRuleException("Price is not configured for the selected seat class.");
        }

        String reference = generateReference();
        Booking booking = Booking.builder()
            .bookingReference(reference)
            .flight(flight)
            .customer(customer)
            .createdBy(createdBy)
            .seatClass(dto.getSeatClass())
            .passengerCount(dto.getPassengerCount())
            .totalAmount(unitPrice.multiply(BigDecimal.valueOf(dto.getPassengerCount())))
            .status(BookingStatus.CONFIRMED)
            .notes(dto.getNotes())
            .build();

        Booking saved = bookingRepository.save(booking);
        flightService.decrementSeats(flight.getId(), dto.getSeatClass(), dto.getPassengerCount());
        log.info("Booking created: {} for customer {} on flight {}", reference,
            customer.getFullName(), flight.getFlightNumber());
        return saved;
    }

    @Override
    public Booking updateStatus(Long id, BookingStatus newStatus) {
        Booking booking = getBooking(id);
        BookingStatus oldStatus = booking.getStatus();

        if (newStatus == null) {
            throw new BusinessRuleException("Booking status is required.");
        }
        if (oldStatus == BookingStatus.CANCELLED && newStatus != BookingStatus.CANCELLED) {
            throw new BusinessRuleException("A cancelled booking cannot be reactivated.");
        }

        booking.setStatus(newStatus);
        if (newStatus == BookingStatus.CANCELLED && oldStatus != BookingStatus.CANCELLED) {
            flightService.incrementSeats(booking.getFlight().getId(), booking.getSeatClass(), booking.getPassengerCount());
            booking.setCancelledAt(LocalDateTime.now());
        }
        return bookingRepository.save(booking);
    }

    @Override
    public void cancelBooking(Long id, AppUser requestedBy) {
        Booking booking = getBooking(id);
        boolean admin = requestedBy.hasRole("ROLE_ADMIN") || requestedBy.hasRole("ROLE_SUPER_ADMIN");

        if (!admin && (booking.getCreatedBy() == null || !booking.getCreatedBy().getId().equals(requestedBy.getId()))) {
            throw new BusinessRuleException("You can only cancel bookings you created.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessRuleException("Booking " + booking.getBookingReference() + " is already cancelled.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.BOARDED) {
            throw new BusinessRuleException("Cannot cancel a booking that is "
                + booking.getStatus().name().toLowerCase() + ".");
        }

        LocalDateTime departure = booking.getFlight().getDepartureTime();
        if (departure != null && !LocalDateTime.now().isBefore(departure.minusHours(2))) {
            throw new BusinessRuleException("Cannot cancel booking " + booking.getBookingReference()
                + " - departure is less than 2 hours away.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);
        flightService.incrementSeats(booking.getFlight().getId(), booking.getSeatClass(), booking.getPassengerCount());
        log.info("Booking {} cancelled by {}", booking.getBookingReference(), requestedBy.getEmail());
    }

    @Override
    public void deleteBooking(Long id) {
        Booking booking = getBooking(id);
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            flightService.incrementSeats(booking.getFlight().getId(), booking.getSeatClass(), booking.getPassengerCount());
        }
        bookingRepository.delete(booking);
    }

    private Booking getBooking(Long id) {
        return bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    private BigDecimal getPriceForClass(Flight flight, SeatClass seatClass) {
        return switch (seatClass) {
            case ECONOMY -> flight.getEconomyPrice();
            case BUSINESS -> flight.getBusinessPrice();
            case FIRST_CLASS -> flight.getFirstClassPrice();
        };
    }

    private String generateReference() {
        int year = LocalDate.now().getYear();
        long next = bookingRepository.count() + 1;
        String reference = formatReference(year, next);
        while (bookingRepository.findByBookingReference(reference).isPresent()) {
            reference = formatReference(year, ++next);
        }
        return reference;
    }

    private String formatReference(int year, long sequence) {
        return String.format("SKY-%d%05d", year, sequence);
    }
}
