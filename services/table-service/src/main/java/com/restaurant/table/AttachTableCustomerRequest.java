package com.restaurant.table;

import jakarta.validation.constraints.NotBlank;

public record AttachTableCustomerRequest(
        @NotBlank String customerId
) {
}
