package com.skylink.app.security;

import com.skylink.app.entity.AppUser;
import com.skylink.app.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Login attempt with unknown email: {}", email);
                return new UsernameNotFoundException("No account found for: " + email);
            });

        Set<GrantedAuthority> authorities = appUser.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet());

        return User.builder()
            .username(appUser.getEmail())
            .password(appUser.getPassword())
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(appUser.getStatus().name().equals("SUSPENDED"))
            .credentialsExpired(false)
            .disabled(appUser.getStatus().name().equals("INACTIVE"))
            .build();
    }
}
