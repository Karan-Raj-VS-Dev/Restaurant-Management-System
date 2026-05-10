package com.restaurant.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ManagedUserUpdateRequest(
        String firstName,
        String lastName,
        String username,
        @Size(min = 8, message = "Temporary password must be at least 8 characters")
        String temporaryPassword,
        String phoneCountryCode,
        String phoneNumber,
        @Email(message = "Email must be valid")
        String email,
        String addressLine,
        @NotEmpty(message = "At least one property must be mapped")
        List<String> mappedPropertyIds,
        Double latitude,
        Double longitude,
        String status
) {
}
