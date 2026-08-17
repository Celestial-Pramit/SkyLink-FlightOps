package com.skylink.app.service;

import com.skylink.app.entity.Aircraft;
import com.skylink.app.enums.AircraftStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IAircraftService {
    List<Aircraft> findAll();
    Aircraft findById(Long id);
    List<Aircraft> findByStatus(AircraftStatus status);
    Aircraft save(Aircraft aircraft, MultipartFile image) throws IOException;
    Aircraft update(Long id, Aircraft aircraft, MultipartFile image) throws IOException;
    void delete(Long id);
    boolean existsByRegistrationNumber(String regNumber);
}
