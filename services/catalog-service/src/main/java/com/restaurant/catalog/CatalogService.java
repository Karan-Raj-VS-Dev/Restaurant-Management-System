package com.restaurant.catalog;

import com.restaurant.catalog.persistence.entity.IngredientEntity;
import com.restaurant.catalog.persistence.entity.MenuItemEntity;
import com.restaurant.catalog.persistence.entity.RecipeIngredientEntity;
import com.restaurant.catalog.persistence.repository.IngredientRepository;
import com.restaurant.catalog.persistence.repository.MenuItemRepository;
import com.restaurant.catalog.persistence.repository.RecipeIngredientRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CatalogService {

    private final MenuItemRepository menuItemRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    public CatalogService(MenuItemRepository menuItemRepository,
                          IngredientRepository ingredientRepository,
                          RecipeIngredientRepository recipeIngredientRepository) {
        this.menuItemRepository = menuItemRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    public List<MenuItemResponse> listMenuItems(String tenantId, String propertyId) {
        List<MenuItemEntity> items = menuItemRepository.findByTenantIdAndPropertyIdAndActiveTrueOrderByItemNameAsc(tenantId, propertyId);
        Map<String, List<RecipeIngredient>> recipeByItemId = buildRecipeByMenuItem(tenantId, propertyId, items.stream()
                .map(MenuItemEntity::getMenuItemId)
                .toList());

        return items.stream()
                .map(item -> new MenuItemResponse(
                        item.getMenuItemId(),
                        propertyId,
                        item.getItemName(),
                        item.getPrice(),
                        item.isActive(),
                        recipeByItemId.getOrDefault(item.getMenuItemId(), List.of())
                ))
                .toList();
    }

    public MenuSettingsSummaryResponse getSettingsSummary(String tenantId, String propertyId) {
        List<MenuItemEntity> items = menuItemRepository.findByTenantIdAndPropertyIdOrderByItemNameAsc(tenantId, propertyId);
        Map<String, List<RecipeIngredient>> recipeByItemId = buildRecipeByMenuItem(tenantId, propertyId, items.stream()
                .map(MenuItemEntity::getMenuItemId)
                .toList());

        return new MenuSettingsSummaryResponse(
                (int) items.stream().filter(MenuItemEntity::isActive).count(),
                recipeByItemId.values().stream().mapToInt(List::size).sum(),
                List.of("item code", "dish name", "price", "recipe ingredients", "status"),
                items.stream()
                        .map(item -> new MenuSettingsItemResponse(
                                item.getMenuItemId(),
                                item.getItemCode(),
                                item.getItemName(),
                                item.getDescription(),
                                item.getPrice(),
                                recipeByItemId.getOrDefault(item.getMenuItemId(), List.of()).size(),
                                item.isActive(),
                                item.isVegetarian(),
                                item.getPrepTimeMinutes(),
                                recipeByItemId.getOrDefault(item.getMenuItemId(), List.of())
                        ))
                        .toList()
        );
    }

    public MenuSettingsItemResponse createMenuItem(String tenantId, String propertyId, MenuSettingsItemRequest request) {
        MenuItemEntity entity = new MenuItemEntity();
        entity.setMenuItemId("item-" + randomId());
        entity.setTenantId(tenantId);
        entity.setPropertyId(propertyId);
        entity.setCategoryId(null);
        applyMenuValues(entity, request);
        MenuItemEntity saved = menuItemRepository.save(entity);
        saveRecipe(tenantId, propertyId, saved.getMenuItemId(), request.recipe());
        return buildSettingsResponse(tenantId, propertyId, saved);
    }

    public MenuSettingsItemResponse updateMenuItem(String tenantId, String propertyId, String menuItemId, MenuSettingsItemRequest request) {
        MenuItemEntity entity = menuItemRepository.findByTenantIdAndPropertyIdAndMenuItemId(tenantId, propertyId, menuItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish was not found"));

        applyMenuValues(entity, request);
        MenuItemEntity saved = menuItemRepository.save(entity);
        recipeIngredientRepository.deleteByMenuItemId(saved.getMenuItemId());
        saveRecipe(tenantId, propertyId, saved.getMenuItemId(), request.recipe());
        return buildSettingsResponse(tenantId, propertyId, saved);
    }

    private void applyMenuValues(MenuItemEntity entity, MenuSettingsItemRequest request) {
        entity.setItemCode(normalizeCode(request.itemCode()));
        entity.setItemName(requireValue(request.itemName(), "Dish name"));
        entity.setDescription(blankToNull(request.description()));
        entity.setPrice(normalizeMoney(request.price()));
        entity.setVegetarian(request.vegetarian());
        entity.setActive(!"INACTIVE".equalsIgnoreCase(request.status()));
        entity.setPrepTimeMinutes(Math.max(0, request.prepTimeMinutes()));
    }

    private void saveRecipe(String tenantId, String propertyId, String menuItemId, List<RecipeIngredientInput> recipeInputs) {
        if (recipeInputs == null || recipeInputs.isEmpty()) {
            return;
        }

        for (RecipeIngredientInput recipeInput : recipeInputs) {
            if (recipeInput.ingredientId() == null || recipeInput.ingredientId().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select an ingredient for each recipe row.");
            }

            IngredientEntity ingredient = ingredientRepository
                    .findByTenantIdAndPropertyIdAndIngredientId(tenantId, propertyId, recipeInput.ingredientId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingredient " + recipeInput.ingredientId() + " was not found"));

            RecipeIngredientEntity entity = new RecipeIngredientEntity();
            entity.setRecipeIngredientId("recipe-" + randomId());
            entity.setMenuItemId(menuItemId);
            entity.setIngredientId(ingredient.getIngredientId());
            entity.setQuantityRequired(parseQuantity(recipeInput.quantity(), ingredient.getIngredientName()));
            entity.setWastageFactor(BigDecimal.ZERO);
            recipeIngredientRepository.save(entity);
        }
    }

    private MenuSettingsItemResponse buildSettingsResponse(String tenantId, String propertyId, MenuItemEntity entity) {
        List<RecipeIngredient> recipe = buildRecipeByMenuItem(tenantId, propertyId, List.of(entity.getMenuItemId()))
                .getOrDefault(entity.getMenuItemId(), List.of());
        return new MenuSettingsItemResponse(
                entity.getMenuItemId(),
                entity.getItemCode(),
                entity.getItemName(),
                entity.getDescription(),
                entity.getPrice(),
                recipe.size(),
                entity.isActive(),
                entity.isVegetarian(),
                entity.getPrepTimeMinutes(),
                recipe
        );
    }

    private Map<String, List<RecipeIngredient>> buildRecipeByMenuItem(String tenantId, String propertyId, Collection<String> menuItemIds) {
        if (menuItemIds.isEmpty()) {
            return Map.of();
        }

        List<RecipeIngredientEntity> recipeLinks = recipeIngredientRepository.findByMenuItemIdIn(menuItemIds);
        if (recipeLinks.isEmpty()) {
            return Map.of();
        }

        List<String> ingredientIds = recipeLinks.stream()
                .map(RecipeIngredientEntity::getIngredientId)
                .distinct()
                .toList();
        Map<String, IngredientEntity> ingredientById = ingredientRepository
                .findByTenantIdAndPropertyIdAndIngredientIdIn(tenantId, propertyId, ingredientIds).stream()
                .collect(Collectors.toMap(IngredientEntity::getIngredientId, Function.identity()));

        return recipeLinks.stream().collect(Collectors.groupingBy(
                RecipeIngredientEntity::getMenuItemId,
                Collectors.mapping(link -> toRecipeIngredient(link, ingredientById.get(link.getIngredientId())), Collectors.toList())
        ));
    }

    private RecipeIngredient toRecipeIngredient(RecipeIngredientEntity link, IngredientEntity ingredient) {
        if (ingredient == null) {
            return new RecipeIngredient(link.getIngredientId(), "Unknown ingredient", formatQuantity(link.getQuantityRequired()));
        }
        return new RecipeIngredient(
                ingredient.getIngredientId(),
                ingredient.getIngredientName(),
                formatQuantity(link.getQuantityRequired())
        );
    }

    private BigDecimal parseQuantity(String quantityText, String ingredientName) {
        if (quantityText == null || quantityText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a quantity for " + ingredientName + ".");
        }
        try {
            BigDecimal quantity = new BigDecimal(quantityText.trim());
            if (quantity.signum() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity for " + ingredientName + " must be greater than zero.");
            }
            return quantity.setScale(3, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid quantity for " + ingredientName + ".");
        }
    }

    private String formatQuantity(BigDecimal quantity) {
        return quantity.stripTrailingZeros().toPlainString();
    }

    private BigDecimal normalizeMoney(BigDecimal price) {
        if (price == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (price.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price cannot be negative");
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item code is required");
        }
        return value.trim();
    }

    private String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toLowerCase(Locale.ROOT);
    }
}
