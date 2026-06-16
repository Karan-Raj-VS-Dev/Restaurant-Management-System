package com.restaurant.order;

import java.time.Instant;

public record OrderStatusTrailEntry(
        OrderStatus status,
        String changedBy,
        String remarks,
        Instant changedAt
) {
}
