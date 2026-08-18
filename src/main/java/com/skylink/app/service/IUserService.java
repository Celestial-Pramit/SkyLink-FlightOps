package com.skylink.app.service;

import com.skylink.app.entity.AppUser;
import com.skylink.app.enums.UserStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<AppUser> findAll();
    List<AppUser> findByRoleName(String roleName);
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByEmail(String email);
    AppUser createAdmin(AppUser user, String rawPassword) throws IOException;
    AppUser createStaff(AppUser user, String rawPassword,
                        MultipartFile photo) throws IOException;
    AppUser updateUser(Long id, AppUser updatedData, String rawPassword,
                       MultipartFile photo) throws IOException;
    AppUser updateStatus(Long id, UserStatus status);
    void resetPasswordAndReturn(Long id, String newRawPassword);
    boolean isEmailUnique(String email);
    boolean isEmailUnique(String email, Long excludeId);
    void updateLastLogin(String email);
}
