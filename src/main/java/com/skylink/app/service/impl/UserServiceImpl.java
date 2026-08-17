package com.skylink.app.service.impl;

import com.skylink.app.entity.AppUser;
import com.skylink.app.enums.UserStatus;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AppUserRepository;
import com.skylink.app.repository.RoleRepository;
import com.skylink.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements IUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    @Override
    public AppUser createAdmin(AppUser user, String roleName) {
        // TODO Phase 1B / Phase 8: encode password, attach role, persist
        return null;
    }

    @Override
    public AppUser createStaff(AppUser user) {
        // TODO Phase 1B: encode password, attach ROLE_STAFF, persist
        return null;
    }

    @Override
    public AppUser updateStatus(Long id, UserStatus status) {
        // TODO Phase 8: load, set status, save
        AppUser existing = appUserRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        existing.setStatus(status);
        return appUserRepository.save(existing);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        // TODO Phase 8: load, encode new password, save
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email) {
        return !appUserRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Long excludeId) {
        // TODO Phase 8: existsByEmailAndIdNot (custom repo method)
        return !appUserRepository.existsByEmail(email);
    }
}
