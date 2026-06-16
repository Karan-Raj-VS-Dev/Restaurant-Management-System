package com.restaurant.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.restaurant.catalog.persistence.entity.IngredientEntity;
import com.restaurant.catalog.persistence.entity.MenuCategoryEntity;
import com.restaurant.catalog.persistence.entity.MenuItemEntity;
import com.restaurant.catalog.persistence.entity.RecipeIngredientEntity;
import com.restaurant.catalog.persistence.repository.IngredientRepository;
import com.restaurant.catalog.persistence.repository.MenuCategoryRepository;
import com.restaurant.catalog.persistence.repository.MenuItemRepository;
import com.restaurant.catalog.persistence.repository.RecipeIngredientRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private MenuCategoryRepository menuCategoryRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(
                menuItemRepository,
                menuCategoryRepository,
                ingredientRepository,
                recipeIngredientRepository
        );
    }

    @Test
    void listMenuItemsIncludesCategoryAndRecipe() {
        MenuItemEntity item = menuItem("item-001", "dish-001", "Pasta Alfredo");
        item.setCategoryId("cat-001");
        RecipeIngredientEntity recipeLink = new RecipeIngredientEntity();
        recipeLink.setRecipeIngredientId("recipe-001");
        recipeLink.setMenuItemId("item-001");
        recipeLink.setIngredientId("ing-001");
        recipeLink.setQuantityRequired(new BigDecimal("2.000"));
        recipeLink.setWastageFactor(BigDecimal.ZERO);

        when(menuItemRepository.findByTenantIdAndPropertyIdAndActiveTrueOrderByItemNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(item));
        when(menuCategoryRepository.findByTenantIdAndPropertyIdAndActiveTrueOrderByDisplayOrderAscCategoryNameAsc("bikini-bottom", "krusty-krab"))
                .thenReturn(List.of(MenuCategoryEntity.create("cat-001", "bikini-bottom", "krusty-krab", "Italian dishes", 1, true)));
        when(recipeIngredientRepository.findByMenuItemIdIn(List.of("item-001")))
                .thenReturn(List.of(recipeLink));
        when(ingredientRepository.findByTenantIdAndPropertyIdAndIngredientIdIn("bikini-bottom", "krusty-krab", List.of("ing-001")))
                .thenReturn(List.of(ingredient("ing-001", "Cream")));

        List<MenuItemResponse> response = catalogService.listMenuItems("bikini-bottom", "krusty-krab");

        assertThat(response).singleElement().satisfies(menuItem -> {
            assertThat(menuItem.categoryName()).isEqualTo("Italian dishes");
            assertThat(menuItem.recipe()).containsExactly(new RecipeIngredient("ing-001", "Cream", "2"));
        });
    }

    @Test
    void createMenuItemCreatesCategoryAndRecipeLinks() {
        MenuCategoryEntity category = MenuCategoryEntity.create("cat-001", "bikini-bottom", "krusty-krab", "Italian dishes", 1, true);
        when(menuCategoryRepository.findByTenantIdAndPropertyIdAndCategoryNameIgnoreCase("bikini-bottom", "krusty-krab", "Italian dishes"))
                .thenReturn(Optional.empty());
        when(menuCategoryRepository.save(any(MenuCategoryEntity.class))).thenReturn(category);
        when(menuCategoryRepository.findById("cat-001")).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ingredientRepository.findByTenantIdAndPropertyIdAndIngredientId("bikini-bottom", "krusty-krab", "ing-001"))
                .thenReturn(Optional.of(ingredient("ing-001", "Cream")));
        when(recipeIngredientRepository.findByMenuItemIdIn(any())).thenReturn(List.of());
        when(recipeIngredientRepository.save(any(RecipeIngredientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MenuSettingsItemResponse response = catalogService.createMenuItem(
                "bikini-bottom",
                "krusty-krab",
                new MenuSettingsItemRequest(
                        "dish-001",
                        "Pasta Alfredo",
                        "Italian dishes",
                        "Creamy pasta",
                        new BigDecimal("249"),
                        false,
                        12,
                        "ACTIVE",
                        List.of(new RecipeIngredientInput("ing-001", "Cream", "2"))
                )
        );

        ArgumentCaptor<MenuItemEntity> menuCaptor = ArgumentCaptor.forClass(MenuItemEntity.class);
        verify(menuItemRepository).save(menuCaptor.capture());
        MenuItemEntity savedMenuItem = menuCaptor.getValue();
        assertThat(savedMenuItem.getCategoryId()).startsWith("cat-");
        assertThat(savedMenuItem.getPrice()).isEqualByComparingTo("249.00");
        assertThat(response.categoryName()).isEqualTo("Italian dishes");

        ArgumentCaptor<RecipeIngredientEntity> recipeCaptor = ArgumentCaptor.forClass(RecipeIngredientEntity.class);
        verify(recipeIngredientRepository).save(recipeCaptor.capture());
        assertThat(recipeCaptor.getValue().getQuantityRequired()).isEqualByComparingTo("2.000");
    }

    @Test
    void updateMenuItemReplacesRecipeLinks() {
        MenuItemEntity existing = menuItem("item-001", "dish-001", "Pasta Alfredo");
        MenuCategoryEntity category = MenuCategoryEntity.create("cat-001", "bikini-bottom", "krusty-krab", "Italian dishes", 1, true);
        when(menuItemRepository.findByTenantIdAndPropertyIdAndMenuItemId("bikini-bottom", "krusty-krab", "item-001"))
                .thenReturn(Optional.of(existing));
        when(menuCategoryRepository.findByTenantIdAndPropertyIdAndCategoryNameIgnoreCase("bikini-bottom", "krusty-krab", "Italian dishes"))
                .thenReturn(Optional.of(category));
        when(menuCategoryRepository.findById("cat-001")).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ingredientRepository.findByTenantIdAndPropertyIdAndIngredientId("bikini-bottom", "krusty-krab", "ing-001"))
                .thenReturn(Optional.of(ingredient("ing-001", "Cream")));
        when(recipeIngredientRepository.findByMenuItemIdIn(any())).thenReturn(List.of());
        when(recipeIngredientRepository.save(any(RecipeIngredientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MenuSettingsItemResponse response = catalogService.updateMenuItem(
                "bikini-bottom",
                "krusty-krab",
                "item-001",
                new MenuSettingsItemRequest(
                        "dish-002",
                        "Pasta Alfredo Special",
                        "Italian dishes",
                        "Richer sauce",
                        new BigDecimal("299"),
                        true,
                        15,
                        "INACTIVE",
                        List.of(new RecipeIngredientInput("ing-001", "Cream", "3"))
                )
        );

        verify(recipeIngredientRepository).deleteByMenuItemId("item-001");
        assertThat(existing.getItemCode()).isEqualTo("dish-002");
        assertThat(existing.isActive()).isFalse();
        assertThat(existing.isVegetarian()).isTrue();
        assertThat(response.itemName()).isEqualTo("Pasta Alfredo Special");
    }

    @Test
    void createMenuItemRejectsMissingIngredient() {
        when(menuCategoryRepository.findByTenantIdAndPropertyIdAndCategoryNameIgnoreCase("bikini-bottom", "krusty-krab", "Italian dishes"))
                .thenReturn(Optional.of(MenuCategoryEntity.create("cat-001", "bikini-bottom", "krusty-krab", "Italian dishes", 1, true)));
        when(menuItemRepository.save(any(MenuItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ingredientRepository.findByTenantIdAndPropertyIdAndIngredientId("bikini-bottom", "krusty-krab", "missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.createMenuItem(
                "bikini-bottom",
                "krusty-krab",
                new MenuSettingsItemRequest(
                        "dish-001",
                        "Pasta Alfredo",
                        "Italian dishes",
                        null,
                        BigDecimal.TEN,
                        false,
                        10,
                        "ACTIVE",
                        List.of(new RecipeIngredientInput("missing", "Unknown", "2"))
                )
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Ingredient missing was not found");
    }

    private MenuItemEntity menuItem(String menuItemId, String itemCode, String itemName) {
        MenuItemEntity entity = new MenuItemEntity();
        entity.setMenuItemId(menuItemId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setItemCode(itemCode);
        entity.setItemName(itemName);
        entity.setDescription("Tasty");
        entity.setPrice(new BigDecimal("249.00"));
        entity.setActive(true);
        entity.setVegetarian(false);
        entity.setPrepTimeMinutes(12);
        return entity;
    }

    private IngredientEntity ingredient(String ingredientId, String ingredientName) {
        IngredientEntity entity = new TestIngredientEntity();
        entity.setIngredientId(ingredientId);
        entity.setTenantId("bikini-bottom");
        entity.setPropertyId("krusty-krab");
        entity.setIngredientCode("code-" + ingredientId);
        entity.setIngredientName(ingredientName);
        entity.setUnitOfMeasure("grams");
        entity.setActive(true);
        return entity;
    }

    private static final class TestIngredientEntity extends IngredientEntity {
    }
}
