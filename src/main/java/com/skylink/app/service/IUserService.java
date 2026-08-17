package com.skylink.app.service;

import com.skylink.app.entity.AppUser;
import com.skylink.app.enums.UserStatus;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<AppUser> findAll();
    Optional<AppUser> findById(Long id);
    Optional<AppUser> findByEmail(String email);
    AppUser createAdmin(AppUser user, String roleName);
    AppUser createStaff(AppUser user);
    AppUser updateStatus(Long id, UserStatus status);
    void resetPassword(Long id, String newPassword);
    boolean isEmailUnique(String email);
    boolean isEmailUnique(String email, Long excludeId);
}
