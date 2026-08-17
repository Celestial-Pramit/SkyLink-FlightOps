package com.skylink.app.dto;

import com.skylink.app.enums.AircraftStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class AircraftDto {
    private Long id;

    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^[A-Z0-9]{1,3}-[A-Z0-9]{2,5}$", message = "Format must be like S2-AKA or AP-BIG")
    private String registrationNumber;

    @NotBlank(message = "Model name is required")
    @Size(max = 100, message = "Model name cannot exceed 100 characters")
    private String modelName;

    @NotBlank(message = "Manufacturer is required")
    @Size(max = 100, message = "Manufacturer cannot exceed 100 characters")
    private String manufacturer;

    @NotBlank(message = "Aircraft type code is required")
    @Size(min = 2, max = 10, message = "Type code must be 2-10 characters")
    private String aircraftTypeCode;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 900, message = "Total seats cannot exceed 900")
    private int totalSeats;

    @Min(value = 0, message = "Economy seats cannot be negative")
    private int economySeats;

    @Min(value = 0, message = "Business seats cannot be negative")
    private int businessSeats;

    @Min(value = 0, message = "First class seats cannot be negative")
    private int firstClassSeats;

    private AircraftStatus status;

    private MultipartFile imageFile;
    private String existingImagePath;

    public boolean seatsAddUp() {
        return economySeats + businessSeats + firstClassSeats == totalSeats;
    }
}
