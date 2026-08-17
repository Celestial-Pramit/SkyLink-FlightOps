package com.skylink.app.controller;

import com.skylink.app.dto.AircraftDto;
import com.skylink.app.entity.Aircraft;
import com.skylink.app.enums.AircraftStatus;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.service.IAircraftService;
import com.skylink.app.util.AircraftMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/aircraft")
@RequiredArgsConstructor
@Slf4j
public class AircraftController {

    private final IAircraftService aircraftService;
    private final AircraftMapper aircraftMapper;

    @GetMapping
    public String list(@RequestParam(required = false) AircraftStatus status, Model model) {
        List<Aircraft> aircraft = status == null
            ? aircraftService.findAll()
            : aircraftService.findByStatus(status);

        model.addAttribute("aircraft", aircraft);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", AircraftStatus.values());
        model.addAttribute("pageTitle", "Aircraft");
        model.addAttribute("activePage", "aircraft");
        model.addAttribute("totalCount", aircraftService.findAll().size());
        model.addAttribute("activeCount", aircraftService.findByStatus(AircraftStatus.ACTIVE).size());
        model.addAttribute("maintenanceCount", aircraftService.findByStatus(AircraftStatus.MAINTENANCE).size());
        model.addAttribute("retiredCount", aircraftService.findByStatus(AircraftStatus.RETIRED).size());
        return "aircraft/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        AircraftDto dto = new AircraftDto();
        prepareFormModel(model, dto, false, "Add Aircraft");
        return "aircraft/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String processAdd(
            @Valid @ModelAttribute("aircraftDto") AircraftDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!dto.seatsAddUp()) {
            bindingResult.rejectValue("totalSeats", "seats.mismatch",
                "Economy + Business + First Class must equal Total Seats");
        }
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, dto, false, "Add Aircraft");
            return "aircraft/form";
        }

        try {
            aircraftService.save(aircraftMapper.fromDto(dto), imageFile);
            addFlash(redirectAttributes, "success", "Aircraft " + dto.getRegistrationNumber() + " added successfully.");
            return "redirect:/aircraft";
        } catch (BusinessRuleException e) {
            bindingResult.rejectValue("registrationNumber", "duplicate", e.getMessage());
            prepareFormModel(model, dto, false, "Add Aircraft");
            return "aircraft/form";
        } catch (IOException e) {
            log.error("Image upload failed while adding aircraft", e);
            addFlash(redirectAttributes, "error", "Image upload failed. Please try again.");
            return "redirect:/aircraft/add";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        prepareFormModel(model, aircraftMapper.toDto(aircraftService.findById(id)), true, "Edit Aircraft");
        return "aircraft/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String processEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("aircraftDto") AircraftDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!dto.seatsAddUp()) {
            bindingResult.rejectValue("totalSeats", "seats.mismatch",
                "Economy + Business + First Class must equal Total Seats");
        }
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, dto, true, "Edit Aircraft");
            return "aircraft/form";
        }

        try {
            aircraftService.update(id, aircraftMapper.fromDto(dto), imageFile);
            addFlash(redirectAttributes, "success", "Aircraft " + dto.getRegistrationNumber() + " updated successfully.");
            return "redirect:/aircraft";
        } catch (BusinessRuleException e) {
            bindingResult.rejectValue("registrationNumber", "duplicate", e.getMessage());
            prepareFormModel(model, dto, true, "Edit Aircraft");
            return "aircraft/form";
        } catch (IOException e) {
            log.error("Image upload failed while editing aircraft {}", id, e);
            addFlash(redirectAttributes, "error", "Image upload failed. Please try again.");
            return "redirect:/aircraft/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Aircraft aircraft = aircraftService.findById(id);
            String registrationNumber = aircraft.getRegistrationNumber();
            aircraftService.delete(id);
            addFlash(redirectAttributes, "success", "Aircraft " + registrationNumber + " deleted.");
        } catch (BusinessRuleException e) {
            addFlash(redirectAttributes, "warning", e.getMessage());
        } catch (Exception e) {
            log.error("Delete failed for aircraft id {}", id, e);
            addFlash(redirectAttributes, "error", "Delete failed. Please try again.");
        }
        return "redirect:/aircraft";
    }

    private void prepareFormModel(Model model, AircraftDto dto, boolean isEdit, String pageTitle) {
        model.addAttribute("aircraftDto", dto);
        model.addAttribute("statuses", AircraftStatus.values());
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activePage", "aircraft");
    }

    private void addFlash(RedirectAttributes attributes, String type, String message) {
        attributes.addFlashAttribute("flashType", type);
        attributes.addFlashAttribute("flashMessage", message);
    }
}
