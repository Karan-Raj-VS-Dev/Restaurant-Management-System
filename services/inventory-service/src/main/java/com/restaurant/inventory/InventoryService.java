package com.restaurant.inventory;

import com.restaurant.inventory.persistence.entity.OperationalSupplyEntity;
import com.restaurant.inventory.persistence.repository.CatalogIngredientRepository;
import com.restaurant.inventory.persistence.repository.OperationalSupplyRepository;
import com.restaurant.platform.eventing.AggregateTypes;
import com.restaurant.platform.eventing.DomainEventPublisher;
import com.restaurant.platform.eventing.EventEnvelopeFactory;
import com.restaurant.platform.eventing.EventKeys;
import com.restaurant.platform.eventing.contract.IngredientReservationItem;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import com.restaurant.platform.eventing.contract.StockAlertEvent;
import com.restaurant.platform.eventing.contract.StockReservedEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private final InventoryProjection inventoryProjection;
    private final CatalogIngredientRepository catalogIngredientRepository;
    private final OperationalSupplyRepository operationalSupplyRepository;
    private final EventEnvelopeFactory eventEnvelopeFactory;
    private final DomainEventPublisher domainEventPublisher;

    public InventoryService(InventoryProjection inventoryProjection,
                            CatalogIngredientRepository catalogIngredientRepository,
                            OperationalSupplyRepository operationalSupplyRepository,
                            EventEnvelopeFactory eventEnvelopeFactory,
                            DomainEventPublisher domainEventPublisher) {
        this.inventoryProjection = inventoryProjection;
        this.catalogIngredientRepository = catalogIngredientRepository;
        this.operationalSupplyRepository = operationalSupplyRepository;
        this.eventEnvelopeFactory = eventEnvelopeFactory;
        this.domainEventPublisher = domainEventPublisher;
    }

    public List<StockItemResponse> listStock(String tenantId, String propertyId) {
        return inventoryProjection.listStock(tenantId, propertyId).stream()
                .map(stock -> new StockItemResponse(
                        stock.ingredientId(),
                        stock.propertyId(),
                        stock.ingredientName(),
                        stock.currentQuantity(),
                        stock.unit(),
                        stock.reorderThreshold(),
                        stock.maximumCapacity(),
                        determineStockHealth(stock)
                ))
                .toList();
    }

    public List<MenuAvailabilityResponse> getMenuAvailability(String tenantId, String propertyId) {
        return inventoryProjection.getMenuAvailability(tenantId, propertyId).stream()
                .map(item -> new MenuAvailabilityResponse(item.itemId(), item.propertyId(), item.available(), item.reason()))
                .toList();
    }

    public MenuOrderValidationResponse validateMenuOrder(String tenantId, String propertyId, ValidateMenuOrderRequest request) {
        return inventoryProjection.validateMenuOrder(tenantId, propertyId, request.items());
    }

    public InventorySettingsSummaryResponse getSettingsSummary(String tenantId, String propertyId) {
        List<IngredientStockRecord> stockRecords = inventoryProjection.listStock(tenantId, propertyId);
        Map<String, com.restaurant.inventory.persistence.entity.CatalogIngredientEntity> catalogIngredientsById = stockRecords.isEmpty()
                ? Map.of()
                : catalogIngredientRepository
                        .findByTenantIdAndPropertyIdAndIngredientIdIn(
                                tenantId,
                                propertyId,
                                stockRecords.stream().map(IngredientStockRecord::ingredientId).toList()
                        )
                        .stream()
                        .collect(Collectors.toMap(com.restaurant.inventory.persistence.entity.CatalogIngredientEntity::getIngredientId, Function.identity()));

        List<IngredientSettingsItemResponse> ingredients = stockRecords.stream()
                .map(stock -> new IngredientSettingsItemResponse(
                        stock.ingredientId(),
                        resolveIngredientCode(stock.ingredientId(), catalogIngredientsById),
                        stock.ingredientName(),
                        stock.unit(),
                        stock.reorderThreshold(),
                        stock.maximumCapacity(),
                        stock.marketUnitPrice(),
                        resolveIngredientStatus(stock.ingredientId(), catalogIngredientsById)
                ))
                .toList();

        return new InventorySettingsSummaryResponse(
                ingredients,
                operationalSupplyRepository.findByTenantIdAndPropertyIdOrderBySupplyNameAsc(tenantId, propertyId).stream()
                        .map(this::toSupplyResponse)
                        .toList(),
                List.of("ingredient code", "ingredient name", "unit of measure", "reorder threshold", "maximum capacity", "market price", "status"),
                List.of("supply code", "supply name", "unit", "reorder level", "market price", "status")
        );
    }

    public IngredientSettingsItemResponse createIngredientSetting(String tenantId, String propertyId, UpsertIngredientSettingsRequest request) {
        String ingredientId = "ing-" + randomId();
        IngredientStockRecord stock = inventoryProjection.upsertIngredientSettings(
                tenantId,
                propertyId,
                ingredientId,
                normalizeCode(request.ingredientCode()),
                request.ingredientName(),
                request.unit(),
                request.reorderThreshold(),
                request.maximumCapacity(),
                request.marketPrice(),
                request.status()
        );
        return new IngredientSettingsItemResponse(
                stock.ingredientId(),
                normalizeCode(request.ingredientCode()),
                stock.ingredientName(),
                stock.unit(),
                stock.reorderThreshold(),
                stock.maximumCapacity(),
                stock.marketUnitPrice(),
                defaultStatus(request.status())
        );
    }

    public IngredientSettingsItemResponse updateIngredientSetting(String tenantId, String propertyId, String ingredientId, UpsertIngredientSettingsRequest request) {
        IngredientStockRecord stock = inventoryProjection.upsertIngredientSettings(
                tenantId,
                propertyId,
                ingredientId,
                normalizeCode(request.ingredientCode()),
                request.ingredientName(),
                request.unit(),
                request.reorderThreshold(),
                request.maximumCapacity(),
                request.marketPrice(),
                request.status()
        );
        return new IngredientSettingsItemResponse(
                stock.ingredientId(),
                normalizeCode(request.ingredientCode()),
                stock.ingredientName(),
                stock.unit(),
                stock.reorderThreshold(),
                stock.maximumCapacity(),
                stock.marketUnitPrice(),
                defaultStatus(request.status())
        );
    }

    public List<StockItemResponse> applyStockAdjustments(String tenantId, String propertyId, InventoryStockAdjustmentBatchRequest request) {
        if (request.adjustments() == null || request.adjustments().isEmpty()) {
            throw new IllegalArgumentException("Add at least one stock change before confirming.");
        }

        List<StockItemResponse> updatedItems = new ArrayList<>();
        for (InventoryStockAdjustmentRequest adjustment : request.adjustments()) {
            if (adjustment.quantityDelta() == 0) {
                continue;
            }
            IngredientStockRecord updated = inventoryProjection.adjustStock(
                    tenantId,
                    propertyId,
                    adjustment.ingredientId(),
                    adjustment.quantityDelta()
            );
            if (updated == null) {
                throw new IllegalArgumentException("Unable to find ingredient for stock update: " + adjustment.ingredientId());
            }
            StockAlertEvent alert = inventoryProjection.toAlert(updated);
            if (alert != null) {
                domainEventPublisher.publish(eventEnvelopeFactory.create(
                        "OUT_OF_STOCK".equals(alert.stockHealth()) ? EventKeys.STOCK_OUT : EventKeys.STOCK_LOW,
                        AggregateTypes.STOCK,
                        alert.ingredientId(),
                        alert.propertyId(),
                        alert.ingredientId(),
                        null,
                        alert
                ));
            }
            updatedItems.add(toStockItemResponse(updated));
        }

        if (updatedItems.isEmpty()) {
            throw new IllegalArgumentException("No stock changes were entered.");
        }

        return listStock(tenantId, propertyId);
    }

    public List<StockItemResponse> importStockSheet(String tenantId, String propertyId, InventoryStockImportRequest request) {
        if (request.fileContent() == null || request.fileContent().isBlank()) {
            throw new IllegalArgumentException("Upload a stock sheet with at least one row.");
        }

        var ingredientsByName = inventoryProjection.listStock(tenantId, propertyId).stream()
                .collect(java.util.stream.Collectors.toMap(stock -> stock.ingredientName().trim().toLowerCase(Locale.ROOT), stock -> stock));

        List<InventoryStockAdjustmentRequest> adjustments = new ArrayList<>();
        String[] rows = request.fileContent().split("\\r?\\n");
        for (int index = 1; index < rows.length; index++) {
            String row = rows[index].trim();
            if (row.isEmpty()) {
                continue;
            }
            String[] columns = row.split(",", -1);
            if (columns.length < 5) {
                throw new IllegalArgumentException("Stock sheet row " + (index + 1) + " is incomplete.");
            }

            String ingredientName = columns[0].trim();
            String onHandDeltaText = columns[1].trim();
            String unit = columns[2].trim();
            String maximumCapacityText = columns[3].trim();
            String reorderThresholdText = columns[4].trim();

            IngredientStockRecord stock = ingredientsByName.get(ingredientName.toLowerCase(Locale.ROOT));
            if (stock == null) {
                throw new IllegalArgumentException("Unknown ingredient in stock sheet: " + ingredientName);
            }
            if (!stock.unit().equalsIgnoreCase(unit)) {
                throw new IllegalArgumentException("Unit mismatch for " + ingredientName + ". Keep the template columns unchanged.");
            }
            if (stock.maximumCapacity() != parseInteger(maximumCapacityText, "maximum capacity for " + ingredientName)) {
                throw new IllegalArgumentException("Maximum capacity changed for " + ingredientName + ". Keep the template columns unchanged.");
            }
            if (stock.reorderThreshold() != parseInteger(reorderThresholdText, "reorder threshold for " + ingredientName)) {
                throw new IllegalArgumentException("Reorder threshold changed for " + ingredientName + ". Keep the template columns unchanged.");
            }

            int quantityDelta = parseInteger(onHandDeltaText, "stock change for " + ingredientName);
            if (quantityDelta != 0) {
                adjustments.add(new InventoryStockAdjustmentRequest(stock.ingredientId(), quantityDelta));
            }
        }

        return applyStockAdjustments(tenantId, propertyId, new InventoryStockAdjustmentBatchRequest(adjustments, "CSV import"));
    }

    public SupplySettingsItemResponse createSupplySetting(String tenantId, String propertyId, UpsertSupplySettingsRequest request) {
        OperationalSupplyEntity entity = new OperationalSupplyEntity();
        entity.setSupplyId("sup-" + randomId());
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setSupplyCode(normalizeCode(request.supplyCode()));
        entity.setSupplyName(request.supplyName().trim());
        entity.setUnitOfMeasure(request.unit().trim());
        entity.setReorderLevel(BigDecimal.valueOf(request.reorderLevel()));
        entity.setMarketUnitPrice(BigDecimal.valueOf(request.marketPrice()).setScale(2, RoundingMode.HALF_UP));
        entity.setStatus(defaultStatus(request.status()));
        return toSupplyResponse(operationalSupplyRepository.save(entity));
    }

    public SupplySettingsItemResponse updateSupplySetting(String tenantId, String propertyId, String supplyId, UpsertSupplySettingsRequest request) {
        OperationalSupplyEntity entity = operationalSupplyRepository.findByTenantIdAndPropertyIdAndSupplyId(tenantId, propertyId, supplyId)
                .orElseThrow(() -> new IllegalArgumentException("Supply was not found."));
        entity.setSupplyCode(normalizeCode(request.supplyCode()));
        entity.setSupplyName(request.supplyName().trim());
        entity.setUnitOfMeasure(request.unit().trim());
        entity.setReorderLevel(BigDecimal.valueOf(request.reorderLevel()));
        entity.setMarketUnitPrice(BigDecimal.valueOf(request.marketPrice()).setScale(2, RoundingMode.HALF_UP));
        entity.setStatus(defaultStatus(request.status()));
        return toSupplyResponse(operationalSupplyRepository.save(entity));
    }

    public ReservationResponse reserveIngredients(String tenantId, String propertyId, ReserveIngredientsRequest request) {
        ReservationResult result = inventoryProjection.reserveManual(
                request.referenceId(),
                tenantId,
                propertyId,
                request.ingredients().stream()
                        .map(item -> new IngredientReservationItem(item.ingredientId(), item.ingredientName(), item.quantity(), item.unit()))
                        .toList()
        );
        publishReservation(result, null);
        return new ReservationResponse(
                "resv-" + request.referenceId(),
                request.referenceId(),
                true,
                "Ingredients reserved for preparation"
        );
    }

    public void reserveForKitchenOrder(OrderSubmittedToKitchenEvent event, String causationId) {
        ReservationResult result = inventoryProjection.reserveForOrder(event);
        publishReservation(result, causationId);
    }

    public void publishReservation(ReservationResult result, String causationId) {
        domainEventPublisher.publish(eventEnvelopeFactory.create(
                EventKeys.STOCK_RESERVED,
                AggregateTypes.STOCK,
                result.referenceId(),
                result.propertyId(),
                result.referenceId(),
                causationId,
                new StockReservedEvent(result.referenceId(), result.tenantId(), result.propertyId(), result.reservedIngredients(), Instant.now())
        ));

        result.alerts().forEach(alert -> domainEventPublisher.publish(eventEnvelopeFactory.create(
                "OUT_OF_STOCK".equals(alert.stockHealth()) ? EventKeys.STOCK_OUT : EventKeys.STOCK_LOW,
                AggregateTypes.STOCK,
                alert.ingredientId(),
                alert.propertyId(),
                result.referenceId(),
                causationId,
                alert
        )));
    }

    private SupplySettingsItemResponse toSupplyResponse(OperationalSupplyEntity entity) {
        return new SupplySettingsItemResponse(
                entity.getSupplyId(),
                entity.getSupplyCode(),
                entity.getSupplyName(),
                entity.getUnitOfMeasure(),
                entity.getReorderLevel().intValue(),
                entity.getMarketUnitPrice().doubleValue(),
                entity.getStatus()
        );
    }

    private StockItemResponse toStockItemResponse(IngredientStockRecord stock) {
        return new StockItemResponse(
                stock.ingredientId(),
                stock.propertyId(),
                stock.ingredientName(),
                stock.currentQuantity(),
                stock.unit(),
                stock.reorderThreshold(),
                stock.maximumCapacity(),
                determineStockHealth(stock)
        );
    }

    private String determineStockHealth(IngredientStockRecord stock) {
        if (stock.currentQuantity() <= 0) {
            return "OUT_OF_STOCK";
        }
        if (stock.currentQuantity() <= stock.reorderThreshold()) {
            return "LOW_STOCK";
        }
        if (stock.maximumCapacity() > 0 && stock.currentQuantity() > stock.maximumCapacity()) {
            return "OVER_CAPACITY";
        }
        return "STABLE";
    }

    private int parseInteger(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid number for " + fieldName + ".");
        }
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return "code-" + randomId();
        }
        return value.trim();
    }

    private String resolveIngredientCode(String ingredientId, Map<String, com.restaurant.inventory.persistence.entity.CatalogIngredientEntity> catalogIngredientsById) {
        com.restaurant.inventory.persistence.entity.CatalogIngredientEntity entity = catalogIngredientsById.get(ingredientId);
        if (entity == null || entity.getIngredientCode() == null || entity.getIngredientCode().isBlank()) {
            return ingredientId;
        }
        return entity.getIngredientCode();
    }

    private String resolveIngredientStatus(String ingredientId, Map<String, com.restaurant.inventory.persistence.entity.CatalogIngredientEntity> catalogIngredientsById) {
        com.restaurant.inventory.persistence.entity.CatalogIngredientEntity entity = catalogIngredientsById.get(ingredientId);
        if (entity == null || entity.isActive()) {
            return "ACTIVE";
        }
        return "INACTIVE";
    }

    private String defaultStatus(String status) {
        return status == null || status.isBlank() ? "ACTIVE" : status.trim().toUpperCase(Locale.ROOT);
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toLowerCase(Locale.ROOT);
    }
}
