package com.restaurant.inventory;

public record InventoryStockImportRequest(
        String fileName,
        String fileContent
) {
}
