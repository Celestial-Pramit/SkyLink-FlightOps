package com.skylink.app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skylink.app.record.TopRoute;
import com.skylink.app.service.IReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
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

    @GetMapping("/export")
    public void exportCsv(@RequestParam(defaultValue = "30") int days,
                          @RequestParam(defaultValue = "10") int routeLimit,
                          HttpServletResponse response) throws IOException {
        int safeDays = whitelist(days, 30, 7, 14, 30);
        int safeLimit = whitelist(routeLimit, 10, 5, 8, 10);
        String filename = "skylink-report-" + LocalDate.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("SkyLink Ops - Report Export");
            writer.println("Generated," + LocalDate.now());
            writer.println("Period (days)," + safeDays);
            writer.println();
            writer.println("SUMMARY");
            writer.println("Total Bookings," + reportService.getTotalBookings());
            writer.println("Total Revenue (BDT)," + money(reportService.getTotalRevenue()));
            writer.println("Total Customers," + reportService.getTotalCustomers());
            writer.println("Total Flights," + reportService.getTotalFlights());
            writer.println();

            writer.println("TOP ROUTES");
            writer.println("Rank,Origin,Destination,Total Bookings,Revenue (BDT)");
            List<TopRoute> routes = reportService.getTopRoutes(safeLimit);
            for (int i = 0; i < routes.size(); i++) {
                TopRoute route = routes.get(i);
                writer.printf("%d,%s,%s,%d,%s%n",
                    i + 1,
                    csvSafe(route.originIata() + " - " + route.originCity()),
                    csvSafe(route.destinationIata() + " - " + route.destinationCity()),
                    route.totalBookings(),
                    money(route.totalRevenue()));
            }
            writer.println();

            writer.println("STATUS BREAKDOWN");
            writer.println("Status,Count");
            reportService.getStatusBreakdown().forEach(status ->
                writer.println(csvSafe(status.status()) + "," + status.count()));
            writer.println();

            writer.println("BOOKINGS BY DAY (last " + safeDays + " days)");
            writer.println("Date,Bookings");
            reportService.getBookingsByDay(safeDays).forEach(day ->
                writer.println(csvSafe(day.label()) + "," + day.count()));
        }
    }

    private int whitelist(int value, int fallback, int... allowed) {
        for (int candidate : allowed) if (value == candidate) return value;
        return fallback;
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException ex) {
            log.error("Could not serialize report data", ex);
            return "[]";
        }
    }

    private String money(java.math.BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private String csvSafe(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
