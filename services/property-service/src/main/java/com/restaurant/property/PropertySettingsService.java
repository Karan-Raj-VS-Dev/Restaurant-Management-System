package com.restaurant.property;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PropertySettingsService {

    public PropertySettingsOverviewResponse getOverview(String tenantId, String propertyId) {
        return new PropertySettingsOverviewResponse(
                tenantId,
                propertyId,
                List.of(
                        new PropertySettingsModuleResponse(
                                "areas-sections",
                                "Property areas and sections",
                                "Define floors and sections, set how many tables fit there, and note the waiters and cleaners assigned to each zone.",
                                "property-service",
                                List.of("floor", "section", "table capacity", "waiters", "cleaners"),
                                false
                        ),
                        new PropertySettingsModuleResponse(
                                "tables",
                                "Tables",
                                "Create and edit the physical table map, capacities, sections, and occupancy-facing setup for the property.",
                                "table-service",
                                List.of("capacity", "section", "display name", "service status"),
                                false
                        ),
                        new PropertySettingsModuleResponse(
                                "menu-recipes",
                                "Food and recipes",
                                "Create dishes, map recipes, set selling prices, and evaluate ingredient-driven profitability.",
                                "catalog-service",
                                List.of("dish codes", "recipe mapping", "price", "ingredient cost"),
                                false
                        ),
                        new PropertySettingsModuleResponse(
                                "ingredients",
                                "Ingredients",
                                "Maintain the food-side ingredients that recipe definitions and stock reservations rely on.",
                                "inventory-service",
                                List.of("ingredient code", "unit of measure", "stock visibility", "market price"),
                                false
                        ),
                        new PropertySettingsModuleResponse(
                                "supplies",
                                "Operational supplies",
                                "Track non-food property items such as cleaning tools, disposables, and other day-to-day support material.",
                                "inventory-service",
                                List.of("supply item", "reorder level", "unit", "market price"),
                                false
                        ),
                        new PropertySettingsModuleResponse(
                                "taxes",
                                "Tax settings",
                                "Define reusable taxes once so billing can apply them consistently at the final bill stage.",
                                "billing-service",
                                List.of("tax_id", "rate_percent", "scope", "status"),
                                false
                        ),
                        new PropertySettingsModuleResponse(
                                "billing-templates",
                                "Billing templates",
                                "Manage the future receipt and bill layouts that can be reused across dine-in and takeaway channels.",
                                "billing-service",
                                List.of("template name", "header/footer", "channel", "status"),
                                false
                        ),
                        new PropertySettingsModuleResponse(
                                "bulk-import",
                                "Excel bulk import",
                                "Upload a single workbook later to seed dishes, tables, ingredients, supplies, taxes, billing templates, and area/section mappings together.",
                                "property-service",
                                List.of("sheet validation", "preview", "error rows", "idempotent import"),
                                true
                        )
                ),
                List.of(
                        new ImportWorkbookSheetResponse(
                                "areas_sections",
                                "Seed floor and section definitions along with capacity and assigned employee lists.",
                                List.of("floor_name", "section_name", "max_table_count", "waiter_names", "cleaner_names", "status")
                        ),
                        new ImportWorkbookSheetResponse(
                                "tables",
                                "Create or update table definitions for the property.",
                                List.of("table_code", "display_name", "capacity", "section", "status")
                        ),
                        new ImportWorkbookSheetResponse(
                                "menu_items",
                                "Create or update dishes and seed their selling price, prep time, and dietary markers.",
                                List.of("item_code", "item_name", "price", "is_vegetarian", "prep_time_minutes", "status")
                        ),
                        new ImportWorkbookSheetResponse(
                                "recipe_ingredients",
                                "Map dishes to ingredient quantities used during preparation.",
                                List.of("item_code", "ingredient_code", "quantity_required", "wastage_factor")
                        ),
                        new ImportWorkbookSheetResponse(
                                "ingredients",
                                "Seed the ingredient master list used by recipes and stock.",
                                List.of("ingredient_code", "ingredient_name", "unit_of_measure", "market_price", "status")
                        ),
                        new ImportWorkbookSheetResponse(
                                "supplies",
                                "Seed non-food operational items such as cleaning and service supplies.",
                                List.of("supply_code", "supply_name", "unit", "reorder_level", "market_price", "status")
                        ),
                        new ImportWorkbookSheetResponse(
                                "tax_definitions",
                                "Seed the shared tax master that final bill generation can apply consistently.",
                                List.of("tax_id", "tax_name", "rate_percent", "applies_to", "status")
                        ),
                        new ImportWorkbookSheetResponse(
                                "billing_templates",
                                "Seed future bill and receipt layout placeholders.",
                                List.of("template_code", "template_name", "channel", "header_text", "footer_text", "status")
                        )
                )
        );
    }
}
