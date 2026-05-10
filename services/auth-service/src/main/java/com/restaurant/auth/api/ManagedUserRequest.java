package com.restaurant.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ManagedUserRequest(
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Temporary password is required")
        @Size(min = 8, message = "Temporary password must be at least 8 characters")
        String temporaryPassword,
        @NotBlank(message = "Phone country code is required")
        String phoneCountryCode,
        @NotBlank(message = "Phone number is required")
        String phoneNumber,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotBlank(message = "Address is required")
        String addressLine,
        @NotEmpty(message = "At least one property must be mapped")
        List<String> mappedPropertyIds,
        Double latitude,
        Double longitude
) {
}
