package com.restaurant.catalog;

import java.math.BigDecimal;
import java.util.List;

public record MenuSettingsItemRequest(
        String itemCode,
        String itemName,
        String categoryName,
        String description,
        BigDecimal price,
        boolean vegetarian,
        int prepTimeMinutes,
        String status,
        List<RecipeIngredientInput> recipe
) {
}
