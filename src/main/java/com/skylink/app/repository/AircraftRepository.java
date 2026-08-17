package com.skylink.app.repository;

import com.skylink.app.entity.Aircraft;
import com.skylink.app.enums.AircraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    long countByStatus(AircraftStatus status);
    List<Aircraft> findByStatus(AircraftStatus status);
    boolean existsByRegistrationNumber(String registrationNumber);
    List<Aircraft> findByManufacturer(String manufacturer);
}
