package com.skylink.app.entity;

import com.skylink.app.enums.FlightStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Flight number is required")
    @Column(nullable = false, unique = true, length = 20)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origin_airport_id", nullable = false)
    private Airport originAirport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_airport_id", nullable = false)
    private Airport destinationAirport;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FlightStatus status = FlightStatus.SCHEDULED;

    @DecimalMin(value = "0.0", inclusive = false, message = "Economy price must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal economyPrice;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal businessPrice;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal firstClassPrice;

    @Column(nullable = false)
    private int availableEconomySeats;

    @Column(nullable = false)
    private int availableBusinessSeats;

    @Column(nullable = false)
    private int availableFirstClassSeats;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public long getDurationMinutes() {
        if (departureTime == null || arrivalTime == null) return 0;
        return Duration.between(departureTime, arrivalTime).toMinutes();
    }

    public String getFormattedDuration() {
        long mins = getDurationMinutes();
        return (mins / 60) + "h " + (mins % 60) + "m";
    }
}
