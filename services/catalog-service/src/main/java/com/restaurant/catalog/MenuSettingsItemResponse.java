package com.restaurant.catalog;

import java.math.BigDecimal;
import java.util.List;

public record MenuSettingsItemResponse(
        String menuItemId,
        String itemCode,
        String itemName,
        String categoryId,
        String categoryName,
        String description,
        BigDecimal price,
        int recipeCount,
        boolean active,
        boolean vegetarian,
        int prepTimeMinutes,
        List<RecipeIngredient> recipe
) {
}
