package com.restaurant.auth.api;

public record PasswordUpdateResponse(
        String username,
        String message
) {
}
