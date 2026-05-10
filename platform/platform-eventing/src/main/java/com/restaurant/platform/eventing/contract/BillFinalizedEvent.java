package com.restaurant.platform.eventing.contract;

import java.math.BigDecimal;
import java.time.Instant;

public record BillFinalizedEvent(
        String billId,
        String orderId,
        String tenantId,
        String propertyId,
        BigDecimal total,
        Instant occurredAt
) {
}
