package com.skylink.app.dto;

import com.skylink.app.enums.SeatClass;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookingCreateDto {

    @NotNull(message = "Flight is required")
    private Long flightId;

    @NotNull(message = "Customer is required")
    private Long customerId;

    @NotNull(message = "Seat class is required")
    private SeatClass seatClass;

    @Min(value = 1, message = "At least 1 passenger required")
    @Max(value = 9, message = "Maximum 9 passengers per booking")
    private int passengerCount = 1;

    private String notes;
}
