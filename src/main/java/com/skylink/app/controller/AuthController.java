package com.skylink.app.controller;

import com.skylink.app.dto.UserDto;
import com.skylink.app.entity.AppUser;
import com.skylink.app.enums.UserStatus;
import com.skylink.app.service.IUserService;
import com.skylink.app.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final IUserService userService;
    private final UserMapper userMapper;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",   required = false) String error,
            @RequestParam(value = "logout",  required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {
        if (error   != null) model.addAttribute("loginError",     true);
        if (logout  != null) model.addAttribute("logoutSuccess",  true);
        if (expired != null) model.addAttribute("sessionExpired", true);
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @Valid @ModelAttribute("userDto") UserDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!bindingResult.hasFieldErrors("email")
                && !userService.isEmailUnique(dto.getEmail())) {
            bindingResult.rejectValue("email", "duplicate",
                "This email is already registered.");
        }

        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            bindingResult.rejectValue("password", "length",
                "Password must be at least 8 characters.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            AppUser user = AppUser.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .status(UserStatus.INACTIVE)
                .build();

            userService.createStaff(user, dto.getPassword(), null);

            log.info("New staff account registered: {} — pending admin approval",
                dto.getEmail());

            redirectAttributes.addFlashAttribute("signupSuccess", true);
            redirectAttributes.addFlashAttribute("signupEmail", dto.getEmail());
            return "redirect:/login?registered=true";

        } catch (Exception e) {
            log.error("Signup failed for {}", dto.getEmail(), e);
            model.addAttribute("signupError",
                "Registration failed. Please try again.");
            return "auth/signup";
        }
    }
}
