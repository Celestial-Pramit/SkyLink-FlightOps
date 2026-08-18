package com.skylink.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skylink.app.record.WeeklyBookingPoint;
import com.skylink.app.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final IDashboardService dashboardService;
    private final ObjectMapper objectMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        model.addAttribute("recentBookings", dashboardService.getRecentBookings(5));
        dashboardService.getNextDeparture()
            .ifPresent(card -> model.addAttribute("nextFlight", card));

        List<WeeklyBookingPoint> trend = dashboardService.getWeeklyBookingTrend();
        model.addAttribute("chartLabels", toJson(trend.stream()
            .map(WeeklyBookingPoint::label).toList()));
        model.addAttribute("chartCounts", toJson(trend.stream()
            .map(WeeklyBookingPoint::count).toList()));
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activePage", "dashboard");
        return "dashboard/index";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization failed for dashboard chart data", e);
            return "[]";
        }
    }
}
