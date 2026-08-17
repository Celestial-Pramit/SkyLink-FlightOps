package com.skylink.app.controller;

import com.skylink.app.dto.FlightDto;
import com.skylink.app.dto.FlightSearchDto;
import com.skylink.app.entity.Flight;
import com.skylink.app.enums.AircraftStatus;
import com.skylink.app.enums.FlightStatus;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.repository.AircraftRepository;
import com.skylink.app.repository.AirportRepository;
import com.skylink.app.service.IFlightService;
import com.skylink.app.util.FlightMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightController {

    private final IFlightService flightService;
    private final FlightMapper flightMapper;
    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) Long originAirportId,
            @RequestParam(required = false) Long destinationAirportId,
            @RequestParam(required = false) FlightStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @RequestParam(required = false) String flightNumber,
            Model model) {

        FlightSearchDto search = new FlightSearchDto();
        search.setOriginAirportId(originAirportId);
        search.setDestinationAirportId(destinationAirportId);
        search.setStatus(status);
        search.setDepartureDate(departureDate);
        search.setFlightNumber(flightNumber);

        var allFlights = flightService.findAll();
        var flights = search.isEmpty()
            ? allFlights
            : flightService.searchFull(
                originAirportId, destinationAirportId, status, departureDate, flightNumber);

        model.addAttribute("flights", flights);
        model.addAttribute("search", search);
        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("statuses", FlightStatus.values());
        model.addAttribute("scheduledCount", countByStatus(allFlights, FlightStatus.SCHEDULED));
        model.addAttribute("boardingCount", countByStatus(allFlights, FlightStatus.BOARDING));
        model.addAttribute("departedCount", countByStatus(allFlights, FlightStatus.DEPARTED));
        model.addAttribute("cancelledCount", countByStatus(allFlights, FlightStatus.CANCELLED));
        model.addAttribute("totalCount", allFlights.size());
        model.addAttribute("pageTitle", "Flights");
        model.addAttribute("activePage", "flights");
        return "flights/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Flight flight = flightService.findById(id);
        model.addAttribute("flight", flight);
        model.addAttribute("pageTitle", "Flight " + flight.getFlightNumber());
        model.addAttribute("activePage", "flights");
        return "flights/detail";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("flightDto", new FlightDto());
        populateFormModel(model, false, "Add Flight");
        return "flights/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String processAdd(
            @Valid @ModelAttribute("flightDto") FlightDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateCrossFields(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            populateFormModel(model, false, "Add Flight");
            return "flights/form";
        }

        try {
            flightService.save(flightMapper.fromDto(dto));
            addFlash(redirectAttributes, "success",
                "Flight " + dto.getFlightNumber() + " added successfully.");
            return "redirect:/flights";
        } catch (BusinessRuleException e) {
            bindingResult.rejectValue("flightNumber", "flight.rule", e.getMessage());
            populateFormModel(model, false, "Add Flight");
            return "flights/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Flight flight = flightService.findById(id);
        model.addAttribute("flightDto", flightMapper.toDto(flight));
        populateFormModel(model, true, "Edit Flight " + flight.getFlightNumber());
        return "flights/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String processEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute("flightDto") FlightDto dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        validateCrossFields(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            populateFormModel(model, true, "Edit Flight");
            return "flights/form";
        }

        try {
            flightService.update(id, flightMapper.fromDto(dto));
            addFlash(redirectAttributes, "success",
                "Flight " + dto.getFlightNumber() + " updated successfully.");
            return "redirect:/flights";
        } catch (BusinessRuleException e) {
            bindingResult.rejectValue("flightNumber", "flight.rule", e.getMessage());
            populateFormModel(model, true, "Edit Flight");
            return "flights/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Flight flight = flightService.findById(id);
            String flightNumber = flight.getFlightNumber();
            flightService.delete(id);
            addFlash(redirectAttributes, "success", "Flight " + flightNumber + " deleted.");
        } catch (BusinessRuleException e) {
            addFlash(redirectAttributes, "warning", e.getMessage());
        } catch (Exception e) {
            log.error("Delete failed for flight id {}", id, e);
            addFlash(redirectAttributes, "error", "Delete failed. Please try again.");
        }
        return "redirect:/flights";
    }

    @GetMapping("/schedule")
    public String schedule(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {

        YearMonth current = year != null && month != null
            ? YearMonth.of(year, month)
            : YearMonth.now();

        model.addAttribute("currentMonth", current);
        model.addAttribute("prevMonth", current.minusMonths(1));
        model.addAttribute("nextMonth", current.plusMonths(1));
        model.addAttribute("flights", flightService.findInMonth(
            current.getYear(), current.getMonthValue()));
        model.addAttribute("todayDate", LocalDate.now());
        model.addAttribute("firstDay", current.atDay(1).getDayOfWeek().getValue());
        model.addAttribute("daysInMonth", current.lengthOfMonth());
        model.addAttribute("pageTitle", "Flight Schedule");
        model.addAttribute("activePage", "flights");
        return "flights/schedule";
    }

    private long countByStatus(Iterable<Flight> flights, FlightStatus status) {
        long count = 0;
        for (Flight flight : flights) {
            if (flight.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    private void validateCrossFields(FlightDto dto, BindingResult bindingResult) {
        if (!dto.isArrivalAfterDeparture()) {
            bindingResult.rejectValue("arrivalTime", "time.order",
                "Arrival must be after departure.");
        }
        if (!dto.isDifferentAirports()) {
            bindingResult.rejectValue("destinationAirportId", "airports.same",
                "Origin and destination cannot be the same airport.");
        }
    }

    private void populateFormModel(Model model, boolean isEdit, String pageTitle) {
        model.addAttribute("statuses", FlightStatus.values());
        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("aircraft", aircraftRepository.findByStatus(AircraftStatus.ACTIVE));
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activePage", "flights");
    }

    private void addFlash(RedirectAttributes attributes, String type, String message) {
        attributes.addFlashAttribute("flashType", type);
        attributes.addFlashAttribute("flashMessage", message);
    }
}
