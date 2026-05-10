package com.restaurant.review;

import java.time.Instant;

public record ReviewRequestRecord(
        String reviewRequestId,
        String billId,
        String tenantId,
        String propertyId,
        String customerId,
        String status,
        Instant requestedAt
) {
}
