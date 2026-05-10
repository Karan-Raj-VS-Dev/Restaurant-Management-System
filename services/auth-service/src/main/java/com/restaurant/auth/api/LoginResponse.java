package com.restaurant.auth.api;

public record LoginResponse(
        String userId,
        String tenantId,
        String defaultPropertyId,
        java.util.List<String> mappedPropertyIds,
        String username,
        String fullName,
        boolean adminUser,
        boolean mustChangePassword,
        String landingPage
) {
}
