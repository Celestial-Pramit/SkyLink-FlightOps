package com.skylink.app.controller;

import com.skylink.app.entity.AppUser;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AppUserRepository;
import com.skylink.app.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final AppUserRepository userRepository;
    private final IUserService userService;

    @GetMapping
    public String settings(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        AppUser user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("currentUser", user);
        model.addAttribute("pageTitle", "Settings");
        model.addAttribute("activePage", "settings");
        return "settings/index";
    }

    @PostMapping("/password")
    public String changePassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("flashType", "warning");
            redirectAttributes.addFlashAttribute("flashMessage",
                "Passwords do not match.");
            return "redirect:/settings";
        }
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("flashType", "warning");
            redirectAttributes.addFlashAttribute("flashMessage",
                "Password must be at least 8 characters.");
            return "redirect:/settings";
        }
        AppUser user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userService.resetPasswordAndReturn(user.getId(), newPassword);
        redirectAttributes.addFlashAttribute("flashType", "success");
        redirectAttributes.addFlashAttribute("flashMessage",
            "Password updated successfully.");
        return "redirect:/settings";
    }
}