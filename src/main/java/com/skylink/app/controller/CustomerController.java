package com.skylink.app.controller;

import com.skylink.app.dto.CustomerDto;
import com.skylink.app.entity.Customer;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.BookingRepository;
import com.skylink.app.service.ICustomerService;
import com.skylink.app.util.CustomerMapper;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final ICustomerService customerService;
    private final CustomerMapper customerMapper;
    private final BookingRepository bookingRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String query, Model model) {
        var customers = query == null || query.isBlank()
            ? customerService.findAll()
            : customerService.search(query);

        model.addAttribute("customers", customers);
        model.addAttribute("query", query);
        model.addAttribute("totalCount", customerService.findAll().size());
        model.addAttribute("pageTitle", "Customers");
        model.addAttribute("activePage", "customers");
        return "customers/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Customer customer = customerService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        model.addAttribute("customer", customer);
        model.addAttribute("bookings", bookingRepository.findByCustomer(customer));
        model.addAttribute("confirmedCount", bookingRepository.findByCustomer(customer).stream()
            .filter(booking -> booking.getStatus() != null && booking.getStatus().name().equals("CONFIRMED"))
            .count());
        model.addAttribute("cancelledCount", bookingRepository.findByCustomer(customer).stream()
            .filter(booking -> booking.getStatus() != null && booking.getStatus().name().equals("CANCELLED"))
            .count());
        model.addAttribute("pageTitle", customer.getFullName());
        model.addAttribute("activePage", "customers");
        return "customers/detail";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        prepareForm(model, new CustomerDto(), false, "Add Customer");
        return "customers/form";
    }

    @PostMapping("/add")
    public String processAdd(
            @Valid @ModelAttribute("customerDto") CustomerDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateUniqueFields(dto, null, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, dto, false, "Add Customer");
            return "customers/form";
        }

        try {
            customerService.save(customerMapper.fromDto(dto), photoFile);
            addFlash(redirectAttributes, "success",
                "Customer " + dto.getFullName() + " registered successfully.");
            return "redirect:/customers";
        } catch (BusinessRuleException e) {
            bindingResult.reject("customer.rule", e.getMessage());
            prepareForm(model, dto, false, "Add Customer");
            return "customers/form";
        } catch (IOException e) {
            log.error("Photo upload failed while adding customer", e);
            addFlash(redirectAttributes, "error", "Photo upload failed. Please try again.");
            return "redirect:/customers/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        prepareForm(model, customerMapper.toDto(customer), true, "Edit Customer");
        return "customers/form";
    }

    @GetMapping("/api/check-email")
    @ResponseBody
    public java.util.Map<String, Boolean> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId) {
        boolean available = excludeId == null
            ? customerService.isEmailUnique(email)
            : customerService.isEmailUnique(email, excludeId);
        return java.util.Map.of("available", available);
    }

    @PostMapping("/edit/{id}")
    public String processEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("customerDto") CustomerDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateUniqueFields(dto, id, bindingResult);
        if (bindingResult.hasErrors()) {
            prepareForm(model, dto, true, "Edit Customer");
            return "customers/form";
        }

        try {
            customerService.update(id, customerMapper.fromDto(dto), photoFile);
            addFlash(redirectAttributes, "success",
                "Customer " + dto.getFullName() + " updated successfully.");
            return "redirect:/customers/" + id;
        } catch (BusinessRuleException e) {
            bindingResult.reject("customer.rule", e.getMessage());
            prepareForm(model, dto, true, "Edit Customer");
            return "customers/form";
        } catch (IOException e) {
            log.error("Photo upload failed while editing customer {}", id, e);
            addFlash(redirectAttributes, "error", "Photo upload failed. Please try again.");
            return "redirect:/customers/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = customerService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
            String name = customer.getFullName();
            customerService.delete(id);
            addFlash(redirectAttributes, "success", "Customer " + name + " deleted successfully.");
        } catch (BusinessRuleException e) {
            addFlash(redirectAttributes, "warning", e.getMessage());
        } catch (Exception e) {
            log.error("Delete failed for customer id {}", id, e);
            addFlash(redirectAttributes, "error", "Delete failed. Please try again.");
        }
        return "redirect:/customers";
    }

    private void validateUniqueFields(CustomerDto dto, Long excludeId, BindingResult bindingResult) {
        boolean emailUnique = excludeId == null
            ? customerService.isEmailUnique(dto.getEmail())
            : customerService.isEmailUnique(dto.getEmail(), excludeId);
        if (!emailUnique) {
            bindingResult.rejectValue("email", "duplicate", "This email is already registered.");
        }

        boolean nidUnique = excludeId == null
            ? customerService.isNidUnique(dto.getNidOrPassport())
            : customerService.isNidUnique(dto.getNidOrPassport(), excludeId);
        if (!nidUnique) {
            bindingResult.rejectValue("nidOrPassport", "duplicate",
                "This NID/Passport is already registered.");
        }
    }

    private void prepareForm(Model model, CustomerDto dto, boolean isEdit, String pageTitle) {
        model.addAttribute("customerDto", dto);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activePage", "customers");
    }

    private void addFlash(RedirectAttributes attributes, String type, String message) {
        attributes.addFlashAttribute("flashType", type);
        attributes.addFlashAttribute("flashMessage", message);
    }
}
