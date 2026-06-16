package com.restaurant.property;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PropertySettingsServiceTest {

    private final PropertySettingsService propertySettingsService = new PropertySettingsService();

    @Test
    void overviewContainsCoreEditableAndPlaceholderModules() {
        PropertySettingsOverviewResponse response = propertySettingsService.getOverview("bikini-bottom", "krusty-krab");

        assertThat(response.modules()).extracting(PropertySettingsModuleResponse::moduleId)
                .contains("areas-sections", "tables", "menu-recipes", "ingredients", "supplies", "taxes", "billing-templates", "bulk-import");
        assertThat(response.modules()).filteredOn(PropertySettingsModuleResponse::placeholder).singleElement()
                .extracting(PropertySettingsModuleResponse::moduleId)
                .isEqualTo("bulk-import");
        assertThat(response.importWorkbookSheets()).isNotEmpty();
    }

    @Test
    void overviewIncludesExpectedWorkbookSheetsAndEditableModulesStayNonPlaceholder() {
        PropertySettingsOverviewResponse response = propertySettingsService.getOverview("bikini-bottom", "krusty-krab");

        assertThat(response.modules())
                .filteredOn(module -> !"bulk-import".equals(module.moduleId()))
                .allMatch(module -> !module.placeholder());
        assertThat(response.importWorkbookSheets())
                .extracting(ImportWorkbookSheetResponse::sheetName)
                .containsExactly(
                        "areas_sections",
                        "tables",
                        "menu_items",
                        "recipe_ingredients",
                        "ingredients",
                        "supplies",
                        "tax_definitions",
                        "billing_templates"
                );
        ImportWorkbookSheetResponse tablesSheet = response.importWorkbookSheets().stream()
                .filter(sheet -> "tables".equals(sheet.sheetName()))
                .findFirst()
                .orElseThrow();
        assertThat(tablesSheet.requiredColumns()).contains("table_code", "display_name", "capacity", "section", "status");
    }
}
