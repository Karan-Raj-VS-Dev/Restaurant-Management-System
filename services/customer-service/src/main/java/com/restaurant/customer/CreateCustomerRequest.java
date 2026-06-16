package com.restaurant.customer;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        String name,
        @NotBlank String phoneNumber
) {
}
