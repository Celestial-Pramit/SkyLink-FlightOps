package com.skylink.app.repository;

import com.skylink.app.entity.Booking;
import com.skylink.app.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingStatusHistoryRepository
        extends JpaRepository<BookingStatusHistory, Long> {

    List<BookingStatusHistory> findByBookingOrderByChangedAtAsc(Booking booking);
}