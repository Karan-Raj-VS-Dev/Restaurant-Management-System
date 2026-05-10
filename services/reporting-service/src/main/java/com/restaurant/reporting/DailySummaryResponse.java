package com.restaurant.reporting;

import java.math.BigDecimal;

public record DailySummaryResponse(
        String propertyId,
        int totalOrders,
        BigDecimal grossSales,
        int completedTakeawayOrders,
        int pendingKitchenTickets
) {
}
