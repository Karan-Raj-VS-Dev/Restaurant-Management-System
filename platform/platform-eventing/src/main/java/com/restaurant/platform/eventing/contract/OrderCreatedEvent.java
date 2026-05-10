package com.restaurant.platform.eventing.contract;

import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String tenantId,
        String propertyId,
        String tableId,
        String waiterId,
        String customerId,
        List<OrderLineItem> items,
        Instant createdAt
) {
}
