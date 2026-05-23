package com.restaurant.inventory;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({
        "/api/inventory",
        "/chefy/tenant/{tenantId}/property/{propertyId}/api/inventory"
})
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/stock")
    public List<StockItemResponse> listStock(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return inventoryService.listStock(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @GetMapping("/availability/menu-items")
    public List<MenuAvailabilityResponse> getMenuAvailability(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return inventoryService.getMenuAvailability(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @PostMapping("/availability/menu-items/validate-order")
    public MenuOrderValidationResponse validateMenuOrder(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @Valid @RequestBody ValidateMenuOrderRequest request
    ) {
        return inventoryService.validateMenuOrder(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @GetMapping("/settings/inventory")
    public InventorySettingsSummaryResponse getSettingsSummary(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId
    ) {
        return inventoryService.getSettingsSummary(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId)
        );
    }

    @PostMapping("/settings/inventory/ingredients")
    public IngredientSettingsItemResponse createIngredientSetting(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertIngredientSettingsRequest request
    ) {
        return inventoryService.createIngredientSetting(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PatchMapping("/settings/inventory/ingredients/{ingredientId}")
    public IngredientSettingsItemResponse updateIngredientSetting(
            @PathVariable String ingredientId,
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertIngredientSettingsRequest request
    ) {
        return inventoryService.updateIngredientSetting(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                ingredientId,
                request
        );
    }

    @PostMapping("/settings/inventory/supplies")
    public SupplySettingsItemResponse createSupplySetting(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertSupplySettingsRequest request
    ) {
        return inventoryService.createSupplySetting(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PatchMapping("/settings/inventory/supplies/{supplyId}")
    public SupplySettingsItemResponse updateSupplySetting(
            @PathVariable String supplyId,
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody UpsertSupplySettingsRequest request
    ) {
        return inventoryService.updateSupplySetting(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                supplyId,
                request
        );
    }

    @PostMapping("/stock/adjustments")
    public List<StockItemResponse> applyStockAdjustments(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody InventoryStockAdjustmentBatchRequest request
    ) {
        return inventoryService.applyStockAdjustments(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PostMapping("/stock/import")
    public List<StockItemResponse> importStockSheet(
            @PathVariable(name = "tenantId", required = false) String scopedTenantId,
            @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
            @RequestParam(name = "tenantId", required = false) String tenantId,
            @RequestParam(name = "propertyId", required = false) String propertyId,
            @RequestBody InventoryStockImportRequest request
    ) {
        return inventoryService.importStockSheet(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId),
                request
        );
    }

    @PostMapping("/ingredients/reserve")
    public ReservationResponse reserveIngredients(@PathVariable(name = "tenantId", required = false) String scopedTenantId,
                                                  @PathVariable(name = "propertyId", required = false) String scopedPropertyId,
                                                  @RequestParam(name = "tenantId", required = false) String tenantId,
                                                  @RequestParam(name = "propertyId", required = false) String propertyId,
                                                  @Valid @RequestBody ReserveIngredientsRequest request) {
        return inventoryService.reserveIngredients(
                resolveTenantId(scopedTenantId, tenantId),
                resolvePropertyId(scopedPropertyId, propertyId != null ? propertyId : request.propertyId()),
                request
        );
    }

    private String resolveTenantId(String pathTenantId, String requestTenantId) {
        if (pathTenantId != null && !pathTenantId.isBlank()) {
            return pathTenantId;
        }
        if (requestTenantId != null && !requestTenantId.isBlank()) {
            return requestTenantId;
        }
        return "bikini-bottom";
    }

    private String resolvePropertyId(String pathPropertyId, String requestPropertyId) {
        if (pathPropertyId != null && !pathPropertyId.isBlank()) {
            return pathPropertyId;
        }
        if (requestPropertyId != null && !requestPropertyId.isBlank()) {
            return requestPropertyId;
        }
        return "krusty-krab";
    }
}
