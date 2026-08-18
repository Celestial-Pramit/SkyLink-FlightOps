package com.skylink.app.dto;

import com.skylink.app.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Enter a valid email address")
    private String email;

    private String password;

    @Size(max = 20)
    private String phone;

    private UserStatus status;
    private MultipartFile photoFile;
    private String existingPhotoPath;

    public boolean isPasswordRequired() {
        return id == null;
    }

    public boolean hasNewPassword() {
        return password != null && !password.isBlank();
    }
}
