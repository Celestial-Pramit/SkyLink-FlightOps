package com.skylink.app.dto;

import com.skylink.app.enums.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class UserResponseDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String photoPath;
    private UserStatus status;
    private Set<String> roleNames;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public String getPrimaryRole() {
        if (roleNames == null || roleNames.isEmpty()) return "No Role";
        if (roleNames.contains("ROLE_SUPER_ADMIN")) return "Super Admin";
        if (roleNames.contains("ROLE_ADMIN")) return "Admin";
        if (roleNames.contains("ROLE_STAFF")) return "Staff";
        return roleNames.iterator().next();
    }

    public String getStatusCssClass() {
        if (status == null) return "";
        return switch (status) {
            case ACTIVE -> "badge-active";
            case INACTIVE -> "badge-pending";
            case SUSPENDED -> "badge-cancelled";
        };
    }

    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) +
            parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
