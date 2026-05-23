package com.restaurant.inventory;

import java.util.List;

public record MenuOrderValidationResponse(
        boolean valid,
        List<MenuOrderValidationItemResponse> issues
) {
}
