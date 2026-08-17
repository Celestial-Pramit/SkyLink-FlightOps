package com.skylink.app.service;

import com.skylink.app.entity.AppUser;
import com.skylink.app.entity.Booking;
import com.skylink.app.entity.Customer;
import com.skylink.app.entity.Flight;
import com.skylink.app.enums.BookingStatus;
import com.skylink.app.enums.SeatClass;

import java.util.List;
import java.util.Optional;

public interface IBookingService {
    List<Booking> findAll();
    List<Booking> findByCreatedBy(AppUser user);
    Optional<Booking> findById(Long id);
    Optional<Booking> findByReference(String reference);
    List<Booking> findByCustomer(Customer customer);
    List<Booking> findByFlight(Flight flight);
    List<Booking> findRecent(int limit);
    Booking createBooking(Flight flight, Customer customer,
                          SeatClass seatClass, int passengers,
                          AppUser createdBy);
    Booking updateStatus(Long id, BookingStatus status);
    void cancelBooking(Long id, AppUser requestedBy);
    void deleteBooking(Long id);
    long countTodaysBookings();
}
