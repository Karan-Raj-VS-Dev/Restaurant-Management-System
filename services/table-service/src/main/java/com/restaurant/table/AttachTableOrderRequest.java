package com.restaurant.table;

import jakarta.validation.constraints.NotBlank;

public record AttachTableOrderRequest(
        @NotBlank String orderId
) {
}
