package com.restaurant.platform.eventing.contract;

import java.time.Instant;
import java.util.List;

public record OrderSubmittedToKitchenEvent(
        String orderId,
        String tenantId,
        String propertyId,
        String tableId,
        String waiterId,
        List<OrderLineItem> items,
        Instant submittedAt
) {
}
