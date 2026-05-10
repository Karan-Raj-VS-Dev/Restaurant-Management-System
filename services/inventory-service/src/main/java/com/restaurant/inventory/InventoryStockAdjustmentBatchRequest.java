package com.restaurant.inventory;

import java.util.List;

public record InventoryStockAdjustmentBatchRequest(
        List<InventoryStockAdjustmentRequest> adjustments,
        String reason
) {
}
