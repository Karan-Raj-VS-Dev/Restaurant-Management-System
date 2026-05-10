package com.restaurant.platform.eventing.contract;

import java.time.Instant;
import java.util.List;

public record StockReservedEvent(
        String referenceId,
        String tenantId,
        String propertyId,
        List<IngredientReservationItem> ingredients,
        Instant occurredAt
) {
}
