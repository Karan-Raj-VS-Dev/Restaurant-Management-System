package com.restaurant.platform.eventing.contract;

import java.time.Instant;

public record ReviewSubmittedEvent(
        String reviewId,
        String billId,
        String tenantId,
        String propertyId,
        String customerId,
        int rating,
        Instant submittedAt
) {
}
