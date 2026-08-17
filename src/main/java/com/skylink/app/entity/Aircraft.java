package com.skylink.app.entity;

import com.skylink.app.enums.AircraftStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "aircraft")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Registration number is required")
    @Column(nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @NotBlank(message = "Model name is required")
    @Column(nullable = false, length = 100)
    private String modelName;

    @NotBlank(message = "Manufacturer is required")
    @Column(nullable = false, length = 100)
    private String manufacturer;

    @NotBlank(message = "Type code is required")
    @Column(nullable = false, length = 10)
    private String aircraftTypeCode;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 900, message = "Total seats cannot exceed 900")
    @Column(nullable = false)
    private int totalSeats;

    @Min(value = 0)
    @Column(nullable = false)
    private int economySeats;

    @Min(value = 0)
    @Column(nullable = false)
    private int businessSeats;

    @Min(value = 0)
    @Column(nullable = false)
    private int firstClassSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AircraftStatus status = AircraftStatus.ACTIVE;

    @Column(name = "image_path")
    private String imagePath;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
