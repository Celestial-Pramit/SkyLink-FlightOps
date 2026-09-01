package com.skylink.app.security;

import com.skylink.app.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final IUserService userService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static final String SESSION_OTP          = "skylink_otp";
    public static final String SESSION_OTP_EXPIRY   = "skylink_otp_expiry";
    public static final String SESSION_OTP_EMAIL    = "skylink_otp_email";
    public static final String SESSION_OTP_REDIRECT = "skylink_otp_redirect";
    public static final String SESSION_AUTHENTICATED = "skylink_otp_verified";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        String email       = authentication.getName();
        String redirectUrl = determineTargetUrl(authentication.getAuthorities());

        int otp = 100000 + RANDOM.nextInt(900000);

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_OTP, String.valueOf(otp));
        session.setAttribute(SESSION_OTP_EXPIRY, LocalDateTime.now().plusMinutes(5));
        session.setAttribute(SESSION_OTP_EMAIL, email);
        session.setAttribute(SESSION_OTP_REDIRECT, redirectUrl);
        session.setAttribute(SESSION_AUTHENTICATED, false);

        log.info("OTP {} generated for {} — pending verification", otp, email);

        response.sendRedirect(request.getContextPath() + "/verify-otp");
    }

    private String determineTargetUrl(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority authority : authorities) {
            switch (authority.getAuthority()) {
                case "ROLE_SUPER_ADMIN" -> { return "/superadmin/dashboard"; }
                case "ROLE_ADMIN", "ROLE_STAFF" -> { return "/dashboard"; }
                default -> { }
            }
        }
        return "/dashboard";
    }
}
