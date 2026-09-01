package com.skylink.app.service;

import com.skylink.app.record.DashboardStats;
import com.skylink.app.record.RecentBookingRow;
import com.skylink.app.record.UpcomingFlightCard;
import com.skylink.app.record.WeeklyBookingPoint;
import com.skylink.app.entity.Flight;

import java.util.List;
import java.util.Optional;

public interface IDashboardService {
    DashboardStats getStats();
    List<RecentBookingRow> getRecentBookings(int limit);
    Optional<UpcomingFlightCard> getNextDeparture();
    List<WeeklyBookingPoint> getWeeklyBookingTrend();
    List<Flight> getUrgentFlights();
}
