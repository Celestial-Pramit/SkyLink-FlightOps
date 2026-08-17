package com.skylink.app.service;

import com.skylink.app.entity.Airport;

import java.util.List;
import java.util.Optional;

public interface IAirportService {
    List<Airport> findAll();
    Optional<Airport> findById(Long id);
    Optional<Airport> findByIataCode(String iataCode);
    Airport save(Airport airport);
    void delete(Long id);
}
