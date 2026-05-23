import {
  createAreaSectionSetting,
  createBillingTemplate,
  createIngredientSetting,
  createMenuSetting,
  createSupplySetting,
  createTableSetting,
  createTaxSetting,
  loadAreaSectionSettingsSummary,
  loadBillingSettingsSummary,
  loadInventorySettingsSummary,
  loadMenuSettingsSummary,
  loadPropertyEmployees,
  loadPropertySettingsOverview,
  loadTableSettingsSummary,
  updateAreaSectionSetting,
  updateBillingTemplate,
  updateIngredientSetting,
  updateMenuSetting,
  updateSupplySetting,
  updateTableSetting,
  updateTaxSetting,
  type AreaSectionSettingPayload,
  type AreaSectionSettingsSummary,
  type BillingSettingsSummary,
  type BillingTemplatePayload,
  type EmployeeRecord,
  type IngredientSettingPayload,
  type InventorySettingsSummary,
  type MenuSettingPayload,
  type MenuSettingsSummary,
  type PropertyRecord,
  type PropertySettingsOverview,
  type RecipeIngredientPayload,
  type SupplySettingPayload,
  type TableSettingPayload,
  type TableSettingsSummary,
  type TaxSettingPayload
} from "@restaurant/api";
import { Button, SectionCard, StatusPill } from "@restaurant/ui";
import { useEffect, useMemo, useState, type Dispatch, type SetStateAction } from "react";

interface PropertySettingsPageProps {
  selectedProperty: PropertyRecord;
}

type ModuleId =
  | "areas-sections"
  | "tables"
  | "menu-recipes"
  | "ingredients"
  | "supplies"
  | "taxes"
  | "billing-templates"
  | "bulk-import";

type EditorState = {
  moduleId: ModuleId;
  mode: "create" | "edit";
  recordId?: string;
} | null;

type AreaSectionFormState = {
  floorName: string;
  sectionName: string;
  maxTableCount: number;
  waiterNames: string[];
  cleanerNames: string[];
  status: string;
};

const defaultAreaSectionForm: AreaSectionFormState = {
  floorName: "",
  sectionName: "",
  maxTableCount: 0,
  waiterNames: [],
  cleanerNames: [],
  status: "ACTIVE"
};

const defaultTableForm: TableSettingPayload = {
  tableNumber: "",
  displayName: "",
  floorName: "",
  sectionName: "",
  capacity: 4,
  status: "AVAILABLE",
  active: true
};

const defaultMenuForm: MenuSettingPayload = {
  itemCode: "",
  itemName: "",
  categoryName: "",
  description: "",
  price: 0,
  vegetarian: false,
  prepTimeMinutes: 10,
  status: "ACTIVE",
  recipe: []
};

const defaultIngredientForm: IngredientSettingPayload = {
  ingredientCode: "",
  ingredientName: "",
  unit: "",
  reorderThreshold: 0,
  maximumCapacity: 0,
  marketPrice: 0,
  status: "ACTIVE"
};

const ingredientUnitChoices = [
  "grams",
  "kg",
  "ml",
  "liters",
  "pieces",
  "packets",
  "bottles",
  "boxes",
  "cans",
  "scoops",
  "portions"
] as const;

const defaultSupplyForm: SupplySettingPayload = {
  supplyCode: "",
  supplyName: "",
  unit: "",
  reorderLevel: 0,
  marketPrice: 0,
  status: "ACTIVE"
};

const defaultTaxForm: TaxSettingPayload = {
  taxId: "",
  taxName: "",
  ratePercent: 0,
  appliesTo: "FINAL_BILL",
  status: "ACTIVE"
};

const defaultTemplateDescription = {
  summary: "",
  channel: "DINE_IN",
  headerText: "",
  footerText: "",
  sections: ["header", "line_items", "taxes", "totals", "footer"]
};

const defaultTemplateForm: BillingTemplatePayload = {
  templateId: "",
  templateName: "",
  description: defaultTemplateDescription,
  status: "ACTIVE"
};

export function PropertySettingsPage(props: PropertySettingsPageProps) {
  const [selectedModuleId, setSelectedModuleId] = useState<ModuleId>("areas-sections");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [overview, setOverview] = useState<PropertySettingsOverview | null>(null);
  const [areaSectionSummary, setAreaSectionSummary] = useState<AreaSectionSettingsSummary | null>(null);
  const [tableSummary, setTableSummary] = useState<TableSettingsSummary | null>(null);
  const [menuSummary, setMenuSummary] = useState<MenuSettingsSummary | null>(null);
  const [inventorySummary, setInventorySummary] = useState<InventorySettingsSummary | null>(null);
  const [billingSummary, setBillingSummary] = useState<BillingSettingsSummary | null>(null);
  const [propertyEmployees, setPropertyEmployees] = useState<EmployeeRecord[]>([]);
  const [editor, setEditor] = useState<EditorState>(null);
  const [waiterPickerValue, setWaiterPickerValue] = useState("");
  const [cleanerPickerValue, setCleanerPickerValue] = useState("");

  const [areaSectionForm, setAreaSectionForm] = useState<AreaSectionFormState>(defaultAreaSectionForm);
  const [tableForm, setTableForm] = useState<TableSettingPayload>(defaultTableForm);
  const [menuForm, setMenuForm] = useState<MenuSettingPayload>(defaultMenuForm);
  const [ingredientForm, setIngredientForm] = useState<IngredientSettingPayload>(defaultIngredientForm);
  const [supplyForm, setSupplyForm] = useState<SupplySettingPayload>(defaultSupplyForm);
  const [taxForm, setTaxForm] = useState<TaxSettingPayload>(defaultTaxForm);
  const [templateForm, setTemplateForm] = useState<BillingTemplatePayload>(defaultTemplateForm);
  const [templateJsonText, setTemplateJsonText] = useState(JSON.stringify(defaultTemplateDescription, null, 2));

  const refresh = async () => {
    setLoading(true);
    setError(null);
    try {
      const [nextOverview, nextAreaSections, nextTables, nextMenu, nextInventory, nextBilling, nextEmployees] = await Promise.all([
        loadPropertySettingsOverview(),
        loadAreaSectionSettingsSummary(),
        loadTableSettingsSummary(),
        loadMenuSettingsSummary(),
        loadInventorySettingsSummary(),
        loadBillingSettingsSummary(),
        loadPropertyEmployees()
      ]);
      setOverview(nextOverview);
      setAreaSectionSummary(nextAreaSections);
      setTableSummary(nextTables);
      setMenuSummary(nextMenu);
      setInventorySummary(nextInventory);
      setBillingSummary(nextBilling);
      setPropertyEmployees(nextEmployees);
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Unable to load property settings.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refresh();
  }, [props.selectedProperty.propertyId]);

  const modules = overview?.modules ?? [];
  const selectedModule = modules.find((module) => module.moduleId === selectedModuleId) ?? null;
  const importSheets = overview?.importWorkbookSheets ?? [];
  const editorOpen = editor?.moduleId === selectedModuleId;

  const ingredientOptions = inventorySummary?.ingredients ?? [];
  const uniqueFloors = useMemo(
    () => Array.from(new Set((areaSectionSummary?.records ?? []).map((record) => record.floorName))).sort((left, right) => left.localeCompare(right)),
    [areaSectionSummary]
  );
  const availableSections = useMemo(
    () => (areaSectionSummary?.records ?? [])
      .filter((record) => !tableForm.floorName || record.floorName === tableForm.floorName)
      .map((record) => record.sectionName),
    [areaSectionSummary, tableForm.floorName]
  );
  const selectedAreaSection = useMemo(
    () => (areaSectionSummary?.records ?? []).find((record) => record.floorName === tableForm.floorName && record.sectionName === tableForm.sectionName) ?? null,
    [areaSectionSummary, tableForm.floorName, tableForm.sectionName]
  );
  const ingredientUnitOptions = useMemo(() => {
    const currentUnit = ingredientForm.unit.trim();
    if (!currentUnit || ingredientUnitChoices.includes(currentUnit as (typeof ingredientUnitChoices)[number])) {
      return ingredientUnitChoices;
    }
    return [...ingredientUnitChoices, currentUnit];
  }, [ingredientForm.unit]);

  const ingredientCost = useMemo(
    () => calculateIngredientCost(menuForm.recipe, ingredientOptions),
    [menuForm.recipe, ingredientOptions]
  );
  const suggestedProfitAmount = ingredientCost > 0 ? ingredientCost * 1.2 : null;
  const waiterOptions = useMemo(
    () => propertyEmployees
      .filter((employee) => employee.role === "WAITER")
      .sort((left, right) => left.name.localeCompare(right.name)),
    [propertyEmployees]
  );
  const cleanerOptions = useMemo(
    () => propertyEmployees
      .filter((employee) => employee.role === "CLEANER")
      .sort((left, right) => left.name.localeCompare(right.name)),
    [propertyEmployees]
  );

  const openCreate = (moduleId: ModuleId) => {
    setError(null);
    setSuccess(null);
    setEditor({ moduleId, mode: "create" });
    setWaiterPickerValue("");
    setCleanerPickerValue("");

    if (moduleId === "areas-sections") {
      setAreaSectionForm(defaultAreaSectionForm);
    }
    if (moduleId === "tables") {
      setTableForm(defaultTableForm);
    }
    if (moduleId === "menu-recipes") {
      setMenuForm(defaultMenuForm);
    }
    if (moduleId === "ingredients") {
      setIngredientForm(defaultIngredientForm);
    }
    if (moduleId === "supplies") {
      setSupplyForm(defaultSupplyForm);
    }
    if (moduleId === "taxes") {
      setTaxForm(defaultTaxForm);
    }
    if (moduleId === "billing-templates") {
      setTemplateForm(defaultTemplateForm);
      setTemplateJsonText(JSON.stringify(defaultTemplateDescription, null, 2));
    }
  };

  const openEdit = (moduleId: ModuleId, recordId: string) => {
    setError(null);
    setSuccess(null);
    setEditor({ moduleId, mode: "edit", recordId });

    if (moduleId === "areas-sections") {
      const item = areaSectionSummary?.records.find((record) => record.areaSectionId === recordId);
      if (item) {
        setAreaSectionForm({
          floorName: item.floorName,
          sectionName: item.sectionName,
          maxTableCount: item.maxTableCount,
          waiterNames: item.waiterNames,
          cleanerNames: item.cleanerNames,
          status: item.status
        });
      }
    }

    if (moduleId === "tables") {
      const item = tableSummary?.tables.find((table) => table.tableId === recordId);
      if (item) {
        setTableForm({
          tableNumber: item.tableNumber,
          displayName: item.displayName,
          floorName: item.floorName,
          sectionName: item.sectionName,
          capacity: item.capacity,
          status: item.status,
          active: item.active
        });
      }
    }

    if (moduleId === "menu-recipes") {
      const item = menuSummary?.items.find((menuItem) => menuItem.menuItemId === recordId);
      if (item) {
        setMenuForm({
          itemCode: item.itemCode,
          itemName: item.itemName,
          categoryName: item.categoryName ?? "",
          description: item.description,
          price: item.price,
          vegetarian: item.vegetarian,
          prepTimeMinutes: item.prepTimeMinutes,
          status: item.active ? "ACTIVE" : "INACTIVE",
          recipe: item.recipe.map((recipeLine) => ({
            ingredientId: recipeLine.ingredientId,
            ingredientName: recipeLine.name,
            quantity: recipeLine.quantity
          }))
        });
      }
    }

    if (moduleId === "ingredients") {
      const item = inventorySummary?.ingredients.find((ingredient) => ingredient.ingredientId === recordId);
      if (item) {
        setIngredientForm({
          ingredientCode: item.ingredientCode,
          ingredientName: item.ingredientName,
          unit: item.unit,
          reorderThreshold: item.reorderThreshold,
          maximumCapacity: item.maximumCapacity,
          marketPrice: item.marketPrice,
          status: item.status
        });
      }
    }

    if (moduleId === "supplies") {
      const item = inventorySummary?.supplies.find((supply) => supply.supplyId === recordId);
      if (item) {
        setSupplyForm({
          supplyCode: item.supplyCode,
          supplyName: item.supplyName,
          unit: item.unit,
          reorderLevel: item.reorderLevel,
          marketPrice: item.marketPrice,
          status: item.status
        });
      }
    }

    if (moduleId === "taxes") {
      const item = billingSummary?.taxes.find((tax) => tax.taxId === recordId);
      if (item) {
        setTaxForm({
          taxId: item.taxId,
          taxName: item.taxName,
          ratePercent: item.ratePercent,
          appliesTo: item.appliesTo,
          status: item.status
        });
      }
    }

    if (moduleId === "billing-templates") {
      const item = billingSummary?.templates.find((template) => template.templateId === recordId);
      if (item) {
        setTemplateForm({
          templateId: item.templateId,
          templateName: item.templateName,
          description: item.description,
          status: item.status
        });
        setTemplateJsonText(JSON.stringify(item.description, null, 2));
      }
    }
  };

  const closeEditor = () => {
    setEditor(null);
    setError(null);
    setSuccess(null);
  };

  const saveCurrentModule = async () => {
    if (!editor) {
      return;
    }

    setError(null);
    setSuccess(null);

    try {
      if (editor.moduleId === "areas-sections") {
        const payload: AreaSectionSettingPayload = {
          floorName: areaSectionForm.floorName,
          sectionName: areaSectionForm.sectionName,
          maxTableCount: areaSectionForm.maxTableCount,
          waiterNames: areaSectionForm.waiterNames,
          cleanerNames: areaSectionForm.cleanerNames,
          status: areaSectionForm.status
        };
        if (editor.mode === "create") {
          await createAreaSectionSetting(payload);
          setSuccess("Area and section saved successfully.");
        } else {
          await updateAreaSectionSetting(editor.recordId!, payload);
          setSuccess("Area and section updated successfully.");
        }
      }

      if (editor.moduleId === "tables") {
        if (editor.mode === "create") {
          await createTableSetting(tableForm);
          setSuccess("Table saved successfully.");
        } else {
          await updateTableSetting(editor.recordId!, tableForm);
          setSuccess("Table updated successfully.");
        }
      }

      if (editor.moduleId === "menu-recipes") {
        if (editor.mode === "create") {
          await createMenuSetting(menuForm);
          setSuccess("Dish and recipe saved successfully.");
        } else {
          await updateMenuSetting(editor.recordId!, menuForm);
          setSuccess("Dish and recipe updated successfully.");
        }
      }

      if (editor.moduleId === "ingredients") {
        if (editor.mode === "create") {
          await createIngredientSetting(ingredientForm);
          setSuccess("Ingredient saved successfully.");
        } else {
          await updateIngredientSetting(editor.recordId!, ingredientForm);
          setSuccess("Ingredient updated successfully.");
        }
      }

      if (editor.moduleId === "supplies") {
        if (editor.mode === "create") {
          await createSupplySetting(supplyForm);
          setSuccess("Operational supply saved successfully.");
        } else {
          await updateSupplySetting(editor.recordId!, supplyForm);
          setSuccess("Operational supply updated successfully.");
        }
      }

      if (editor.moduleId === "taxes") {
        if (editor.mode === "create") {
          await createTaxSetting(taxForm);
          setSuccess("Tax setting saved successfully.");
        } else {
          await updateTaxSetting(editor.recordId!, taxForm);
          setSuccess("Tax setting updated successfully.");
        }
      }

      if (editor.moduleId === "billing-templates") {
        const payload = { ...templateForm, description: JSON.parse(templateJsonText) as Record<string, unknown> };
        if (editor.mode === "create") {
          await createBillingTemplate(payload);
          setSuccess("Billing template saved successfully.");
        } else {
          await updateBillingTemplate(editor.recordId!, payload);
          setSuccess("Billing template updated successfully.");
        }
      }

      await refresh();
      closeEditor();
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Unable to save the setting.");
    }
  };

  const moduleActionLabel = useMemo(() => {
    switch (selectedModuleId) {
      case "areas-sections":
        return "Add area / section";
      case "tables":
        return "Add table";
      case "menu-recipes":
        return "Add dish";
      case "ingredients":
        return "Add ingredient";
      case "supplies":
        return "Add supply";
      case "taxes":
        return "Add tax";
      case "billing-templates":
        return "Add template";
      default:
        return null;
    }
  }, [selectedModuleId]);

  return (
    <SectionCard
      title="Property settings"
      subtitle={`Use this workspace to add and edit operational settings for ${props.selectedProperty.name}. Excel import stays as a future placeholder, while the rest of the modules are editable here.`}
      action={
        <Button variant="ghost" onClick={() => void refresh()}>
          Refresh
        </Button>
      }
    >
      {error ? <div className="admin-alert admin-alert-error">{error}</div> : null}
      {success ? <div className="admin-alert admin-alert-success">{success}</div> : null}

      <div className="property-settings-layout">
        <aside className="property-settings-nav">
          {modules.map((module) => (
            <button
              key={module.moduleId}
              type="button"
              className={`property-settings-nav-item ${selectedModuleId === module.moduleId ? "is-active" : ""}`}
              onClick={() => {
                setSelectedModuleId(module.moduleId as ModuleId);
                setEditor(null);
                setSuccess(null);
                setError(null);
              }}
            >
              <strong>{module.title}</strong>
              <span>{module.ownerService}</span>
            </button>
          ))}
        </aside>

        <div className="property-settings-detail">
          {loading && !overview ? <p className="admin-inline-note">Loading property settings...</p> : null}
          {selectedModule ? (
            <SectionCard
              title={selectedModule.title}
              subtitle={selectedModule.description}
              action={
                selectedModuleId !== "bulk-import" && moduleActionLabel ? (
                  <Button variant="primary" onClick={() => openCreate(selectedModuleId)}>
                    {moduleActionLabel}
                  </Button>
                ) : (
                  <StatusPill tone="warning">Placeholder only</StatusPill>
                )
              }
            >
              <div className={`property-settings-editor-layout ${editorOpen ? "has-editor" : ""}`}>
                <div className="property-settings-data-pane">
                  {selectedModuleId === "areas-sections" && areaSectionSummary ? (
                    <div className="admin-table-wrapper">
                      <table className="admin-table">
                        <thead>
                          <tr>
                            <th>Floor</th>
                            <th>Section</th>
                            <th>Max tables</th>
                            <th>Waiters</th>
                            <th>Cleaners</th>
                            <th>Status</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {areaSectionSummary.records.map((record) => (
                            <tr key={record.areaSectionId}>
                              <td>{record.floorName}</td>
                              <td>{record.sectionName}</td>
                              <td>{record.maxTableCount}</td>
                              <td>{record.waiterNames.join(", ") || "Unassigned"}</td>
                              <td>{record.cleanerNames.join(", ") || "Unassigned"}</td>
                              <td><StatusPill tone={record.status === "ACTIVE" ? "success" : "warning"}>{record.status}</StatusPill></td>
                              <td><Button variant="ghost" onClick={() => openEdit("areas-sections", record.areaSectionId)}>Edit</Button></td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : null}

                  {selectedModuleId === "tables" && tableSummary ? (
                    <div className="admin-table-wrapper">
                      <table className="admin-table">
                        <thead>
                          <tr>
                            <th>Code</th>
                            <th>Name</th>
                            <th>Floor</th>
                            <th>Section</th>
                            <th>Capacity</th>
                            <th>Status</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {tableSummary.tables.map((table) => (
                            <tr key={table.tableId}>
                              <td>{table.tableNumber}</td>
                              <td>{table.displayName}</td>
                              <td>{table.floorName}</td>
                              <td>{table.sectionName}</td>
                              <td>{table.capacity}</td>
                              <td><StatusPill tone={table.active ? "success" : "warning"}>{table.status}</StatusPill></td>
                              <td><Button variant="ghost" onClick={() => openEdit("tables", table.tableId)}>Edit</Button></td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : null}

                  {selectedModuleId === "menu-recipes" && menuSummary ? (
                    <div className="admin-table-wrapper">
                      <table className="admin-table">
                        <thead>
                          <tr>
                            <th>Code</th>
                            <th>Dish</th>
                            <th>Category</th>
                            <th>Price</th>
                            <th>Ingredients cost</th>
                            <th>Prep</th>
                            <th>Status</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {menuSummary.items.map((item) => {
                            const itemCost = calculateIngredientCost(item.recipe, ingredientOptions);
                            return (
                              <tr key={item.menuItemId}>
                                <td>{item.itemCode}</td>
                                <td>{item.itemName}</td>
                                <td>{item.categoryName || "Uncategorized"}</td>
                                <td>Rs {item.price}</td>
                                <td>Rs {formatMoney(itemCost)}</td>
                                <td>{item.prepTimeMinutes} min</td>
                                <td><StatusPill tone={item.active ? "success" : "warning"}>{item.active ? "ACTIVE" : "INACTIVE"}</StatusPill></td>
                                <td><Button variant="ghost" onClick={() => openEdit("menu-recipes", item.menuItemId)}>Edit</Button></td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  ) : null}

                  {selectedModuleId === "ingredients" && inventorySummary ? (
                    <div className="admin-table-wrapper">
                      <table className="admin-table">
                        <thead>
                          <tr>
                            <th>Code</th>
                            <th>Name</th>
                            <th>Unit</th>
                            <th>Reorder threshold</th>
                            <th>Maximum capacity</th>
                            <th>Market price</th>
                            <th>Status</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {inventorySummary.ingredients.map((ingredient) => (
                            <tr key={ingredient.ingredientId}>
                              <td>{ingredient.ingredientCode}</td>
                              <td>{ingredient.ingredientName}</td>
                              <td>{ingredient.unit}</td>
                              <td>{ingredient.reorderThreshold}</td>
                              <td>{ingredient.maximumCapacity}</td>
                              <td>Rs {formatMoney(ingredient.marketPrice)}</td>
                              <td><StatusPill tone={ingredient.status === "ACTIVE" ? "success" : "warning"}>{ingredient.status}</StatusPill></td>
                              <td><Button variant="ghost" onClick={() => openEdit("ingredients", ingredient.ingredientId)}>Edit</Button></td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : null}

                  {selectedModuleId === "supplies" && inventorySummary ? (
                    <div className="admin-table-wrapper">
                      <table className="admin-table">
                        <thead>
                          <tr>
                            <th>Code</th>
                            <th>Name</th>
                            <th>Unit</th>
                            <th>Reorder</th>
                            <th>Market price</th>
                            <th>Status</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {inventorySummary.supplies.map((supply) => (
                            <tr key={supply.supplyId}>
                              <td>{supply.supplyCode}</td>
                              <td>{supply.supplyName}</td>
                              <td>{supply.unit}</td>
                              <td>{supply.reorderLevel}</td>
                              <td>Rs {formatMoney(supply.marketPrice)}</td>
                              <td><StatusPill tone={supply.status === "ACTIVE" ? "success" : "warning"}>{supply.status}</StatusPill></td>
                              <td><Button variant="ghost" onClick={() => openEdit("supplies", supply.supplyId)}>Edit</Button></td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : null}

                  {selectedModuleId === "taxes" && billingSummary ? (
                    <div className="admin-table-wrapper">
                      <table className="admin-table">
                        <thead>
                          <tr>
                            <th>Tax ID</th>
                            <th>Name</th>
                            <th>Rate</th>
                            <th>Applies to</th>
                            <th>Status</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {billingSummary.taxes.map((tax) => (
                            <tr key={tax.taxId}>
                              <td>{tax.taxId}</td>
                              <td>{tax.taxName}</td>
                              <td>{tax.ratePercent}%</td>
                              <td>{tax.appliesTo}</td>
                              <td><StatusPill tone={tax.status === "ACTIVE" ? "success" : "warning"}>{tax.status}</StatusPill></td>
                              <td><Button variant="ghost" onClick={() => openEdit("taxes", tax.taxId)}>Edit</Button></td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  ) : null}

                  {selectedModuleId === "billing-templates" && billingSummary ? (
                    <div className="property-settings-template-stack">
                      {billingSummary.templates.map((template) => (
                        <article key={template.templateId} className="admin-link-card property-settings-template-card">
                          <div>
                            <h3>{template.templateName}</h3>
                            <p>{getTemplateSummary(template.description)}</p>
                            {getTemplateChannel(template.description) ? (
                              <p className="admin-inline-note">Channel: {getTemplateChannel(template.description)}</p>
                            ) : null}
                          </div>
                          <div className="property-settings-template-actions">
                            <StatusPill tone={template.status === "ACTIVE" ? "success" : "warning"}>{template.status}</StatusPill>
                            <Button variant="ghost" onClick={() => openEdit("billing-templates", template.templateId)}>Edit</Button>
                          </div>
                        </article>
                      ))}
                    </div>
                  ) : null}

                  {selectedModuleId === "bulk-import" ? (
                    <SectionCard title="Excel workbook placeholder" subtitle="This remains the future bulk-onboarding path for dishes, tables, ingredients, supplies, taxes, templates, and area-section setup.">
                      <div className="property-settings-template-stack">
                        {importSheets.map((sheet) => (
                          <article key={sheet.sheetName} className="admin-link-card property-settings-template-card">
                            <div>
                              <h3>{sheet.sheetName}</h3>
                              <p>{sheet.purpose}</p>
                            </div>
                            <p className="admin-inline-note">Columns: {sheet.requiredColumns.join(", ")}</p>
                          </article>
                        ))}
                      </div>
                    </SectionCard>
                  ) : null}
                </div>

                {editorOpen ? (
                  <div className="property-settings-editor-pane">
                    <div className="property-settings-editor-header">
                      <h3>{getEditorTitle(selectedModuleId, editor?.mode ?? "create")}</h3>
                      <Button variant="ghost" onClick={closeEditor}>Close</Button>
                    </div>

                    {selectedModuleId === "areas-sections" ? (
                      <div className="property-settings-form">
                        <label><span>Floor</span><input value={areaSectionForm.floorName} onChange={(event) => setAreaSectionForm({ ...areaSectionForm, floorName: event.target.value })} /></label>
                        <label><span>Section</span><input value={areaSectionForm.sectionName} onChange={(event) => setAreaSectionForm({ ...areaSectionForm, sectionName: event.target.value })} /></label>
                        <label><span>Max tables</span><input type="number" value={areaSectionForm.maxTableCount} onChange={(event) => setAreaSectionForm({ ...areaSectionForm, maxTableCount: Number(event.target.value) })} /></label>
                        <label><span>Status</span><select value={areaSectionForm.status} onChange={(event) => setAreaSectionForm({ ...areaSectionForm, status: event.target.value })}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label>
                        <div className="property-settings-form-wide property-settings-selector-stack">
                          <label>
                            <span>Waiters</span>
                            <select
                              value={waiterPickerValue}
                              onChange={(event) => {
                                const nextName = event.target.value;
                                setWaiterPickerValue(nextName);
                                if (nextName) {
                                  setAreaSectionForm((current) => ({
                                    ...current,
                                    waiterNames: addUniqueName(current.waiterNames, nextName)
                                  }));
                                  setWaiterPickerValue("");
                                }
                              }}
                            >
                              <option value="">{waiterOptions.length === 0 ? "No waiters available" : "Select waiter"}</option>
                              {waiterOptions.map((employee) => (
                                <option key={employee.employeeId} value={employee.name}>
                                  {employee.name}
                                </option>
                              ))}
                            </select>
                          </label>
                          <div className="property-settings-chip-row">
                            {areaSectionForm.waiterNames.length > 0 ? areaSectionForm.waiterNames.map((name) => (
                              <button
                                key={name}
                                type="button"
                                className="property-settings-chip"
                                onClick={() => setAreaSectionForm((current) => ({
                                  ...current,
                                  waiterNames: current.waiterNames.filter((item) => item !== name)
                                }))}
                              >
                                <span>{name}</span>
                                <strong>×</strong>
                              </button>
                            )) : <span className="admin-inline-note">Select one or more waiters from the employee list.</span>}
                          </div>
                        </div>
                        <div className="property-settings-form-wide property-settings-selector-stack">
                          <label>
                            <span>Cleaners</span>
                            <select
                              value={cleanerPickerValue}
                              onChange={(event) => {
                                const nextName = event.target.value;
                                setCleanerPickerValue(nextName);
                                if (nextName) {
                                  setAreaSectionForm((current) => ({
                                    ...current,
                                    cleanerNames: addUniqueName(current.cleanerNames, nextName)
                                  }));
                                  setCleanerPickerValue("");
                                }
                              }}
                            >
                              <option value="">{cleanerOptions.length === 0 ? "No cleaners available" : "Select cleaner"}</option>
                              {cleanerOptions.map((employee) => (
                                <option key={employee.employeeId} value={employee.name}>
                                  {employee.name}
                                </option>
                              ))}
                            </select>
                          </label>
                          <div className="property-settings-chip-row">
                            {areaSectionForm.cleanerNames.length > 0 ? areaSectionForm.cleanerNames.map((name) => (
                              <button
                                key={name}
                                type="button"
                                className="property-settings-chip"
                                onClick={() => setAreaSectionForm((current) => ({
                                  ...current,
                                  cleanerNames: current.cleanerNames.filter((item) => item !== name)
                                }))}
                              >
                                <span>{name}</span>
                                <strong>×</strong>
                              </button>
                            )) : <span className="admin-inline-note">Select one or more cleaners from the employee list.</span>}
                          </div>
                        </div>
                      </div>
                    ) : null}

                    {selectedModuleId === "tables" ? (
                      <div className="property-settings-form">
                        <label><span>Table code</span><input value={tableForm.tableNumber} onChange={(event) => setTableForm({ ...tableForm, tableNumber: event.target.value })} /></label>
                        <label><span>Display name</span><input value={tableForm.displayName} onChange={(event) => setTableForm({ ...tableForm, displayName: event.target.value })} /></label>
                        <label>
                          <span>Floor</span>
                          <select
                            value={tableForm.floorName}
                            onChange={(event) => setTableForm({ ...tableForm, floorName: event.target.value, sectionName: "" })}
                            disabled={uniqueFloors.length === 0}
                          >
                            <option value="">{uniqueFloors.length === 0 ? "Create area / section first" : "Select floor"}</option>
                            {uniqueFloors.map((floor) => <option key={floor} value={floor}>{floor}</option>)}
                          </select>
                        </label>
                        <label>
                          <span>Section</span>
                          <select
                            value={tableForm.sectionName}
                            onChange={(event) => setTableForm({ ...tableForm, sectionName: event.target.value })}
                            disabled={availableSections.length === 0}
                          >
                            <option value="">{availableSections.length === 0 ? "Select floor first" : "Select section"}</option>
                            {availableSections.map((section) => <option key={section} value={section}>{section}</option>)}
                          </select>
                        </label>
                        <label><span>Capacity</span><input type="number" value={tableForm.capacity} onChange={(event) => setTableForm({ ...tableForm, capacity: Number(event.target.value) })} /></label>
                        <label><span>Status</span><select value={tableForm.status} onChange={(event) => setTableForm({ ...tableForm, status: event.target.value })}><option value="AVAILABLE">Available</option><option value="UNAVAILABLE">Unavailable</option><option value="RESERVED">Reserved</option><option value="OCCUPIED">Occupied</option><option value="NEEDS_CLEANING">Needs cleaning</option></select></label>
                        <label className="property-settings-checkbox"><input type="checkbox" checked={tableForm.active} onChange={(event) => setTableForm({ ...tableForm, active: event.target.checked })} />Active</label>
                        {selectedAreaSection ? (
                          <div className="property-settings-form-wide property-settings-inline-note">
                            <strong>{selectedAreaSection.floorName} / {selectedAreaSection.sectionName}</strong>
                            <span>
                              Capacity for this zone: {selectedAreaSection.maxTableCount} tables.
                              Waiters: {selectedAreaSection.waiterNames.join(", ") || "Unassigned"}.
                              Cleaners: {selectedAreaSection.cleanerNames.join(", ") || "Unassigned"}.
                            </span>
                          </div>
                        ) : null}
                        {uniqueFloors.length === 0 ? (
                          <div className="property-settings-form-wide property-settings-inline-note">
                            <span>Create property areas and sections first so tables can be assigned to a defined floor and section.</span>
                          </div>
                        ) : null}
                      </div>
                    ) : null}

                    {selectedModuleId === "menu-recipes" ? (
                      <div className="property-settings-form">
                        <label><span>Dish code</span><input value={menuForm.itemCode} onChange={(event) => setMenuForm({ ...menuForm, itemCode: event.target.value })} /></label>
                        <label><span>Dish name</span><input value={menuForm.itemName} onChange={(event) => setMenuForm({ ...menuForm, itemName: event.target.value })} /></label>
                        <label><span>Category</span><input placeholder="Italian dishes" value={menuForm.categoryName} onChange={(event) => setMenuForm({ ...menuForm, categoryName: event.target.value })} /></label>
                        <label className="property-settings-form-wide"><span>Description</span><textarea rows={3} value={menuForm.description} onChange={(event) => setMenuForm({ ...menuForm, description: event.target.value })} /></label>
                        <label><span>Price</span><input type="number" value={menuForm.price} onChange={(event) => setMenuForm({ ...menuForm, price: Number(event.target.value) })} /></label>
                        <div className="property-settings-inline-note property-settings-form-wide">
                          {suggestedProfitAmount !== null ? (
                            <>
                              <strong>Suggested profit amount: Rs {formatMoney(suggestedProfitAmount)}</strong>
                              <span>Ingredients cost for this dish: Rs {formatMoney(ingredientCost)}</span>
                            </>
                          ) : (
                            <span>Add ingredients for suggested amount.</span>
                          )}
                        </div>
                        <label><span>Prep time (min)</span><input type="number" value={menuForm.prepTimeMinutes} onChange={(event) => setMenuForm({ ...menuForm, prepTimeMinutes: Number(event.target.value) })} /></label>
                        <label><span>Status</span><select value={menuForm.status} onChange={(event) => setMenuForm({ ...menuForm, status: event.target.value })}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label>
                        <label className="property-settings-checkbox"><input type="checkbox" checked={menuForm.vegetarian} onChange={(event) => setMenuForm({ ...menuForm, vegetarian: event.target.checked })} />Vegetarian</label>
                        <div className="property-settings-form-wide property-settings-recipe-builder">
                          <div className="property-settings-recipe-header">
                            <span>Recipe ingredients</span>
                            <Button variant="ghost" onClick={() => addRecipeRow(setMenuForm)}>Add ingredient</Button>
                          </div>
                          <div className="property-settings-recipe-stack">
                            {menuForm.recipe.length === 0 ? <p className="admin-inline-note">Add ingredients to build the recipe and suggested amount.</p> : null}
                            {menuForm.recipe.map((recipeRow, index) => {
                              const ingredient = ingredientOptions.find((item) => item.ingredientId === recipeRow.ingredientId);
                              return (
                                <div key={`${recipeRow.ingredientId}-${index}`} className="property-settings-recipe-row">
                                  <div className="property-settings-recipe-row-header">
                                    <strong>Ingredient {index + 1}</strong>
                                    <Button variant="ghost" onClick={() => removeRecipeRow(setMenuForm, index)}>Remove</Button>
                                  </div>
                                  <label className="property-settings-recipe-field">
                                    <span>Ingredient</span>
                                    <select
                                      value={recipeRow.ingredientId}
                                      onChange={(event) => {
                                        const nextIngredient = ingredientOptions.find((item) => item.ingredientId === event.target.value);
                                        updateRecipeRow(setMenuForm, index, {
                                          ingredientId: event.target.value,
                                          ingredientName: nextIngredient?.ingredientName ?? "",
                                          quantity: recipeRow.quantity
                                        });
                                      }}
                                    >
                                      <option value="">Select ingredient</option>
                                      {ingredientOptions.map((item) => (
                                        <option key={item.ingredientId} value={item.ingredientId}>
                                          {item.ingredientName}
                                        </option>
                                      ))}
                                    </select>
                                  </label>
                                  <div className="property-settings-recipe-controls">
                                    <label className="property-settings-recipe-field">
                                      <span>Quantity</span>
                                      <input
                                        type="number"
                                        min="1"
                                        step="1"
                                        placeholder="1"
                                        value={recipeRow.quantity}
                                        onChange={(event) => updateRecipeRow(setMenuForm, index, { ...recipeRow, quantity: event.target.value })}
                                      />
                                    </label>
                                    <div className="property-settings-recipe-meta">
                                      <span className="property-settings-recipe-unit">{ingredient?.unit ?? "unit"}</span>
                                      <span className="property-settings-recipe-cost">
                                        Cost: Rs {formatMoney(calculateRecipeRowCost(recipeRow, ingredientOptions))}
                                      </span>
                                    </div>
                                  </div>
                                </div>
                              );
                            })}
                          </div>
                        </div>
                      </div>
                    ) : null}

                    {selectedModuleId === "ingredients" ? (
                      <div className="property-settings-form">
                        <label><span>Ingredient code</span><input value={ingredientForm.ingredientCode} onChange={(event) => setIngredientForm({ ...ingredientForm, ingredientCode: event.target.value })} /></label>
                        <label><span>Ingredient name</span><input value={ingredientForm.ingredientName} onChange={(event) => setIngredientForm({ ...ingredientForm, ingredientName: event.target.value })} /></label>
                        <label>
                          <span>Unit</span>
                          <select value={ingredientForm.unit} onChange={(event) => setIngredientForm({ ...ingredientForm, unit: event.target.value })}>
                            <option value="">Select unit</option>
                            {ingredientUnitOptions.map((unit) => (
                              <option key={unit} value={unit}>
                                {unit}
                              </option>
                            ))}
                          </select>
                        </label>
                        <label><span>Reorder threshold</span><input type="number" min="0" step="1" value={ingredientForm.reorderThreshold} onChange={(event) => setIngredientForm({ ...ingredientForm, reorderThreshold: Number(event.target.value) })} /></label>
                        <label><span>Maximum capacity</span><input type="number" min="0" step="1" value={ingredientForm.maximumCapacity} onChange={(event) => setIngredientForm({ ...ingredientForm, maximumCapacity: Number(event.target.value) })} /></label>
                        <label>
                          <span>{ingredientForm.unit ? `Market price (per ${ingredientForm.unit})` : "Market price"}</span>
                          <input type="number" min="0" step="0.01" value={ingredientForm.marketPrice} onChange={(event) => setIngredientForm({ ...ingredientForm, marketPrice: Number(event.target.value) })} />
                        </label>
                        <label><span>Status</span><select value={ingredientForm.status} onChange={(event) => setIngredientForm({ ...ingredientForm, status: event.target.value })}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label>
                      </div>
                    ) : null}

                    {selectedModuleId === "supplies" ? (
                      <div className="property-settings-form">
                        <label><span>Supply code</span><input value={supplyForm.supplyCode} onChange={(event) => setSupplyForm({ ...supplyForm, supplyCode: event.target.value })} /></label>
                        <label><span>Supply name</span><input value={supplyForm.supplyName} onChange={(event) => setSupplyForm({ ...supplyForm, supplyName: event.target.value })} /></label>
                        <label><span>Unit</span><input value={supplyForm.unit} onChange={(event) => setSupplyForm({ ...supplyForm, unit: event.target.value })} /></label>
                        <label><span>Reorder level</span><input type="number" value={supplyForm.reorderLevel} onChange={(event) => setSupplyForm({ ...supplyForm, reorderLevel: Number(event.target.value) })} /></label>
                        <label><span>Market price</span><input type="number" value={supplyForm.marketPrice} onChange={(event) => setSupplyForm({ ...supplyForm, marketPrice: Number(event.target.value) })} /></label>
                        <label><span>Status</span><select value={supplyForm.status} onChange={(event) => setSupplyForm({ ...supplyForm, status: event.target.value })}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label>
                      </div>
                    ) : null}

                    {selectedModuleId === "taxes" ? (
                      <div className="property-settings-form">
                        <label><span>Tax ID</span><input value={taxForm.taxId} onChange={(event) => setTaxForm({ ...taxForm, taxId: event.target.value })} /></label>
                        <label><span>Tax name</span><input value={taxForm.taxName} onChange={(event) => setTaxForm({ ...taxForm, taxName: event.target.value })} /></label>
                        <label><span>Rate (%)</span><input type="number" value={taxForm.ratePercent} onChange={(event) => setTaxForm({ ...taxForm, ratePercent: Number(event.target.value) })} /></label>
                        <label><span>Applies to</span><select value={taxForm.appliesTo} onChange={(event) => setTaxForm({ ...taxForm, appliesTo: event.target.value })}><option value="FINAL_BILL">Final bill</option><option value="SERVICE">Service</option></select></label>
                        <label><span>Status</span><select value={taxForm.status} onChange={(event) => setTaxForm({ ...taxForm, status: event.target.value })}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label>
                      </div>
                    ) : null}

                    {selectedModuleId === "billing-templates" ? (
                      <div className="property-settings-form">
                        <label><span>Template ID</span><input value={templateForm.templateId} onChange={(event) => setTemplateForm({ ...templateForm, templateId: event.target.value })} /></label>
                        <label><span>Template name</span><input value={templateForm.templateName} onChange={(event) => setTemplateForm({ ...templateForm, templateName: event.target.value })} /></label>
                        <label><span>Status</span><select value={templateForm.status} onChange={(event) => setTemplateForm({ ...templateForm, status: event.target.value })}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option></select></label>
                        <label className="property-settings-form-wide"><span>Template JSON</span><textarea rows={10} value={templateJsonText} onChange={(event) => setTemplateJsonText(event.target.value)} /></label>
                      </div>
                    ) : null}

                    <div className="property-settings-editor-actions">
                      <Button variant="primary" onClick={() => void saveCurrentModule()}>
                        {editor?.mode === "create" ? "Create" : "Save changes"}
                      </Button>
                      <Button variant="ghost" onClick={closeEditor}>Cancel</Button>
                    </div>
                  </div>
                ) : null}
              </div>
            </SectionCard>
          ) : null}
        </div>
      </div>
    </SectionCard>
  );
}

function calculateIngredientCost(
  recipe: Array<{ ingredientId: string; quantity: string }>,
  ingredients: Array<{ ingredientId: string; marketPrice: number }>
) {
  return recipe.reduce((total, recipeItem) => {
    const ingredient = ingredients.find((item) => item.ingredientId === recipeItem.ingredientId);
    const quantity = Number.parseFloat(recipeItem.quantity);
    if (!ingredient || Number.isNaN(quantity)) {
      return total;
    }
    return total + ingredient.marketPrice * quantity;
  }, 0);
}

function calculateRecipeRowCost(
  recipeItem: { ingredientId: string; quantity: string },
  ingredients: Array<{ ingredientId: string; marketPrice: number }>
) {
  const ingredient = ingredients.find((item) => item.ingredientId === recipeItem.ingredientId);
  const quantity = Number.parseFloat(recipeItem.quantity);
  if (!ingredient || Number.isNaN(quantity)) {
    return 0;
  }
  return ingredient.marketPrice * quantity;
}

function formatMoney(value: number) {
  return value.toFixed(2);
}

function addRecipeRow(setMenuForm: Dispatch<SetStateAction<MenuSettingPayload>>) {
  setMenuForm((current) => ({
    ...current,
    recipe: [
      ...current.recipe,
      {
        ingredientId: "",
        ingredientName: "",
        quantity: ""
      }
    ]
  }));
}

function updateRecipeRow(setMenuForm: Dispatch<SetStateAction<MenuSettingPayload>>, index: number, nextValue: RecipeIngredientPayload) {
  setMenuForm((current) => ({
    ...current,
    recipe: current.recipe.map((item, itemIndex) => (itemIndex === index ? nextValue : item))
  }));
}

function removeRecipeRow(setMenuForm: Dispatch<SetStateAction<MenuSettingPayload>>, index: number) {
  setMenuForm((current) => ({
    ...current,
    recipe: current.recipe.filter((_, itemIndex) => itemIndex !== index)
  }));
}

function addUniqueName(currentValues: string[], nextValue: string) {
  if (!nextValue.trim() || currentValues.includes(nextValue)) {
    return currentValues;
  }
  return [...currentValues, nextValue];
}

function getTemplateSummary(description: Record<string, unknown>) {
  const summary = description.summary;
  if (typeof summary === "string" && summary.trim().length > 0) {
    return summary;
  }
  return "Structured bill-template definition placeholder is ready.";
}

function getTemplateChannel(description: Record<string, unknown>) {
  const channel = description.channel;
  if (typeof channel === "string" && channel.trim().length > 0) {
    return channel;
  }
  return null;
}

function getEditorTitle(moduleId: ModuleId, mode: "create" | "edit") {
  const prefix = mode === "create" ? "Add" : "Edit";
  switch (moduleId) {
    case "areas-sections":
      return `${prefix} area / section`;
    case "tables":
      return `${prefix} table`;
    case "menu-recipes":
      return `${prefix} dish and recipe`;
    case "ingredients":
      return `${prefix} ingredient`;
    case "supplies":
      return `${prefix} operational supply`;
    case "taxes":
      return `${prefix} tax setting`;
    case "billing-templates":
      return `${prefix} billing template`;
    default:
      return `${prefix} setting`;
  }
}
