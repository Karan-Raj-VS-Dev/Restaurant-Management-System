package com.restaurant.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    @Mock
    private CatalogService catalogService;

    private CatalogController controller;

    @BeforeEach
    void setUp() {
        controller = new CatalogController(catalogService);
    }

    @Test
    void listMenuItemsPrefersPathScopedTenantAndProperty() {
        List<MenuItemResponse> expected = List.of(menuItemResponse());
        when(catalogService.listMenuItems("tenant-path", "property-path")).thenReturn(expected);

        List<MenuItemResponse> response = controller.listMenuItems(
                "tenant-path",
                "property-path",
                "tenant-query",
                "property-query"
        );

        assertThat(response).isEqualTo(expected);
        verify(catalogService).listMenuItems("tenant-path", "property-path");
    }

    @Test
    void settingsAndMutationsDelegateUsingDefaultsWhenScopeMissing() {
        MenuSettingsSummaryResponse summary = new MenuSettingsSummaryResponse(1, 2, List.of("dish name"), List.of(menuSettingsResponse()));
        MenuSettingsItemRequest request = new MenuSettingsItemRequest(
                "dish-001",
                "Pasta Alfredo",
                "Italian dishes",
                "Creamy pasta",
                new BigDecimal("249.00"),
                false,
                12,
                "ACTIVE",
                List.of(new RecipeIngredientInput("ing-001", "Cream", "2"))
        );
        when(catalogService.getSettingsSummary("bikini-bottom", "krusty-krab")).thenReturn(summary);
        when(catalogService.createMenuItem("bikini-bottom", "krusty-krab", request)).thenReturn(menuSettingsResponse());
        when(catalogService.updateMenuItem("bikini-bottom", "krusty-krab", "item-001", request)).thenReturn(menuSettingsResponse());

        assertThat(controller.getSettingsSummary(null, null, null, null)).isEqualTo(summary);
        assertThat(controller.createMenuItem(null, null, null, null, request)).isEqualTo(menuSettingsResponse());
        assertThat(controller.updateMenuItem("item-001", null, null, null, null, request)).isEqualTo(menuSettingsResponse());

        verify(catalogService).getSettingsSummary("bikini-bottom", "krusty-krab");
        verify(catalogService).createMenuItem("bikini-bottom", "krusty-krab", request);
        verify(catalogService).updateMenuItem("bikini-bottom", "krusty-krab", "item-001", request);
    }

    private MenuItemResponse menuItemResponse() {
        return new MenuItemResponse(
                "item-001",
                "krusty-krab",
                "Pasta Alfredo",
                "cat-001",
                "Italian dishes",
                new BigDecimal("249.00"),
                true,
                List.of(new RecipeIngredient("ing-001", "Cream", "2"))
        );
    }

    private MenuSettingsItemResponse menuSettingsResponse() {
        return new MenuSettingsItemResponse(
                "item-001",
                "dish-001",
                "Pasta Alfredo",
                "cat-001",
                "Italian dishes",
                "Creamy pasta",
                new BigDecimal("249.00"),
                1,
                true,
                false,
                12,
                List.of(new RecipeIngredient("ing-001", "Cream", "2"))
        );
    }
}
