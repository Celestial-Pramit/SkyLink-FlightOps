package com.skylink.app.util;

import com.skylink.app.dto.CustomerDto;
import com.skylink.app.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer fromDto(CustomerDto dto) {
        return Customer.builder()
            .id(dto.getId())
            .fullName(dto.getFullName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .nidOrPassport(dto.getNidOrPassport())
            .dateOfBirth(dto.getDateOfBirth())
            .address(dto.getAddress())
            .build();
    }

    public CustomerDto toDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setNidOrPassport(customer.getNidOrPassport());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setAddress(customer.getAddress());
        dto.setExistingPhotoPath(customer.getPhotoPath());
        return dto;
    }
}
