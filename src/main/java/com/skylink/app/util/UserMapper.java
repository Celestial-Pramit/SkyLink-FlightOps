package com.skylink.app.util;

import com.skylink.app.dto.UserDto;
import com.skylink.app.dto.UserResponseDto;
import com.skylink.app.entity.AppUser;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public AppUser fromDto(UserDto dto) {
        return AppUser.builder()
            .id(dto.getId())
            .fullName(dto.getFullName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .build();
    }

    public UserDto toEditDto(AppUser user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setExistingPhotoPath(user.getProfilePhoto());
        return dto;
    }

    public UserResponseDto toResponseDto(AppUser user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setPhotoPath(user.getProfilePhoto());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        if (user.getRoles() != null) {
            dto.setRoleNames(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        }
        return dto;
    }
}
