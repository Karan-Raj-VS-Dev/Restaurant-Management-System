package com.restaurant.insights;

import java.math.BigDecimal;

public record DailySnapshot(
        String tenantId,
        String propertyId,
        int totalOrdersToday,
        String busiestTableId,
        String topServerId,
        int topServerCustomerCount,
        BigDecimal grossSalesToday
) {
}
