package com.restaurant.inventory;

import com.restaurant.inventory.persistence.entity.CatalogIngredientEntity;
import com.restaurant.inventory.persistence.entity.CatalogMenuItemEntity;
import com.restaurant.inventory.persistence.entity.CatalogRecipeIngredientEntity;
import com.restaurant.inventory.persistence.entity.StockItemEntity;
import com.restaurant.inventory.persistence.repository.CatalogIngredientRepository;
import com.restaurant.inventory.persistence.repository.CatalogMenuItemRepository;
import com.restaurant.inventory.persistence.repository.CatalogRecipeIngredientRepository;
import com.restaurant.inventory.persistence.repository.StockItemRepository;
import com.restaurant.platform.eventing.contract.IngredientReservationItem;
import com.restaurant.platform.eventing.contract.OrderSubmittedToKitchenEvent;
import com.restaurant.platform.eventing.contract.StockAlertEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class InventoryProjection {

    private final StockItemRepository stockItemRepository;
    private final CatalogIngredientRepository catalogIngredientRepository;
    private final CatalogMenuItemRepository catalogMenuItemRepository;
    private final CatalogRecipeIngredientRepository catalogRecipeIngredientRepository;

    public InventoryProjection(StockItemRepository stockItemRepository,
                               CatalogIngredientRepository catalogIngredientRepository,
                               CatalogMenuItemRepository catalogMenuItemRepository,
                               CatalogRecipeIngredientRepository catalogRecipeIngredientRepository) {
        this.stockItemRepository = stockItemRepository;
        this.catalogIngredientRepository = catalogIngredientRepository;
        this.catalogMenuItemRepository = catalogMenuItemRepository;
        this.catalogRecipeIngredientRepository = catalogRecipeIngredientRepository;
    }

    public List<IngredientStockRecord> listStock(String tenantId, String propertyId) {
        return stockItemRepository.findByTenantIdAndPropertyIdOrderByIngredientNameAsc(tenantId, propertyId).stream()
                .map(this::toRecord)
                .toList();
    }

    public List<MenuAvailabilityRecord> getMenuAvailability(String tenantId, String propertyId) {
        List<CatalogMenuItemEntity> menuItems = catalogMenuItemRepository.findByTenantIdAndPropertyIdAndActiveTrueOrderByItemNameAsc(tenantId, propertyId);
        if (menuItems.isEmpty()) {
            return List.of();
        }

        Map<String, IngredientStockRecord> stockByIngredient = listStock(tenantId, propertyId).stream()
                .collect(Collectors.toMap(IngredientStockRecord::ingredientId, Function.identity()));
        Map<String, List<RecipeRequirement>> recipeByItemId = loadRecipeRequirements(tenantId, propertyId, menuItems.stream()
                .map(CatalogMenuItemEntity::getMenuItemId)
                .toList());

        List<MenuAvailabilityRecord> availability = new ArrayList<>();
        for (CatalogMenuItemEntity menuItem : menuItems) {
            List<RecipeRequirement> recipe = recipeByItemId.getOrDefault(menuItem.getMenuItemId(), List.of());
            boolean available = true;
            String reason = recipe.isEmpty() ? "Recipe is not configured yet" : "All recipe ingredients available";

            for (RecipeRequirement requirement : recipe) {
                IngredientStockRecord stock = stockByIngredient.get(requirement.ingredientId());
                if (stock == null || stock.currentQuantity() < requirement.quantity()) {
                    available = false;
                    reason = requirement.ingredientName() + " is not sufficiently stocked";
                    break;
                }
            }

            availability.add(new MenuAvailabilityRecord(
                    menuItem.getMenuItemId(),
                    tenantId,
                    propertyId,
                    available,
                    reason
            ));
        }
        return availability;
    }

    public MenuOrderValidationResponse validateMenuOrder(String tenantId, String propertyId, List<ValidateMenuOrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return new MenuOrderValidationResponse(true, List.of());
        }

        Map<String, IngredientStockRecord> workingStockByIngredient = listStock(tenantId, propertyId).stream()
                .collect(Collectors.toMap(IngredientStockRecord::ingredientId, Function.identity()));
        Map<String, List<RecipeRequirement>> recipeByItemId = loadRecipeRequirements(
                tenantId,
                propertyId,
                items.stream().map(ValidateMenuOrderItemRequest::itemId).distinct().toList()
        );

        List<MenuOrderValidationItemResponse> issues = new ArrayList<>();
        for (ValidateMenuOrderItemRequest item : items) {
            List<RecipeRequirement> recipe = recipeByItemId.getOrDefault(item.itemId(), List.of());
            if (recipe.isEmpty()) {
                issues.add(new MenuOrderValidationItemResponse(
                        item.itemId(),
                        item.itemName(),
                        item.quantity(),
                        0,
                        List.of("Recipe not configured"),
                        item.itemName() + " cannot be ordered because the recipe is not configured yet."
                ));
                continue;
            }

            int maxServableQuantity = Integer.MAX_VALUE;
            List<String> shortageIngredients = new ArrayList<>();
            boolean available = true;

            for (RecipeRequirement requirement : recipe) {
                IngredientStockRecord stock = workingStockByIngredient.get(requirement.ingredientId());
                int availableQuantity = stock != null ? stock.currentQuantity() : 0;
                int possibleQuantity = requirement.quantity() <= 0 ? item.quantity() : availableQuantity / requirement.quantity();
                maxServableQuantity = Math.min(maxServableQuantity, possibleQuantity);

                if (availableQuantity < requirement.quantity() * item.quantity()) {
                    available = false;
                    shortageIngredients.add(requirement.ingredientName());
                }
            }

            if (!available) {
                issues.add(new MenuOrderValidationItemResponse(
                        item.itemId(),
                        item.itemName(),
                        item.quantity(),
                        Math.max(0, maxServableQuantity == Integer.MAX_VALUE ? 0 : maxServableQuantity),
                        List.copyOf(shortageIngredients),
                        buildValidationMessage(item.itemName(), maxServableQuantity, shortageIngredients)
                ));
                continue;
            }

            for (RecipeRequirement requirement : recipe) {
                IngredientStockRecord stock = workingStockByIngredient.get(requirement.ingredientId());
                if (stock == null) {
                    continue;
                }
                int nextQuantity = Math.max(0, stock.currentQuantity() - requirement.quantity() * item.quantity());
                workingStockByIngredient.put(requirement.ingredientId(), new IngredientStockRecord(
                        stock.ingredientId(),
                        stock.tenantId(),
                        stock.propertyId(),
                        stock.ingredientName(),
                        nextQuantity,
                        stock.unit(),
                        stock.reorderThreshold(),
                        stock.maximumCapacity(),
                        stock.marketUnitPrice()
                ));
            }
        }

        return new MenuOrderValidationResponse(issues.isEmpty(), List.copyOf(issues));
    }

    public ReservationResult reserveForOrder(OrderSubmittedToKitchenEvent event) {
        Map<String, List<RecipeRequirement>> recipeByItemId = loadRecipeRequirements(
                event.tenantId(),
                event.propertyId(),
                event.items().stream().map(item -> item.itemId()).distinct().toList()
        );

        List<IngredientReservationItem> reserved = new ArrayList<>();
        List<StockAlertEvent> alerts = new ArrayList<>();

        for (var line : event.items()) {
            List<RecipeRequirement> recipe = recipeByItemId.getOrDefault(line.itemId(), List.of());
            for (RecipeRequirement requirement : recipe) {
                int totalQuantity = requirement.quantity() * line.quantity();
                IngredientStockRecord updated = decrementStock(
                        event.tenantId(),
                        event.propertyId(),
                        requirement.ingredientId(),
                        requirement.ingredientName(),
                        totalQuantity,
                        requirement.unit()
                );
                reserved.add(new IngredientReservationItem(
                        updated.ingredientId(),
                        updated.ingredientName(),
                        totalQuantity,
                        updated.unit()
                ));
                StockAlertEvent alert = toAlert(updated);
                if (alert != null) {
                    alerts.add(alert);
                }
            }
        }
        return new ReservationResult(event.orderId(), event.tenantId(), event.propertyId(), reserved, alerts);
    }

    public ReservationResult reserveManual(String referenceId, String tenantId, String propertyId, List<IngredientReservationItem> ingredients) {
        List<StockAlertEvent> alerts = new ArrayList<>();
        List<IngredientReservationItem> reserved = new ArrayList<>();

        for (IngredientReservationItem ingredient : ingredients) {
            IngredientStockRecord updated = decrementStock(
                    tenantId,
                    propertyId,
                    ingredient.ingredientId(),
                    ingredient.ingredientName(),
                    ingredient.quantity(),
                    ingredient.unit()
            );
            reserved.add(new IngredientReservationItem(updated.ingredientId(), updated.ingredientName(), ingredient.quantity(), updated.unit()));
            StockAlertEvent alert = toAlert(updated);
            if (alert != null) {
                alerts.add(alert);
            }
        }
        return new ReservationResult(referenceId, tenantId, propertyId, reserved, alerts);
    }

    public IngredientStockRecord upsertIngredientSettings(String tenantId,
                                                          String propertyId,
                                                          String ingredientId,
                                                          String ingredientCode,
                                                          String ingredientName,
                                                          String unit,
                                                          int reorderThreshold,
                                                          int maximumCapacity,
                                                          double marketUnitPrice,
                                                          String status) {
        StockItemEntity entity = stockItemRepository.findByTenantIdAndPropertyIdAndIngredientId(tenantId, propertyId, ingredientId)
                .orElseGet(() -> createStockItem(tenantId, propertyId, ingredientId));

        entity.setIngredientName(ingredientName.trim());
        entity.setUnitOfMeasure(unit.trim());
        entity.setReorderThreshold(BigDecimal.valueOf(reorderThreshold));
        entity.setMaximumCapacity(BigDecimal.valueOf(maximumCapacity));
        entity.setMarketUnitPrice(BigDecimal.valueOf(marketUnitPrice));
        entity.setAvailableQuantity(entity.getCurrentQuantity() == null ? BigDecimal.ZERO : entity.getCurrentQuantity());

        StockItemEntity saved = stockItemRepository.save(entity);
        syncCatalogIngredient(saved, ingredientCode, status);
        return toRecord(saved);
    }

    public IngredientStockRecord adjustStock(String tenantId,
                                             String propertyId,
                                             String ingredientId,
                                             int quantityDelta) {
        return stockItemRepository.findByTenantIdAndPropertyIdAndIngredientId(tenantId, propertyId, ingredientId)
                .map(entity -> {
                    int updatedQuantity = Math.max(0, toInteger(entity.getCurrentQuantity()) + quantityDelta);
                    BigDecimal nextQuantity = BigDecimal.valueOf(updatedQuantity);
                    entity.setCurrentQuantity(nextQuantity);
                    entity.setAvailableQuantity(nextQuantity);
                    entity.setLastStockUpdateAt(Instant.now());
                    return toRecord(stockItemRepository.save(entity));
                })
                .orElse(null);
    }

    public StockAlertEvent toAlert(IngredientStockRecord stock) {
        if (stock.currentQuantity() <= 0) {
            return new StockAlertEvent(
                    stock.tenantId(),
                    stock.propertyId(),
                    stock.ingredientId(),
                    stock.ingredientName(),
                    "OUT_OF_STOCK",
                    stock.currentQuantity(),
                    stock.unit(),
                    Instant.now()
            );
        }
        if (stock.currentQuantity() <= stock.reorderThreshold()) {
            return new StockAlertEvent(
                    stock.tenantId(),
                    stock.propertyId(),
                    stock.ingredientId(),
                    stock.ingredientName(),
                    "LOW_STOCK",
                    stock.currentQuantity(),
                    stock.unit(),
                    Instant.now()
            );
        }
        if (stock.maximumCapacity() > 0 && stock.currentQuantity() > stock.maximumCapacity()) {
            return new StockAlertEvent(
                    stock.tenantId(),
                    stock.propertyId(),
                    stock.ingredientId(),
                    stock.ingredientName(),
                    "OVER_CAPACITY",
                    stock.currentQuantity(),
                    stock.unit(),
                    Instant.now()
            );
        }
        return null;
    }

    private StockItemEntity createStockItem(String tenantId, String propertyId, String ingredientId) {
        StockItemEntity entity = new StockItemEntity();
        entity.setStockItemId("stock-" + randomId());
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setIngredientId(ingredientId);
        entity.setCurrentQuantity(BigDecimal.ZERO);
        entity.setReservedQuantity(BigDecimal.ZERO);
        entity.setAvailableQuantity(BigDecimal.ZERO);
        entity.setLastStockUpdateAt(Instant.now());
        return entity;
    }

    private IngredientStockRecord decrementStock(String tenantId,
                                                 String propertyId,
                                                 String ingredientId,
                                                 String ingredientName,
                                                 int quantity,
                                                 String unit) {
        StockItemEntity entity = stockItemRepository.findByTenantIdAndPropertyIdAndIngredientId(tenantId, propertyId, ingredientId)
                .orElseGet(() -> {
                    StockItemEntity created = createStockItem(tenantId, propertyId, ingredientId);
                    created.setIngredientName(ingredientName);
                    created.setUnitOfMeasure(unit);
                    created.setReorderThreshold(BigDecimal.valueOf(50));
                    created.setMaximumCapacity(BigDecimal.valueOf(500));
                    created.setMarketUnitPrice(BigDecimal.ZERO);
                    return created;
                });

        int updatedQuantity = Math.max(0, toInteger(entity.getCurrentQuantity()) - quantity);
        BigDecimal nextQuantity = BigDecimal.valueOf(updatedQuantity);
        entity.setCurrentQuantity(nextQuantity);
        entity.setAvailableQuantity(nextQuantity);
        entity.setLastStockUpdateAt(Instant.now());
        return toRecord(stockItemRepository.save(entity));
    }

    private Map<String, List<RecipeRequirement>> loadRecipeRequirements(String tenantId, String propertyId, Collection<String> menuItemIds) {
        if (menuItemIds.isEmpty()) {
            return Map.of();
        }

        List<CatalogRecipeIngredientEntity> recipeLinks = catalogRecipeIngredientRepository.findByMenuItemIdIn(menuItemIds);
        if (recipeLinks.isEmpty()) {
            return Map.of();
        }

        List<String> ingredientIds = recipeLinks.stream()
                .map(CatalogRecipeIngredientEntity::getIngredientId)
                .distinct()
                .toList();

        Map<String, CatalogIngredientEntity> ingredientsById = catalogIngredientRepository
                .findByTenantIdAndPropertyIdAndIngredientIdIn(tenantId, propertyId, ingredientIds).stream()
                .collect(Collectors.toMap(CatalogIngredientEntity::getIngredientId, Function.identity()));

        Map<String, List<RecipeRequirement>> recipeByItemId = new LinkedHashMap<>();
        for (CatalogRecipeIngredientEntity recipeLink : recipeLinks) {
            CatalogIngredientEntity ingredient = ingredientsById.get(recipeLink.getIngredientId());
            if (ingredient == null) {
                continue;
            }
            recipeByItemId.computeIfAbsent(recipeLink.getMenuItemId(), ignored -> new ArrayList<>())
                    .add(new RecipeRequirement(
                            recipeLink.getIngredientId(),
                            ingredient.getIngredientName(),
                            recipeLink.getQuantityRequired().intValue(),
                            ingredient.getUnitOfMeasure()
                    ));
        }
        return recipeByItemId;
    }

    private void syncCatalogIngredient(StockItemEntity stockItem, String ingredientCode, String status) {
        CatalogIngredientEntity entity = catalogIngredientRepository
                .findByTenantIdAndPropertyIdAndIngredientId(stockItem.getTenantId(), stockItem.getPropertyId(), stockItem.getIngredientId())
                .orElseGet(CatalogIngredientEntity::new);

        entity.setIngredientId(stockItem.getIngredientId());
        entity.setTenantId(stockItem.getTenantId());
        entity.setPropertyId(stockItem.getPropertyId());
        entity.setIngredientCode(ingredientCode == null || ingredientCode.isBlank() ? stockItem.getIngredientId() : ingredientCode.trim());
        entity.setIngredientName(stockItem.getIngredientName());
        entity.setUnitOfMeasure(stockItem.getUnitOfMeasure());
        entity.setActive(!"INACTIVE".equalsIgnoreCase(status));
        catalogIngredientRepository.save(entity);
    }

    private IngredientStockRecord toRecord(StockItemEntity entity) {
        return new IngredientStockRecord(
                entity.getIngredientId(),
                entity.getTenantId(),
                entity.getPropertyId(),
                entity.getIngredientName(),
                toInteger(entity.getCurrentQuantity()),
                entity.getUnitOfMeasure(),
                toInteger(entity.getReorderThreshold()),
                toInteger(entity.getMaximumCapacity()),
                entity.getMarketUnitPrice() == null ? 0 : entity.getMarketUnitPrice().doubleValue()
        );
    }

    private int toInteger(BigDecimal value) {
        return value == null ? 0 : value.intValue();
    }

    private String buildValidationMessage(String itemName, int maxServableQuantity, List<String> shortageIngredients) {
        String ingredientSummary = shortageIngredients.isEmpty()
                ? "ingredient stock is not sufficient"
                : String.join(", ", shortageIngredients) + " are low on stock";
        if (maxServableQuantity <= 0) {
            return itemName + " cannot be placed right now because " + ingredientSummary + ".";
        }
        if (maxServableQuantity == 1) {
            return "Only 1 " + itemName + " can be placed right now because " + ingredientSummary + ".";
        }
        return "Only " + maxServableQuantity + " " + itemName + " dishes can be placed right now because " + ingredientSummary + ".";
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toLowerCase(Locale.ROOT);
    }
}
