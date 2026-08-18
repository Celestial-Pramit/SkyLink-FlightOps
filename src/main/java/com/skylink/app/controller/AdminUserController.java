package com.skylink.app.controller;

import com.skylink.app.dto.UserDto;
import com.skylink.app.dto.UserResponseDto;
import com.skylink.app.entity.AppUser;
import com.skylink.app.enums.UserStatus;
import com.skylink.app.exception.BusinessRuleException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final IUserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public String staffList(Model model) {
        List<UserResponseDto> staff = userService.findByRoleName("ROLE_STAFF").stream()
            .map(userMapper::toResponseDto)
            .toList();
        model.addAttribute("staff", staff);
        model.addAttribute("activeCount", staff.stream()
            .filter(user -> user.getStatus() == UserStatus.ACTIVE).count());
        model.addAttribute("pageTitle", "Staff Management");
        model.addAttribute("activePage", "admin-users");
        return "admin/staff-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("pageTitle", "Add Staff Member");
        model.addAttribute("activePage", "admin-users");
        return "admin/create-staff";
    }

    @PostMapping("/create")
    public String processCreate(@Valid @ModelAttribute("userDto") UserDto dto,
                                BindingResult bindingResult,
                                @RequestParam(value = "photoFile", required = false)
                                MultipartFile photoFile,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        validatePassword(dto, bindingResult);
        if (!bindingResult.hasFieldErrors("email") && !userService.isEmailUnique(dto.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "This email is already registered.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Staff Member");
            model.addAttribute("activePage", "admin-users");
            return "admin/create-staff";
        }

        try {
            userService.createStaff(userMapper.fromDto(dto), dto.getPassword(), photoFile);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage",
                "Staff account created for " + dto.getFullName() + ".");
            return "redirect:/admin/users";
        } catch (BusinessRuleException e) {
            bindingResult.rejectValue("email", "rule", e.getMessage());
            model.addAttribute("pageTitle", "Add Staff Member");
            model.addAttribute("activePage", "admin-users");
            return "admin/create-staff";
        } catch (Exception e) {
            log.error("Staff creation failed", e);
            redirectAttributes.addFlashAttribute("flashType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Account creation failed.");
            return "redirect:/admin/users/create";
        }
    }

    @PostMapping("/{id}/status")
    public String toggleStatus(@PathVariable Long id,
                               @RequestParam UserStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            AppUser target = userService.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Staff account not found."));
            if (!target.hasRole("ROLE_STAFF")) {
                throw new BusinessRuleException("Admins may only change staff account status.");
            }
            userService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("flashType", "success");
            redirectAttributes.addFlashAttribute("flashMessage",
                "Status updated to " + status.name().toLowerCase() + ".");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("flashType", "warning");
            redirectAttributes.addFlashAttribute("flashMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void validatePassword(UserDto dto, BindingResult result) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            result.rejectValue("password", "required", "Password is required.");
        } else if (dto.getPassword().length() < 8) {
            result.rejectValue("password", "size", "Password must be at least 8 characters.");
        }
    }
}
