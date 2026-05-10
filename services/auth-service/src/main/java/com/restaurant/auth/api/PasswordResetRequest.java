package com.restaurant.auth.api;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "Email or phone number is required")
        String identifier
) {
}
