package com.skylink.app.util;

import com.skylink.app.dto.BookingDto;
import com.skylink.app.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setBookingReference(booking.getBookingReference());
        dto.setStatus(booking.getStatus());
        dto.setSeatClass(booking.getSeatClass());
        dto.setPassengerCount(booking.getPassengerCount());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setCancelledAt(booking.getCancelledAt());
        dto.setNotes(booking.getNotes());

        if (booking.getCustomer() != null) {
            dto.setCustomerId(booking.getCustomer().getId());
            dto.setCustomerName(booking.getCustomer().getFullName());
            dto.setCustomerEmail(booking.getCustomer().getEmail());
            dto.setCustomerPhone(booking.getCustomer().getPhone());
        }

        if (booking.getFlight() != null) {
            var flight = booking.getFlight();
            dto.setFlightNumber(flight.getFlightNumber());
            dto.setDepartureTime(flight.getDepartureTime());
            dto.setArrivalTime(flight.getArrivalTime());
            dto.setFormattedDuration(flight.getFormattedDuration());

            if (flight.getOriginAirport() != null) {
                dto.setOriginIata(flight.getOriginAirport().getIataCode());
                dto.setOriginCity(flight.getOriginAirport().getCity());
            }
            if (flight.getDestinationAirport() != null) {
                dto.setDestinationIata(flight.getDestinationAirport().getIataCode());
                dto.setDestinationCity(flight.getDestinationAirport().getCity());
            }
            if (flight.getAircraft() != null) {
                dto.setAircraftModel(flight.getAircraft().getModelName());
            }
        }

        if (booking.getCreatedBy() != null) {
            dto.setCreatedByName(booking.getCreatedBy().getFullName());
        }
        return dto;
    }
}
