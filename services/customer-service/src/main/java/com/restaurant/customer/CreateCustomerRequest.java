package com.restaurant.customer;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank String name,
        @NotBlank String phoneNumber,
        String email
) {
}
