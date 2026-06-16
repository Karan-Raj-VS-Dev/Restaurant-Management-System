package com.restaurant.order;

public record OrderStatusHistoryResponse(
        String orderId,
        OrderStatus status,
        java.util.List<OrderStatusTrailEntry> statusTrail,
        java.time.Instant updatedAt
) {
}
