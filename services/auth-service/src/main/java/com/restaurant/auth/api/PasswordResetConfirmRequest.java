package com.restaurant.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank(message = "Email or phone number is required")
        String identifier,
        @NotBlank(message = "OTP is required")
        @Size(min = 4, max = 8)
        String otp,
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword
) {
}
