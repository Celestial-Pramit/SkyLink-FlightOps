package com.skylink.app.controller;

import com.skylink.app.security.CustomAuthenticationSuccessHandler;
import com.skylink.app.service.IUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OtpController {

    private final IUserService userService;

    // Remember-me re-authentication bypasses the success handler and
    // therefore bypasses OTP. Known demo limitation — in production, a
    // RememberMeAuthenticationSuccessHandler would enforce OTP as well.

    @GetMapping("/verify-otp")
    public String otpPage(HttpSession session,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        String otp = (String) session.getAttribute(
            CustomAuthenticationSuccessHandler.SESSION_OTP);
        if (otp == null) {
            return "redirect:/login";
        }

        LocalDateTime expiry = (LocalDateTime) session.getAttribute(
            CustomAuthenticationSuccessHandler.SESSION_OTP_EXPIRY);
        if (expiry == null || LocalDateTime.now().isAfter(expiry)) {
            session.invalidate();
            return "redirect:/login?expired=true";
        }

        String email = (String) session.getAttribute(
            CustomAuthenticationSuccessHandler.SESSION_OTP_EMAIL);
        long secondsLeft = java.time.Duration.between(
            LocalDateTime.now(), expiry).getSeconds();

        model.addAttribute("demoOtp", otp);
        model.addAttribute("userEmail", email != null ? maskEmail(email) : "your account");
        model.addAttribute("secondsLeft", Math.max(0, secondsLeft));
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp/confirm")
    public String confirmOtp(
            @RequestParam(defaultValue = "") String digit1,
            @RequestParam(defaultValue = "") String digit2,
            @RequestParam(defaultValue = "") String digit3,
            @RequestParam(defaultValue = "") String digit4,
            @RequestParam(defaultValue = "") String digit5,
            @RequestParam(defaultValue = "") String digit6,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String stored = (String) session.getAttribute(
            CustomAuthenticationSuccessHandler.SESSION_OTP);
        LocalDateTime expiry = (LocalDateTime) session.getAttribute(
            CustomAuthenticationSuccessHandler.SESSION_OTP_EXPIRY);
        String email = (String) session.getAttribute(
            CustomAuthenticationSuccessHandler.SESSION_OTP_EMAIL);
        String redirect = (String) session.getAttribute(
            CustomAuthenticationSuccessHandler.SESSION_OTP_REDIRECT);

        if (stored == null || expiry == null) {
            return "redirect:/login?expired=true";
        }
        if (LocalDateTime.now().isAfter(expiry)) {
            session.invalidate();
            return "redirect:/login?expired=true";
        }

        String entered = digit1 + digit2 + digit3 + digit4 + digit5 + digit6;

        if (!entered.trim().equals(stored)) {
            redirectAttributes.addFlashAttribute("otpError",
                "Incorrect code. Please try again.");
            return "redirect:/verify-otp";
        }

        session.setAttribute(
            CustomAuthenticationSuccessHandler.SESSION_AUTHENTICATED, true);
        session.removeAttribute(CustomAuthenticationSuccessHandler.SESSION_OTP);
        session.removeAttribute(CustomAuthenticationSuccessHandler.SESSION_OTP_EMAIL);
        session.removeAttribute(CustomAuthenticationSuccessHandler.SESSION_OTP_EXPIRY);
        session.removeAttribute(CustomAuthenticationSuccessHandler.SESSION_OTP_REDIRECT);

        try {
            if (email != null) userService.updateLastLogin(email);
        } catch (Exception e) {
            log.warn("Could not update last login for {}", email, e);
        }

        log.info("OTP verified for {} — redirecting to {}", email, redirect);
        return "redirect:" + (redirect != null ? redirect : "/dashboard");
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at);
    }
}
