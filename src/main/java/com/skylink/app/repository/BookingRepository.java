package com.skylink.app.repository;

import com.skylink.app.entity.AppUser;
import com.skylink.app.entity.Booking;
import com.skylink.app.entity.Customer;
import com.skylink.app.entity.Flight;
import com.skylink.app.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);
    List<Booking> findByCustomer(Customer customer);
    List<Booking> findByFlight(Flight flight);
    List<Booking> findByCreatedBy(AppUser user);
    List<Booking> findByStatus(BookingStatus status);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.createdAt >= :startOfDay AND b.createdAt < :endOfDay
    """)
    long countBookingsToday(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    List<Booking> findRecent(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b " +
           "WHERE b.status IN " +
           "(com.skylink.app.enums.BookingStatus.CONFIRMED, " +
           "com.skylink.app.enums.BookingStatus.COMPLETED, " +
           "com.skylink.app.enums.BookingStatus.BOARDED)")
    BigDecimal calculateTotalRevenue();

    @Query(value = """
        SELECT DATE(created_at) AS booking_date, COUNT(*) AS booking_count
        FROM bookings
        WHERE created_at >= :sevenDaysAgo
        GROUP BY DATE(created_at)
        ORDER BY booking_date ASC
        """, nativeQuery = true)
    List<Object[]> countBookingsPerDayLastWeek(
        @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo
    );

    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> countByStatus();
}
