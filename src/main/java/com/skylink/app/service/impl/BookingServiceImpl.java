package com.skylink.app.service.impl;

import com.skylink.app.entity.AppUser;
import com.skylink.app.entity.Booking;
import com.skylink.app.entity.Customer;
import com.skylink.app.entity.Flight;
import com.skylink.app.enums.BookingStatus;
import com.skylink.app.enums.SeatClass;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.BookingRepository;
import com.skylink.app.service.IBookingService;
import com.skylink.app.service.IFlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // TODO Phase 2: replace with explicit bookingRepository.findRecent(PageRequest.of(0, limit))
        return List.of();
    }

    @Override
    public Booking createBooking(Flight flight, Customer customer,
                                 SeatClass seatClass, int passengers,
                                 AppUser createdBy) {
        // TODO Phase 6: validate seats, generate booking reference, persist, decrement seats
        return null;
    }

    @Override
    public Booking updateStatus(Long id, BookingStatus status) {
        // TODO Phase 6
        return null;
    }

    @Override
    public void cancelBooking(Long id, AppUser requestedBy) {
        // TODO Phase 6: ownership + business rules, restore seats
    }

    @Override
    public void deleteBooking(Long id) {
        Booking existing = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        bookingRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodaysBookings() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return bookingRepository.countBookingsToday(start, end);
    }
}
