package com.restaurant.insights;

import java.math.BigDecimal;

public record DailyOperationsInsight(
        String propertyId,
        int totalOrdersToday,
        String busiestTableId,
        String topServerId,
        int topServerCustomerCount,
        BigDecimal grossSalesToday
) {
}
