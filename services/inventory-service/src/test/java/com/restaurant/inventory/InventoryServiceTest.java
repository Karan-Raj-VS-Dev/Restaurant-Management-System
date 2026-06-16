package com.restaurant.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.inventory.persistence.entity.OperationalSupplyEntity;
import com.restaurant.inventory.persistence.repository.CatalogIngredientRepository;
import com.restaurant.inventory.persistence.repository.OperationalSupplyRepository;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelope;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.IngredientReservationItem;
import com.restaurant.platform.eventing.contract.StockAlertEvent;
import com.restaurant.platform.eventing.contract.StockReservedEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryProjection inventoryProjection;

    @Mock
    private CatalogIngredientRepository catalogIngredientRepository;

    @Mock
    private OperationalSupplyRepository operationalSupplyRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(
                inventoryProjection,
                catalogIngredientRepository,
                operationalSupplyRepository,
                new EventEnvelopeFactory("test-suite"),
                domainEventPublisher
        );
    }

    @Test
    void listStockMapsHealthAcrossThresholds() {
        when(inventoryProjection.listStock("bikini-bottom", "krusty-krab")).thenReturn(List.of(
                stock("ing-001", "Out", 0, 1, 10),
                stock("ing-002", "Low", 1, 2, 10),
                stock("ing-003", "Over", 20, 2, 10),
                stock("ing-004", "Stable", 5, 2, 10)
        ));

        List<StockItemResponse> response = inventoryService.listStock("bikini-bottom", "krusty-krab");

        assertThat(response).extracting(StockItemResponse::stockHealth)
                .containsExactly("OUT_OF_STOCK", "LOW_STOCK", "OVER_CAPACITY", "STABLE");
    }

    @Test
    void createIngredientSettingUsesProjectionAndDefaultsStatus() {
        when(inventoryProjection.upsertIngredientSettings(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyInt(),
                anyInt(),
                anyDouble(),
                any()
        ))
                .thenReturn(stock("ing-001", "Cream", 10, 2, 20));

        IngredientSettingsItemResponse response = inventoryService.createIngredientSetting(
                "bikini-bottom",
                "krusty-krab",
                new UpsertIngredientSettingsRequest("", "Cream", "ml", 2, 20, 0.5, "")
        );

        assertThat(response.ingredientCode()).startsWith("code-");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void applyStockAdjustmentsRejectsEmptyBatch() {
        assertThatThrownBy(() -> inventoryService.applyStockAdjustments(
                "bikini-bottom",
                "krusty-krab",
                new InventoryStockAdjustmentBatchRequest(List.of(), "Manual change")
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Add at least one stock change");
    }

    @Test
    void importStockSheetRejectsChangedReferenceColumns() {
        when(inventoryProjection.listStock("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(stock("ing-001", "Cream", 10, 2, 20)));

        assertThatThrownBy(() -> inventoryService.importStockSheet(
                "bikini-bottom",
                "krusty-krab",
                new InventoryStockImportRequest(
                        "inventory-template.csv",
                        "ingredient_name,on_hand,unit,maximum_capacity,reorder_threshold\nCream,2,grams,20,2"
                )
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unit mismatch");
    }

    @Test
    void applyStockAdjustmentsPublishesOutOfStockAlertAndReturnsLatestStock() {
        IngredientStockRecord adjusted = stock("ing-001", "Cream", 0, 2, 20);
        when(inventoryProjection.adjustStock("bikini-bottom", "krusty-krab", "ing-001", -10))
                .thenReturn(adjusted);
        when(inventoryProjection.toAlert(adjusted))
                .thenReturn(new StockAlertEvent(
                        "bikini-bottom",
                        "krusty-krab",
                        "ing-001",
                        "Cream",
                        "OUT_OF_STOCK",
                        0,
                        "ml",
                        Instant.parse("2026-06-15T10:15:00Z")
                ));
        when(inventoryProjection.listStock("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(adjusted));

        List<StockItemResponse> response = inventoryService.applyStockAdjustments(
                "bikini-bottom",
                "krusty-krab",
                new InventoryStockAdjustmentBatchRequest(
                        List.of(new InventoryStockAdjustmentRequest("ing-001", -10)),
                        "Manual change"
                )
        );

        assertThat(response).singleElement().extracting(StockItemResponse::stockHealth).isEqualTo("OUT_OF_STOCK");

        ArgumentCaptor<EventEnvelope<?>> eventCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(domainEventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventKey()).isEqualTo(EventKeys.STOCK_OUT);
        assertThat(eventCaptor.getValue().payload()).isInstanceOf(StockAlertEvent.class);
        StockAlertEvent payload = (StockAlertEvent) eventCaptor.getValue().payload();
        assertThat(payload.ingredientName()).isEqualTo("Cream");
        assertThat(payload.availableQuantity()).isZero();
    }

    @Test
    void publishReservationEmitsReservedAndLowStockEvents() {
        ReservationResult result = new ReservationResult(
                "order-001",
                "bikini-bottom",
                "krusty-krab",
                List.of(new IngredientReservationItem("ing-001", "Cream", 2, "ml")),
                List.of(new StockAlertEvent(
                        "bikini-bottom",
                        "krusty-krab",
                        "ing-001",
                        "Cream",
                        "LOW_STOCK",
                        1,
                        "ml",
                        Instant.parse("2026-06-15T10:20:00Z")
                ))
        );

        inventoryService.publishReservation(result, "cause-001");

        ArgumentCaptor<EventEnvelope<?>> eventCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(domainEventPublisher, times(2)).publish(eventCaptor.capture());
        List<EventEnvelope<?>> events = eventCaptor.getAllValues();

        assertThat(events).extracting(EventEnvelope::eventKey)
                .containsExactly(EventKeys.STOCK_RESERVED, EventKeys.STOCK_LOW);
        assertThat(events).extracting(EventEnvelope::causationId)
                .containsOnly("cause-001");
        assertThat(events.get(0).payload()).isInstanceOf(StockReservedEvent.class);
        StockReservedEvent reservedEvent = (StockReservedEvent) events.get(0).payload();
        assertThat(reservedEvent.referenceId()).isEqualTo("order-001");
        assertThat(reservedEvent.ingredients()).singleElement()
                .extracting(IngredientReservationItem::ingredientName)
                .isEqualTo("Cream");
    }

    @Test
    void getSettingsSummaryIncludesIngredientAndSupplyRows() {
        when(inventoryProjection.listStock("bikini-bottom", "krusty-krab")).thenReturn(List.of(stock("ing-001", "Cream", 10, 2, 20)));
        when(catalogIngredientRepository.findByTenantIdAndPropertyIdAndIngredientIdIn(any(), any(), any()))
                .thenReturn(List.of());
        when(operationalSupplyRepository.findByTenantIdAndPropertyIdOrderBySupplyNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(supply("sup-001", "Wipes")));

        InventorySettingsSummaryResponse response = inventoryService.getSettingsSummary("bikini-bottom", "krusty-krab");

        assertThat(response.ingredients()).singleElement().extracting(IngredientSettingsItemResponse::ingredientName).isEqualTo("Cream");
        assertThat(response.supplies()).singleElement().extracting(SupplySettingsItemResponse::supplyName).isEqualTo("Wipes");
    }

    private IngredientStockRecord stock(String ingredientId, String name, int current, int reorder, int max) {
        return new IngredientStockRecord(ingredientId, "bikini-bottom", "krusty-krab", name, current, "ml", reorder, max, 0.5);
    }

    private OperationalSupplyEntity supply(String supplyId, String name) {
        OperationalSupplyEntity entity = new OperationalSupplyEntity();
        entity.setSupplyId(supplyId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setSupplyCode(supplyId);
        entity.setSupplyName(name);
        entity.setUnitOfMeasure("pieces");
        entity.setReorderLevel(BigDecimal.valueOf(2));
        entity.setMarketUnitPrice(BigDecimal.valueOf(10));
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(Instant.parse("2026-06-15T10:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-06-15T10:00:00Z"));
        return entity;
    }
}
