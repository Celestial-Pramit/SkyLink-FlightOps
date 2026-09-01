package com.skylink.app.web;

import com.skylink.app.entity.AppUser;
import com.skylink.app.enums.UserStatus;
import com.skylink.app.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final AppUserRepository userRepository;

    @ModelAttribute("pendingStaffCount")
    public long pendingStaffCount() {
        try {
            Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal())) {
                return 0L;
            }
            boolean isAdminOrSuper = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            if (!isAdminOrSuper) return 0L;

            List<AppUser> staffList = userRepository.findByStatus(UserStatus.INACTIVE);
            return staffList.stream()
                .filter(u -> u.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_STAFF")))
                .count();

        } catch (Exception e) {
            return 0L;
        }
    }
}
