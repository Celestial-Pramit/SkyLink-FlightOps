package com.skylink.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "airports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "IATA code is required")
    @Size(min = 3, max = 3, message = "IATA code must be exactly 3 characters")
    @Column(nullable = false, unique = true, length = 3)
    private String iataCode;

    @NotBlank(message = "Airport name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "City is required")
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "Country is required")
    @Column(nullable = false, length = 100)
    private String country;

    public String getDisplayName() {
        return iataCode + " - " + city + " (" + name + ")";
    }
}
