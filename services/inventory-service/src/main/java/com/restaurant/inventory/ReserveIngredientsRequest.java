package com.restaurant.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReserveIngredientsRequest(
        @NotBlank String referenceId,
        @NotBlank String propertyId,
        @NotEmpty List<IngredientReservation> ingredients
) {
}
