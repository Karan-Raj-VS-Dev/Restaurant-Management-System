package com.restaurant.customer;

import java.time.Instant;

public record CustomerResponse(
        String customerId,
        String tenantId,
        String propertyId,
        String name,
        String phoneNumber,
        Instant createdAt
) {
}
