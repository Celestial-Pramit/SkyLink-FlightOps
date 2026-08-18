package com.skylink.app.service;

import com.skylink.app.record.BookingsByDay;
import com.skylink.app.record.RevenueByRoute;
import com.skylink.app.record.StatusBreakdown;
import com.skylink.app.record.TopRoute;

import java.math.BigDecimal;
import java.util.List;

public interface IReportService {
    List<BookingsByDay> getBookingsByDay(int days);
    List<RevenueByRoute> getRevenueByRoute(int limit);
    List<StatusBreakdown> getStatusBreakdown();
    List<TopRoute> getTopRoutes(int limit);
    long getTotalBookings();
    BigDecimal getTotalRevenue();
    long getTotalCustomers();
    long getTotalFlights();
}
