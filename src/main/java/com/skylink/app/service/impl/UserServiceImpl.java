package com.skylink.app.service.impl;

import com.skylink.app.entity.AppUser;
import com.skylink.app.entity.Role;
import com.skylink.app.enums.UserStatus;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AppUserRepository;
import com.skylink.app.repository.RoleRepository;
import com.skylink.app.service.IFileStorageService;
import com.skylink.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements IUserService {

    private static final String UPLOAD_SUBFOLDER = "users";

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final IFileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppUser> findByRoleName(String roleName) {
        return userRepository.findAll().stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getName())))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Long excludeId) {
        return !userRepository.existsByEmailAndIdNot(email, excludeId);
    }

    @Override
    public AppUser createAdmin(AppUser user, String rawPassword) throws IOException {
        validateEmailUnique(user.getEmail(), null);
        validatePassword(rawPassword);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(Set.of(resolveRole("ROLE_ADMIN")));
        user.setStatus(UserStatus.ACTIVE);
        AppUser saved = userRepository.save(user);
        log.info("Admin account created: {}", saved.getEmail());
        return saved;
    }

    @Override
    public AppUser createStaff(AppUser user, String rawPassword,
                               MultipartFile photo) throws IOException {
        validateEmailUnique(user.getEmail(), null);
        validatePassword(rawPassword);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(Set.of(resolveRole("ROLE_STAFF")));
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }
        if (photo != null && !photo.isEmpty()) {
            user.setProfilePhoto(fileStorageService.store(photo, UPLOAD_SUBFOLDER));
        }
        AppUser saved = userRepository.save(user);
        log.info("Staff account created: {}", saved.getEmail());
        return saved;
    }

    @Override
    public AppUser updateUser(Long id, AppUser updatedData, String rawPassword,
                              MultipartFile photo) throws IOException {
        AppUser existing = getRequiredUser(id);
        validateEmailUnique(updatedData.getEmail(), id);
        existing.setFullName(updatedData.getFullName());
        existing.setEmail(updatedData.getEmail());
        existing.setPhone(updatedData.getPhone());

        if (rawPassword != null && !rawPassword.isBlank()) {
            validatePassword(rawPassword);
            existing.setPassword(passwordEncoder.encode(rawPassword));
        }

        if (photo != null && !photo.isEmpty()) {
            if (existing.getProfilePhoto() != null) {
                fileStorageService.delete(extractFilename(existing.getProfilePhoto()),
                    UPLOAD_SUBFOLDER);
            }
            existing.setProfilePhoto(fileStorageService.store(photo, UPLOAD_SUBFOLDER));
        }
        return userRepository.save(existing);
    }

    @Override
    public AppUser updateStatus(Long id, UserStatus status) {
        AppUser user = getRequiredUser(id);
        if (status == null) {
            throw new BusinessRuleException("A user status is required.");
        }

        if (user.hasRole("ROLE_SUPER_ADMIN") && status != UserStatus.ACTIVE) {
            long activeSuperAdmins = findByRoleName("ROLE_SUPER_ADMIN").stream()
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .count();
            if (activeSuperAdmins <= 1) {
                throw new BusinessRuleException(
                    "Cannot deactivate the last active Super Admin account.");
            }
        }
        user.setStatus(status);
        return userRepository.save(user);
    }

    @Override
    public void resetPasswordAndReturn(Long id, String newRawPassword) {
        AppUser user = getRequiredUser(id);
        validatePassword(newRawPassword);
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getEmail());
    }

    @Override
    public void updateLastLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    private AppUser getRequiredUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Role resolveRole(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }

    private void validateEmailUnique(String email, Long excludeId) {
        boolean unique = excludeId == null
            ? isEmailUnique(email)
            : isEmailUnique(email, excludeId);
        if (!unique) {
            throw new BusinessRuleException("Email '" + email + "' is already registered.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters.");
        }
    }

    private String extractFilename(String relativePath) {
        int slash = relativePath.lastIndexOf('/');
        return slash >= 0 ? relativePath.substring(slash + 1) : relativePath;
    }
}
