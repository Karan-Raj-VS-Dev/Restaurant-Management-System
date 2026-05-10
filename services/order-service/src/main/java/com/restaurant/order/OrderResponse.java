package com.restaurant.order;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderId,
        String propertyId,
        String tableId,
        String waiterId,
        OrderStatus status,
        List<OrderItem> items,
        Instant createdAt
) {
}
