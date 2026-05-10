package com.restaurant.inventory;

import com.restaurant.platform.eventing.contract.IngredientReservationItem;
import com.restaurant.platform.eventing.contract.StockAlertEvent;

import java.util.List;

public record ReservationResult(
        String referenceId,
        String tenantId,
        String propertyId,
        List<IngredientReservationItem> reservedIngredients,
        List<StockAlertEvent> alerts
) {
}
