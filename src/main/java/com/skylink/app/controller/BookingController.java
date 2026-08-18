package com.skylink.app.controller;

import com.skylink.app.dto.BookingCreateDto;
import com.skylink.app.dto.BookingDto;
import com.skylink.app.entity.AppUser;
import com.skylink.app.entity.Booking;
import com.skylink.app.entity.Flight;
import com.skylink.app.enums.BookingStatus;
import com.skylink.app.enums.SeatClass;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AppUserRepository;
import com.skylink.app.repository.AirportRepository;
import com.skylink.app.repository.CustomerRepository;
import com.skylink.app.repository.FlightRepository;
import com.skylink.app.service.IBookingService;
import com.skylink.app.service.ICustomerService;
import com.skylink.app.service.IFlightService;
import com.skylink.app.util.BookingMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final IBookingService bookingService;
    private final IFlightService flightService;
    private final ICustomerService customerService;
    private final BookingMapper bookingMapper;
    private final AppUserRepository userRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final CustomerRepository customerRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        AppUser currentUser = getCurrentUser(userDetails);
        boolean admin = isAdmin(userDetails);
        List<Booking> allBookings = admin
            ? bookingService.findAll()
            : bookingService.findByCreatedBy(currentUser);

        List<Booking> filtered = allBookings.stream()
            .filter(booking -> status == null || booking.getStatus() == status)
            .filter(booking -> matchesSearch(booking, search))
            .toList();

        model.addAttribute("bookings", filtered.stream().map(bookingMapper::toDto).toList());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("totalCount", allBookings.size());
        model.addAttribute("confirmedCount", countByStatus(allBookings, BookingStatus.CONFIRMED));
        model.addAttribute("pendingCount", countByStatus(allBookings, BookingStatus.PENDING));
        model.addAttribute("cancelledCount", countByStatus(allBookings, BookingStatus.CANCELLED));
        model.addAttribute("isAdmin", admin);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("pageTitle", "Bookings");
        model.addAttribute("activePage", "bookings");
        return "bookings/list";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) Long originAirportId,
            @RequestParam(required = false) Long destinationAirportId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) Long customerId,
            Model model) {

        List<Flight> flights = null;
        if (originAirportId != null || destinationAirportId != null || date != null) {
            flights = flightService.search(originAirportId, destinationAirportId, date, null);
        }

        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("flights", flights);
        model.addAttribute("originAirportId", originAirportId);
        model.addAttribute("destinationAirportId", destinationAirportId);
        model.addAttribute("selectedDate", date);
        model.addAttribute("preselectedFlightId", flightId);
        model.addAttribute("preselectedCustomerId", customerId);
        model.addAttribute("pageTitle", "Find & Book");
        model.addAttribute("activePage", "findbook");
        return "bookings/search";
    }

    @GetMapping("/create")
    public String showCreateForm(
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) Long customerId,
            Model model) {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setFlightId(flightId);
        dto.setCustomerId(customerId);
        populateCreateModel(model, dto);
        return "bookings/create";
    }

    @PostMapping("/create")
    public String processCreate(
            @Valid @ModelAttribute("bookingCreateDto") BookingCreateDto dto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateCreateModel(model, dto);
            return "bookings/create";
        }

        try {
            Booking booking = bookingService.createBooking(dto, getCurrentUser(userDetails));
            addFlash(redirectAttributes, "success",
                "Booking " + booking.getBookingReference() + " confirmed successfully.");
            return "redirect:/bookings/" + booking.getId();
        } catch (BusinessRuleException e) {
            addFlash(redirectAttributes, "warning", e.getMessage());
            return "redirect:/bookings/create?flightId=" + dto.getFlightId()
                + "&customerId=" + dto.getCustomerId();
        } catch (Exception e) {
            log.error("Booking creation failed", e);
            addFlash(redirectAttributes, "error", "Booking failed. Please try again.");
            return "redirect:/bookings/create";
        }
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        Booking booking = bookingService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        boolean admin = isAdmin(userDetails);

        if (!admin && (booking.getCreatedBy() == null
                || !booking.getCreatedBy().getId().equals(getCurrentUser(userDetails).getId()))) {
            return "redirect:/bookings?error=access";
        }

        BookingDto dto = bookingMapper.toDto(booking);
        model.addAttribute("booking", dto);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("pageTitle", dto.getBookingReference());
        model.addAttribute("activePage", "bookings");
        return "bookings/detail";
    }

    @PostMapping("/cancel/{id}")
    public String cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, getCurrentUser(userDetails));
            addFlash(redirectAttributes, "success", "Booking cancelled successfully. Seats restored.");
        } catch (BusinessRuleException e) {
            addFlash(redirectAttributes, "warning", e.getMessage());
        } catch (Exception e) {
            log.error("Cancel failed for booking id {}", id, e);
            addFlash(redirectAttributes, "error", "Cancel failed. Please try again.");
        }
        return "redirect:/bookings/" + id;
    }

    @PostMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus newStatus,
            RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateStatus(id, newStatus);
            addFlash(redirectAttributes, "success", "Status updated to " + newStatus + ".");
        } catch (BusinessRuleException e) {
            addFlash(redirectAttributes, "warning", e.getMessage());
        } catch (Exception e) {
            log.error("Status update failed for booking id {}", id, e);
            addFlash(redirectAttributes, "error", "Status update failed.");
        }
        return "redirect:/bookings/" + id;
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Booking booking = bookingService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
            String reference = booking.getBookingReference();
            bookingService.deleteBooking(id);
            addFlash(redirectAttributes, "success", "Booking " + reference + " deleted.");
        } catch (BusinessRuleException e) {
            addFlash(redirectAttributes, "warning", e.getMessage());
        } catch (Exception e) {
            log.error("Delete failed for booking id {}", id, e);
            addFlash(redirectAttributes, "error", "Delete failed. Please try again.");
        }
        return "redirect:/bookings";
    }

    private AppUser getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                || authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private boolean matchesSearch(Booking booking, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String query = search.trim().toLowerCase();
        return contains(booking.getBookingReference(), query)
            || contains(booking.getCustomer() == null ? null : booking.getCustomer().getFullName(), query)
            || contains(booking.getFlight() == null ? null : booking.getFlight().getFlightNumber(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private long countByStatus(List<Booking> bookings, BookingStatus status) {
        return bookings.stream().filter(booking -> booking.getStatus() == status).count();
    }

    private void populateCreateModel(Model model, BookingCreateDto dto) {
        model.addAttribute("bookingCreateDto", dto);
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("seatClasses", SeatClass.values());
        if (dto.getFlightId() != null) {
            flightRepository.findById(dto.getFlightId())
                .ifPresent(flight -> model.addAttribute("selectedFlight", flight));
        }
        if (dto.getCustomerId() != null) {
            customerRepository.findById(dto.getCustomerId())
                .ifPresent(customer -> model.addAttribute("selectedCustomer", customer));
        }
        model.addAttribute("pageTitle", "Create Booking");
        model.addAttribute("activePage", "findbook");
    }

    private void addFlash(RedirectAttributes attributes, String type, String message) {
        attributes.addFlashAttribute("flashType", type);
        attributes.addFlashAttribute("flashMessage", message);
    }
}
