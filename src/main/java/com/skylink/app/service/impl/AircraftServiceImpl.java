package com.skylink.app.service.impl;

import com.skylink.app.entity.Aircraft;
import com.skylink.app.enums.AircraftStatus;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AircraftRepository;
import com.skylink.app.repository.FlightRepository;
import com.skylink.app.service.IAircraftService;
import com.skylink.app.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AircraftServiceImpl implements IAircraftService {

    private static final String UPLOAD_SUBFOLDER = "aircraft";

    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final IFileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<Aircraft> findAll() {
        return aircraftRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Aircraft findById(Long id) {
        return aircraftRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Aircraft not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aircraft> findByStatus(AircraftStatus status) {
        return aircraftRepository.findByStatus(status);
    }

    @Override
    public Aircraft save(Aircraft aircraft, MultipartFile image) throws IOException {
        if (aircraftRepository.existsByRegistrationNumber(aircraft.getRegistrationNumber())) {
            throw new BusinessRuleException("Registration number '" + aircraft.getRegistrationNumber() + "' is already in use.");
        }
        validateSeatAllocation(aircraft);

        if (image != null && !image.isEmpty()) {
            aircraft.setImagePath(fileStorageService.store(image, UPLOAD_SUBFOLDER));
        }

        Aircraft saved = aircraftRepository.save(aircraft);
        log.info("Created aircraft: {} ({})", saved.getRegistrationNumber(), saved.getModelName());
        return saved;
    }

    @Override
    public Aircraft update(Long id, Aircraft updatedData, MultipartFile image) throws IOException {
        Aircraft existing = findById(id);
        validateSeatAllocation(updatedData);

        if (!existing.getRegistrationNumber().equals(updatedData.getRegistrationNumber())
                && aircraftRepository.existsByRegistrationNumber(updatedData.getRegistrationNumber())) {
            throw new BusinessRuleException("Registration number '" + updatedData.getRegistrationNumber() + "' is already in use by another aircraft.");
        }

        existing.setRegistrationNumber(updatedData.getRegistrationNumber());
        existing.setModelName(updatedData.getModelName());
        existing.setManufacturer(updatedData.getManufacturer());
        existing.setAircraftTypeCode(updatedData.getAircraftTypeCode());
        existing.setTotalSeats(updatedData.getTotalSeats());
        existing.setEconomySeats(updatedData.getEconomySeats());
        existing.setBusinessSeats(updatedData.getBusinessSeats());
        existing.setFirstClassSeats(updatedData.getFirstClassSeats());
        existing.setStatus(updatedData.getStatus());

        if (image != null && !image.isEmpty()) {
            String oldImagePath = existing.getImagePath();
            String newImagePath = fileStorageService.store(image, UPLOAD_SUBFOLDER);
            existing.setImagePath(newImagePath);
            deleteStoredImage(oldImagePath);
        }

        Aircraft saved = aircraftRepository.save(existing);
        log.info("Updated aircraft: {}", saved.getRegistrationNumber());
        return saved;
    }

    @Override
    public void delete(Long id) {
        Aircraft aircraft = findById(id);
        boolean hasFutureFlights = !flightRepository
            .findByAircraftAndDepartureTimeAfter(aircraft, LocalDateTime.now())
            .isEmpty();

        if (hasFutureFlights) {
            throw new BusinessRuleException("Cannot delete aircraft '" + aircraft.getRegistrationNumber()
                + "' - it is assigned to one or more future flights. Reassign or cancel those flights first.");
        }

        deleteStoredImage(aircraft.getImagePath());
        aircraftRepository.delete(aircraft);
        log.info("Deleted aircraft: {}", aircraft.getRegistrationNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRegistrationNumber(String regNumber) {
        return aircraftRepository.existsByRegistrationNumber(regNumber);
    }

    private void validateSeatAllocation(Aircraft aircraft) {
        if (aircraft.getEconomySeats() + aircraft.getBusinessSeats() + aircraft.getFirstClassSeats()
                != aircraft.getTotalSeats()) {
            throw new BusinessRuleException("Economy + Business + First Class must equal Total Seats");
        }
    }

    private void deleteStoredImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }
        int slash = imagePath.lastIndexOf('/');
        String filename = slash >= 0 ? imagePath.substring(slash + 1) : imagePath;
        fileStorageService.delete(filename, UPLOAD_SUBFOLDER);
    }
}
