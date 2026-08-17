package com.skylink.app.service;

import com.skylink.app.entity.Flight;
import com.skylink.app.enums.FlightStatus;
import com.skylink.app.enums.SeatClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IFlightService {
    List<Flight> findAll();
    Flight findById(Long id);
    List<Flight> search(Long originId, Long destId,
                        LocalDateTime from, LocalDateTime to,
                        FlightStatus status);
    List<Flight> search(Long originId, Long destId,
                        LocalDate date, FlightStatus status);
    List<Flight> searchFull(Long originId, Long destId,
                            FlightStatus status, LocalDate date,
                            String flightNumber);
    List<Flight> findInMonth(int year, int month);
    List<Flight> findUpcoming(int limit);
    Flight save(Flight flight);
    Flight update(Long id, Flight flight);
    void delete(Long id);
    int getAvailableSeats(Long flightId, SeatClass seatClass);
    void decrementSeats(Long flightId, SeatClass seatClass, int count);
    void incrementSeats(Long flightId, SeatClass seatClass, int count);
}
