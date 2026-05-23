package com.restaurant.inventory;

import java.util.List;

public record MenuOrderValidationItemResponse(
        String itemId,
        String itemName,
        int requestedQuantity,
        int maxServableQuantity,
        List<String> shortageIngredients,
        String message
) {
}
