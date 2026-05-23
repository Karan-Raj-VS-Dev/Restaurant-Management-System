package com.restaurant.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ValidateMenuOrderRequest(
        @NotEmpty List<@Valid ValidateMenuOrderItemRequest> items
) {
}
