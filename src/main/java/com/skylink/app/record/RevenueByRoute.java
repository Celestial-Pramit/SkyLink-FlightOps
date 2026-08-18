package com.skylink.app.record;

import java.math.BigDecimal;

public record RevenueByRoute(String route, BigDecimal revenue, long bookings) {
}
