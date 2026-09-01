package com.skylink.app.controller;

import com.skylink.app.dto.UserDto;
import com.skylink.app.dto.UserResponseDto;
import com.skylink.app.entity.AppUser;
import com.skylink.app.enums.UserStatus;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.service.IActivityLogService;
import com.skylink.app.service.IUserService;
import com.skylink.app.util.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/superadmin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class SuperAdminController {

    private final IUserService userService;
    private final UserMapper userMapper;
    private final IActivityLogService activityLogService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<AppUser> admins = userService.findByRoleName("ROLE_ADMIN");
        List<AppUser> staff  = userService.findByRoleName("ROLE_STAFF");

        model.addAttribute("totalAdmins",  admins.size());
        model.addAttribute("activeAdmins", countActive(admins));
        model.addAttribute("totalStaff",   staff.size());
        model.addAttribute("activeStaff",  countActive(staff));

        model.addAttribute("recentAdmins", admins.stream()
            .sorted(Comparator.comparing(AppUser::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .map(userMapper::toResponseDto)
            .toList());

        model.addAttribute("activityLogs",
            activityLogService.getRecent(50));

        model.addAttribute("pageTitle", "Super Admin");
        model.addAttribute("activePage", "superadmin");
        return "superadmin/dashboard";
    }

    @GetMapping("/users")
    public String userList(
            @RequestParam(defaultValue = "all") String role,
            Model model) {

        List<UserResponseDto> users;
        if ("admin".equals(role)) {
            users = toResponseList(userService.findByRoleName("ROLE_ADMIN"));
        } else if ("staff".equals(role)) {
            users = toResponseList(userService.findByRoleName("ROLE_STAFF"));
        } else {
            users = userService.findAll().stream()
                .filter(u -> u.getRoles().stream()
                    .anyMatch(r -> !"ROLE_SUPER_ADMIN".equals(r.getName())))
                .map(userMapper::toResponseDto)
                .toList();
        }

        model.addAttribute("users",        users);
        model.addAttribute("selectedRole", role);
        model.addAttribute("adminCount",
            userService.findByRoleName("ROLE_ADMIN").size());
        model.addAttribute("staffCount",
            userService.findByRoleName("ROLE_STAFF").size());
        model.addAttribute("pageTitle",  "User Management");
        model.addAttribute("activePage", "superadmin");
        return "superadmin/users";
    }

    @GetMapping("/users/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("pageTitle",  "Create Admin Account");
        model.addAttribute("activePage", "superadmin");
        return "superadmin/create-user";
    }

    @PostMapping("/users/create")
    public String processCreate(
            @Valid @ModelAttribute("userDto") UserDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validatePassword(dto, bindingResult,
            "Password is required when creating an account.");
        validateEmail(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            addFormPageAttributes(model, "Create Admin Account");
            return "superadmin/create-user";
        }

        try {
            AppUser saved = userService.createAdmin(
                userMapper.fromDto(dto), dto.getPassword());

            activityLogService.log(
                "superadmin", "Super Admin",
                "CREATE", "USER", saved.getEmail(),
                "Created admin account for " + saved.getFullName()
                + " (" + saved.getEmail() + ")");

            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage",
                "Admin account created for " + saved.getFullName() + ".");
            return "redirect:/superadmin/users";

        } catch (BusinessRuleException e) {
            bindingResult.rejectValue("email", "rule", e.getMessage());
            addFormPageAttributes(model, "Create Admin Account");
            return "superadmin/create-user";
        } catch (Exception e) {
            log.error("Admin creation failed", e);
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage",
                "Account creation failed.");
            return "redirect:/superadmin/users/create";
        }
    }

    @PostMapping("/users/{id}/status")
    public String toggleStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            AppUser updated = userService.updateStatus(id, status);

            activityLogService.log(
                "superadmin", "Super Admin",
                "STATUS_CHANGE", "USER", updated.getEmail(),
                "Set " + updated.getFullName()
                + " status to " + status.name());

            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage",
                updated.getFullName() + " is now "
                + status.name().toLowerCase() + ".");
        } catch (BusinessRuleException e) {
            addWarning(redirectAttributes, e.getMessage());
        }
        return "redirect:/superadmin/users";
    }

    @PostMapping("/users/{id}/reset")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            userService.resetPasswordAndReturn(id, newPassword);

            activityLogService.log(
                "superadmin", "Super Admin",
                "UPDATE", "USER", String.valueOf(id),
                "Password reset for user ID " + id);

            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage",
                "Password reset successfully.");
        } catch (BusinessRuleException e) {
            addWarning(redirectAttributes, e.getMessage());
        }
        return "redirect:/superadmin/users";
    }

    private long countActive(List<AppUser> users) {
        return users.stream()
            .filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
    }

    private List<UserResponseDto> toResponseList(List<AppUser> users) {
        return users.stream().map(userMapper::toResponseDto).toList();
    }

    private void validatePassword(UserDto dto, BindingResult result,
                                   String requiredMessage) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            result.rejectValue("password", "required", requiredMessage);
        } else if (dto.getPassword().length() < 8) {
            result.rejectValue("password", "size",
                "Password must be at least 8 characters.");
        }
    }

    private void validateEmail(UserDto dto, BindingResult result) {
        if (!result.hasFieldErrors("email")
                && !userService.isEmailUnique(dto.getEmail())) {
            result.rejectValue("email", "duplicate",
                "This email is already registered.");
        }
    }

    private void addFormPageAttributes(Model model, String title) {
        model.addAttribute("pageTitle",  title);
        model.addAttribute("activePage", "superadmin");
    }

    private void addWarning(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute("flashType", "warning");
        attributes.addFlashAttribute("flashMessage", message);
    }
}
