package com.skylink.app.controller;

import com.skylink.app.dto.BookingDto;
import com.skylink.app.entity.AppUser;
import com.skylink.app.entity.Booking;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.AppUserRepository;
import com.skylink.app.service.IBookingService;
import com.skylink.app.util.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IBookingService bookingService;
    private final BookingMapper bookingMapper;
    private final AppUserRepository userRepository;

    @GetMapping("/{reference}")
    public String checkout(@PathVariable String reference,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Booking booking = findAuthorizedBooking(reference, userDetails);
        model.addAttribute("booking", bookingMapper.toDto(booking));
        model.addAttribute("pageTitle", "Payment");
        return "payment/checkout";
    }

    @PostMapping("/{reference}/process")
    public String process(@PathVariable String reference,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        Booking booking = findAuthorizedBooking(reference, userDetails);
        log.info("Mock payment completed for booking {}", booking.getBookingReference());
        redirectAttributes.addFlashAttribute("flashType", "success");
        redirectAttributes.addFlashAttribute("flashMessage",
            "Demo payment completed for " + booking.getBookingReference() + ".");
        return "redirect:/bookings/" + booking.getId();
    }

    private Booking findAuthorizedBooking(String reference, UserDetails userDetails) {
        Booking booking = bookingService.findByReference(reference)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + reference));
        if (isAdmin(userDetails)) return booking;

        AppUser currentUser = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        if (booking.getCreatedBy() == null
                || !booking.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Booking not found: " + reference);
        }
        return booking;
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
