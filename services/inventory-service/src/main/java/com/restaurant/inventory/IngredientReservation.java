package com.restaurant.inventory;

import jakarta.validation.constraints.NotBlank;

public record IngredientReservation(
        @NotBlank String ingredientId,
        @NotBlank String ingredientName,
        int quantity,
        @NotBlank String unit
) {
}
