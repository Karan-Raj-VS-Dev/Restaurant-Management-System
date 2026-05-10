package com.restaurant.platform.eventing.contract;

public record IngredientReservationItem(
        String ingredientId,
        String ingredientName,
        int quantity,
        String unit
) {
}
