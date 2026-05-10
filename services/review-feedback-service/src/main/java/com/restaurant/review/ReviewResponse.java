package com.restaurant.review;

import java.time.Instant;

public record ReviewResponse(
        String reviewId,
        String billId,
        String tenantId,
        String propertyId,
        String customerId,
        int rating,
        String comments,
        Instant createdAt
) {
}
