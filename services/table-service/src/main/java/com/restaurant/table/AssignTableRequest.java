package com.restaurant.table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AssignTableRequest(
        @NotBlank String tableId,
        @NotBlank String propertyId,
        @Min(1) int capacity,
        @NotBlank String waiterId
) {
}
