package com.restaurant.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    private InventoryController controller;

    @BeforeEach
    void setUp() {
        controller = new InventoryController(inventoryService);
    }

    @Test
    void listStockUsesDefaultScope() {
        List<StockItemResponse> expected = List.of(new StockItemResponse("ing-001", "krusty-krab", "Cream", 5, "ml", 1, 10, "STABLE"));
        when(inventoryService.listStock("bikini-bottom", "krusty-krab")).thenReturn(expected);

        List<StockItemResponse> response = controller.listStock(null, null, null, null);

        assertThat(response).isEqualTo(expected);
        verify(inventoryService).listStock("bikini-bottom", "krusty-krab");
    }

    @Test
    void validateMenuOrderDelegatesToService() {
        ValidateMenuOrderRequest request = new ValidateMenuOrderRequest(List.of(new ValidateMenuOrderItemRequest("item-001", "Pizza", 2)));
        MenuOrderValidationResponse expected = new MenuOrderValidationResponse(true, List.of());
        when(inventoryService.validateMenuOrder("tenant-path", "property-path", request)).thenReturn(expected);

        MenuOrderValidationResponse response = controller.validateMenuOrder("tenant-path", "property-path", "tenant-query", "property-query", request);

        assertThat(response).isEqualTo(expected);
        verify(inventoryService).validateMenuOrder("tenant-path", "property-path", request);
    }

    @Test
    void createAndUpdateIngredientAndSupplyDelegateToService() {
        UpsertIngredientSettingsRequest ingredientRequest = new UpsertIngredientSettingsRequest("ing-001", "Cream", "ml", 2, 10, 0.5, "ACTIVE");
        IngredientSettingsItemResponse ingredientResponse = new IngredientSettingsItemResponse("ing-001", "ing-001", "Cream", "ml", 2, 10, 0.5, "ACTIVE");
        UpsertSupplySettingsRequest supplyRequest = new UpsertSupplySettingsRequest("sup-001", "Wipes", "pieces", 3, 10.0, "ACTIVE");
        SupplySettingsItemResponse supplyResponse = new SupplySettingsItemResponse("sup-001", "sup-001", "Wipes", "pieces", 3, 10.0, "ACTIVE");
        when(inventoryService.createIngredientSetting("bikini-bottom", "krusty-krab", ingredientRequest)).thenReturn(ingredientResponse);
        when(inventoryService.updateIngredientSetting("bikini-bottom", "krusty-krab", "ing-001", ingredientRequest)).thenReturn(ingredientResponse);
        when(inventoryService.createSupplySetting("bikini-bottom", "krusty-krab", supplyRequest)).thenReturn(supplyResponse);
        when(inventoryService.updateSupplySetting("bikini-bottom", "krusty-krab", "sup-001", supplyRequest)).thenReturn(supplyResponse);

        assertThat(controller.createIngredientSetting(null, null, null, null, ingredientRequest)).isEqualTo(ingredientResponse);
        assertThat(controller.updateIngredientSetting("ing-001", null, null, null, null, ingredientRequest)).isEqualTo(ingredientResponse);
        assertThat(controller.createSupplySetting(null, null, null, null, supplyRequest)).isEqualTo(supplyResponse);
        assertThat(controller.updateSupplySetting("sup-001", null, null, null, null, supplyRequest)).isEqualTo(supplyResponse);
    }

    @Test
    void reserveIngredientsUsesRequestPropertyWhenQueryIsMissing() {
        ReserveIngredientsRequest request = new ReserveIngredientsRequest("ref-001", "krusty-krab", List.of(new IngredientReservation("ing-001", "Cream", 2, "ml")));
        ReservationResponse expected = new ReservationResponse("resv-001", "ref-001", true, "Reserved");
        when(inventoryService.reserveIngredients("bikini-bottom", "krusty-krab", request)).thenReturn(expected);

        ReservationResponse response = controller.reserveIngredients(null, null, null, null, request);

        assertThat(response).isEqualTo(expected);
        verify(inventoryService).reserveIngredients("bikini-bottom", "krusty-krab", request);
    }
}
