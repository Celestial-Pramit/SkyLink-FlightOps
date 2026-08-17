package com.skylink.app.service.impl;

import com.skylink.app.entity.Airport;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AirportRepository;
import com.skylink.app.service.IAirportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AirportServiceImpl implements IAirportService {

    private final AirportRepository airportRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Airport> findAll() {
        return airportRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Airport> findById(Long id) {
        return airportRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Airport> findByIataCode(String iataCode) {
        return airportRepository.findByIataCode(iataCode);
    }

    @Override
    public Airport save(Airport airport) {
        return airportRepository.save(airport);
    }

    @Override
    public void delete(Long id) {
        Airport existing = airportRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Airport not found with id: " + id));
        airportRepository.delete(existing);
    }
}
