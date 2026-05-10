package com.restaurant.review;

import java.time.Instant;

public record ReviewRequestResponse(
        String reviewRequestId,
        String billId,
        String tenantId,
        String propertyId,
        String status,
        Instant requestedAt
) {
}
