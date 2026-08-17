package com.skylink.app.repository;

import com.skylink.app.entity.Aircraft;
import com.skylink.app.entity.Airport;
import com.skylink.app.entity.Flight;
import com.skylink.app.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    List<Flight> findByAircraftAndDepartureTimeAfter(Aircraft aircraft, LocalDateTime after);

    List<Flight> findByStatus(FlightStatus status);

    List<Flight> findByDepartureTimeAfterAndStatusOrderByDepartureTimeAsc(
        LocalDateTime after, FlightStatus status);

    List<Flight> findByOriginAirportAndDestinationAirport(
        Airport origin, Airport destination);

    List<Flight> findByDepartureTimeAfterOrderByDepartureTimeAsc(LocalDateTime after);

    @Query("""
        SELECT f FROM Flight f
        WHERE (:originId IS NULL OR f.originAirport.id = :originId)
        AND (:destId IS NULL OR f.destinationAirport.id = :destId)
        AND (:status IS NULL OR f.status = :status)
        AND (:from IS NULL OR f.departureTime >= :from)
        AND (:to IS NULL OR f.departureTime <= :to)
        ORDER BY f.departureTime ASC
    """)
    List<Flight> searchFlights(
        @Param("originId") Long originId,
        @Param("destId") Long destId,
        @Param("status") FlightStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT f FROM Flight f
        WHERE f.departureTime >= :from
        AND f.departureTime < :to
        ORDER BY f.departureTime ASC
    """)
    List<Flight> findByDepartureBetween(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT f FROM Flight f
        WHERE (:originId IS NULL OR f.originAirport.id = :originId)
        AND (:destId IS NULL OR f.destinationAirport.id = :destId)
        AND (:status IS NULL OR f.status = :status)
        AND (:fromTime IS NULL OR f.departureTime >= :fromTime)
        AND (:toTime IS NULL OR f.departureTime < :toTime)
        AND (:flightNum IS NULL OR UPPER(f.flightNumber) LIKE
             UPPER(CONCAT('%', :flightNum, '%')))
        ORDER BY f.departureTime ASC
    """)
    List<Flight> search(
        @Param("originId") Long originId,
        @Param("destId") Long destId,
        @Param("status") FlightStatus status,
        @Param("fromTime") LocalDateTime fromTime,
        @Param("toTime") LocalDateTime toTime,
        @Param("flightNum") String flightNum
    );

    @Query("SELECT f FROM Flight f WHERE f.departureTime > :now AND f.status = com.skylink.app.enums.FlightStatus.SCHEDULED ORDER BY f.departureTime ASC")
    List<Flight> findUpcoming(@Param("now") LocalDateTime now);

    boolean existsByFlightNumber(String flightNumber);

    boolean existsByFlightNumberAndIdNot(String flightNumber, Long id);

    List<Flight> findByAircraftOrderByDepartureTimeDesc(Aircraft aircraft);
}
