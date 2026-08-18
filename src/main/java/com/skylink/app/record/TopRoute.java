package com.skylink.app.record;

import java.math.BigDecimal;

public record TopRoute(String originIata, String originCity,
                       String destinationIata, String destinationCity,
                       long totalBookings, BigDecimal totalRevenue) {
    public String getRoute() {
        return originIata + " -> " + destinationIata;
    }

    public String getFullRoute() {
        return originCity + " -> " + destinationCity;
    }
}
