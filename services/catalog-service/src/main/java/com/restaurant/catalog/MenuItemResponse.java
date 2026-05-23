package com.restaurant.catalog;

import java.math.BigDecimal;
import java.util.List;

public record MenuItemResponse(
        String itemId,
        String propertyId,
        String name,
        String categoryId,
        String categoryName,
        BigDecimal price,
        boolean available,
        List<RecipeIngredient> recipe
) {
}
