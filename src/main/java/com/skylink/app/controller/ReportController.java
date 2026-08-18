package com.skylink.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skylink.app.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReportController {
    private final IReportService reportService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String reports(@RequestParam(defaultValue = "30") int days,
                          @RequestParam(defaultValue = "8") int routeLimit, Model model) {
        int safeDays = whitelist(days, 30, 7, 14, 30);
        int safeLimit = whitelist(routeLimit, 8, 5, 8, 10);
        model.addAttribute("days", safeDays);
        model.addAttribute("routeLimit", safeLimit);
        model.addAttribute("bookingsByDay", json(reportService.getBookingsByDay(safeDays)));
        model.addAttribute("revenueByRoute", json(reportService.getRevenueByRoute(safeLimit)));
        model.addAttribute("statusBreakdown", json(reportService.getStatusBreakdown()));
        model.addAttribute("topRoutes", reportService.getTopRoutes(safeLimit));
        model.addAttribute("totalBookings", reportService.getTotalBookings());
        model.addAttribute("totalRevenue", reportService.getTotalRevenue());
        model.addAttribute("totalCustomers", reportService.getTotalCustomers());
        model.addAttribute("totalFlights", reportService.getTotalFlights());
        model.addAttribute("pageTitle", "Reports");
        model.addAttribute("activePage", "reports");
        return "reports/index";
    }

    private int whitelist(int value, int fallback, int... allowed) {
        for (int candidate : allowed) if (value == candidate) return value;
        return fallback;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException ex) { return "[]"; }
    }
}
