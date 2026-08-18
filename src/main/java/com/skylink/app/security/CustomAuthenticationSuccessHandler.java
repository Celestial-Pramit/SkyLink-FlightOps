package com.skylink.app.security;

import com.skylink.app.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final IUserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String redirectUrl = determineTargetUrl(authentication.getAuthorities());
        try {
            userService.updateLastLogin(authentication.getName());
        } catch (Exception e) {
            log.warn("Could not update last login for {}", authentication.getName(), e);
        }
        log.info("User {} logged in, redirecting to {}", authentication.getName(), redirectUrl);
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    private String determineTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            switch (authority.getAuthority()) {
                case "ROLE_SUPER_ADMIN" -> { return "/superadmin/dashboard"; }
                case "ROLE_ADMIN", "ROLE_STAFF" -> { return "/dashboard"; }
                default -> { }
            }
        }
        return "/login?error=role";
    }
}
