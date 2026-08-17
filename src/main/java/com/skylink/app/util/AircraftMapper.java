package com.skylink.app.util;

import com.skylink.app.dto.AircraftDto;
import com.skylink.app.entity.Aircraft;
import org.springframework.stereotype.Component;

@Component
public class AircraftMapper {
    public Aircraft fromDto(AircraftDto dto) {
        Aircraft.AircraftBuilder builder = Aircraft.builder()
            .id(dto.getId())
            .registrationNumber(dto.getRegistrationNumber())
            .modelName(dto.getModelName())
            .manufacturer(dto.getManufacturer())
            .aircraftTypeCode(dto.getAircraftTypeCode())
            .totalSeats(dto.getTotalSeats())
            .economySeats(dto.getEconomySeats())
            .businessSeats(dto.getBusinessSeats())
            .firstClassSeats(dto.getFirstClassSeats());
        if (dto.getStatus() != null) {
            builder.status(dto.getStatus());
        }
        return builder.build();
    }

    public AircraftDto toDto(Aircraft aircraft) {
        AircraftDto dto = new AircraftDto();
        dto.setId(aircraft.getId());
        dto.setRegistrationNumber(aircraft.getRegistrationNumber());
        dto.setModelName(aircraft.getModelName());
        dto.setManufacturer(aircraft.getManufacturer());
        dto.setAircraftTypeCode(aircraft.getAircraftTypeCode());
        dto.setTotalSeats(aircraft.getTotalSeats());
        dto.setEconomySeats(aircraft.getEconomySeats());
        dto.setBusinessSeats(aircraft.getBusinessSeats());
        dto.setFirstClassSeats(aircraft.getFirstClassSeats());
        dto.setStatus(aircraft.getStatus());
        dto.setExistingImagePath(aircraft.getImagePath());
        return dto;
    }
}
