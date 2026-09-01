package com.skylink.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class OtpEnforcementFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PATHS = Set.of(
        "/login", "/signup", "/verify-otp", "/verify-otp/confirm",
        "/logout", "/error"
    );

    private static final Set<String> ALLOWED_PREFIXES = Set.of(
        "/css/", "/js/", "/images/", "/uploads/", "/error/", "/favicon"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isAllowed(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object verified = session.getAttribute(
                CustomAuthenticationSuccessHandler.SESSION_AUTHENTICATED);
            Object otp = session.getAttribute(
                CustomAuthenticationSuccessHandler.SESSION_OTP);

            if (otp != null && !Boolean.TRUE.equals(verified)) {
                response.sendRedirect(
                    request.getContextPath() + "/verify-otp");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String path) {
        if (ALLOWED_PATHS.contains(path)) return true;
        for (String prefix : ALLOWED_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
