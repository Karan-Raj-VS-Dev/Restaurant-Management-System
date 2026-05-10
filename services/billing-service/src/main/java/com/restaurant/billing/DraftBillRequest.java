package com.restaurant.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DraftBillRequest(
        @NotBlank String orderId,
        @NotEmpty List<BillLine> items
) {
}
