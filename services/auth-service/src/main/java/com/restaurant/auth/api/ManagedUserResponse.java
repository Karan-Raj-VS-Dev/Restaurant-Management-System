package com.restaurant.auth.api;

import java.time.Instant;
import java.util.List;

public record ManagedUserResponse(
        String userId,
        String tenantId,
        String defaultPropertyId,
        List<String> mappedPropertyIds,
        String firstName,
        String lastName,
        String fullName,
        String username,
        String email,
        String phoneCountryCode,
        String phoneNumber,
        String addressLine,
        Double latitude,
        Double longitude,
        String status,
        boolean adminUser,
        boolean mustChangePassword,
        Instant lastLoginAt,
        Instant createdAt
) {
}
