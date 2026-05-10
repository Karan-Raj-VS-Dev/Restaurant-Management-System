package com.restaurant.inventory;

import java.util.List;

public record InventorySettingsSummaryResponse(
        List<IngredientSettingsItemResponse> ingredients,
        List<SupplySettingsItemResponse> supplies,
        List<String> ingredientFields,
        List<String> supplyFields
) {
}
