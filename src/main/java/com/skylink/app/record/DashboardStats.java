package com.skylink.app.record;

import java.math.BigDecimal;

public record DashboardStats(
    long totalFlights,
    long bookingsToday,
    long totalCustomers,
    long activeAircraft,
    long totalBookings,
    BigDecimal totalRevenue
) {}
