import {
  applyInventoryStockAdjustments,
  importInventoryStockSheet,
  loadInventoryDashboard,
  type InventoryStockAdjustmentPayload,
  type PropertyRecord,
  type StockItem
} from "@restaurant/api";
import { Button, LivePulse, SectionCard, StatCard, StatusPill, usePollingResource } from "@restaurant/ui";
import { useMemo, useState } from "react";

interface InventoryManagementPageProps {
  selectedProperty: PropertyRecord;
}

type ConfirmMode = "apply" | "cancel" | "import" | null;

export function InventoryManagementPage(props: InventoryManagementPageProps) {
  const { data, loading, refreshing, lastUpdated, error, refresh } = usePollingResource(loadInventoryDashboard, 7000);
  const [editMode, setEditMode] = useState(false);
  const [pendingAdjustments, setPendingAdjustments] = useState<Record<string, string>>({});
  const [confirmMode, setConfirmMode] = useState<ConfirmMode>(null);
  const [uploaderOpen, setUploaderOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [busy, setBusy] = useState(false);
  const [banner, setBanner] = useState<{ tone: "success" | "error"; message: string } | null>(null);

  const stockItems = data?.stock ?? [];
  const lowThresholdItems = useMemo(
    () => stockItems.filter((item) => item.stockHealth === "LOW_STOCK" || item.stockHealth === "OUT_OF_STOCK").length,
    [stockItems]
  );
  const overCapacityItems = useMemo(
    () => stockItems.filter((item) => item.stockHealth === "OVER_CAPACITY").length,
    [stockItems]
  );
  const hasPendingAdjustments = useMemo(
    () => Object.values(pendingAdjustments).some((value) => value.trim() && Number(value) !== 0),
    [pendingAdjustments]
  );

  const refreshWithBannerReset = async () => {
    setBanner(null);
    await refresh();
  };

  const updateAdjustment = (ingredientId: string, value: string) => {
    setPendingAdjustments((current) => ({
      ...current,
      [ingredientId]: value
    }));
    setBanner((current) => (current?.tone === "error" ? null : current));
  };

  const openCancelConfirmation = () => {
    if (!hasPendingAdjustments) {
      setEditMode(false);
      setPendingAdjustments({});
      return;
    }
    setConfirmMode("cancel");
  };

  const handleConfirm = async () => {
    if (confirmMode === "cancel") {
      setPendingAdjustments({});
      setEditMode(false);
      setConfirmMode(null);
      return;
    }

    setBusy(true);
    try {
      if (confirmMode === "apply") {
        const adjustments = buildAdjustmentsPayload(pendingAdjustments);
        if (adjustments.length === 0) {
          setBanner({
            tone: "error",
            message: "Enter at least one stock change before confirming."
          });
          setConfirmMode(null);
          return;
        }

        const payload: InventoryStockAdjustmentPayload = {
          adjustments,
          reason: "Manual inventory update"
        };
        await applyInventoryStockAdjustments(payload);
        await refresh();
        setPendingAdjustments({});
        setEditMode(false);
        setConfirmMode(null);
        setBanner({
          tone: "success",
          message: "Inventory stock updated successfully. Menu availability will refresh on the live polling cycle."
        });
        return;
      }

      if (confirmMode === "import") {
        if (!selectedFile) {
          setBanner({
            tone: "error",
            message: "Choose a stock sheet before uploading."
          });
          setConfirmMode(null);
          return;
        }
        if (!selectedFile.name.toLowerCase().endsWith(".csv")) {
          setBanner({
            tone: "error",
            message: "Upload the CSV template downloaded from this page. You can edit it in Excel and save it back as CSV."
          });
          setConfirmMode(null);
          return;
        }

        const fileContent = await selectedFile.text();
        await importInventoryStockSheet({
          fileName: selectedFile.name,
          fileContent
        });
        await refresh();
        setSelectedFile(null);
        setUploaderOpen(false);
        setConfirmMode(null);
        setBanner({
          tone: "success",
          message: "Inventory stock imported successfully. Updated quantities are now live."
        });
      }
    } catch (caughtError) {
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to update inventory."
      });
      setConfirmMode(null);
    } finally {
      setBusy(false);
    }
  };

  const downloadTemplate = () => {
    if (stockItems.length === 0) {
      setBanner({
        tone: "error",
        message: "There are no inventory items to include in the template yet."
      });
      return;
    }

    const rows = [
      ["Name", "On hand", "Unit", "Max threshold", "Min threshold"],
      ...stockItems.map((item) => [
        item.ingredientName,
        "0",
        item.unit,
        String(item.maximumCapacity),
        String(item.reorderThreshold)
      ])
    ];

    const csv = rows.map((row) => row.map(escapeCsvValue).join(",")).join("\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `${props.selectedProperty.propertyId}-inventory-template.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="admin-dashboard-stack">
      <div className="admin-toolbar">
        <LivePulse label={refreshing ? "Refreshing inventory" : "Inventory polling active"} lastUpdated={lastUpdated} />
        <div className="inventory-toolbar-actions">
          {!editMode ? (
            <>
              <Button variant="ghost" onClick={() => void refreshWithBannerReset()}>
                Refresh now
              </Button>
              <Button variant="ghost" onClick={() => setUploaderOpen(true)}>
                Excel uploader
              </Button>
              <Button onClick={() => setEditMode(true)}>Update stock</Button>
            </>
          ) : (
            <>
              <Button variant="ghost" onClick={openCancelConfirmation}>
                Cancel
              </Button>
              <Button onClick={() => setConfirmMode("apply")}>Confirm changes</Button>
            </>
          )}
        </div>
      </div>

      <div className="admin-landing-stats">
        <StatCard label="Tracked stock items" value={String(stockItems.length)} hint={`Live stock list for ${props.selectedProperty.name}`} tone="warm" />
        <StatCard label="Low-threshold alerts" value={String(lowThresholdItems)} hint="At or below reorder threshold" tone="alert" />
        <StatCard label="Over-capacity alerts" value={String(overCapacityItems)} hint="Above the configured maximum capacity" tone="cool" />
      </div>

      {banner ? <div className={`admin-alert ${banner.tone === "success" ? "admin-alert-success" : "admin-alert-error"}`}>{banner.message}</div> : null}

      <SectionCard
        title="Inventory management"
        subtitle="Adjust live stock here. Reorder threshold marks low stock, and maximum capacity highlights overfilled inventory. Diner menu availability refreshes automatically from the same stock source."
      >
        {loading && !data ? <p className="admin-inline-note">Loading inventory snapshot...</p> : null}
        {error ? <p className="admin-alert admin-alert-error">{error}</p> : null}
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Ingredient</th>
                <th>On hand</th>
                <th>Unit</th>
                <th>Reorder threshold</th>
                <th>Maximum capacity</th>
                <th>Health</th>
                {editMode ? <th>Stock change (+/-)</th> : null}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={editMode ? 7 : 6} className="admin-empty-cell">
                    Loading inventory...
                  </td>
                </tr>
              ) : stockItems.length === 0 ? (
                <tr>
                  <td colSpan={editMode ? 7 : 6} className="admin-empty-cell">
                    No inventory items have been configured for this property yet.
                  </td>
                </tr>
              ) : (
                stockItems.map((item) => (
                  <tr key={item.ingredientId}>
                    <td>{item.ingredientName}</td>
                    <td>{item.onHandQuantity}</td>
                    <td>{item.unit}</td>
                    <td>{item.reorderThreshold}</td>
                    <td>{item.maximumCapacity}</td>
                    <td>
                      <StatusPill tone={inventoryHealthTone(item.stockHealth)}>
                        {formatInventoryHealth(item.stockHealth)}
                      </StatusPill>
                    </td>
                    {editMode ? (
                      <td>
                        <input
                          className="inventory-adjust-input"
                          type="number"
                          step="1"
                          value={pendingAdjustments[item.ingredientId] ?? ""}
                          onChange={(event) => updateAdjustment(item.ingredientId, event.target.value)}
                          placeholder="0"
                        />
                      </td>
                    ) : null}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </SectionCard>

      {uploaderOpen ? (
        <div className="inventory-modal-overlay" role="presentation">
          <div className="inventory-modal-card" role="dialog" aria-modal="true" aria-labelledby="inventory-upload-title">
            <div className="inventory-modal-header">
              <h3 id="inventory-upload-title">Excel uploader</h3>
              <Button variant="ghost" onClick={() => setUploaderOpen(false)}>
                Close
              </Button>
            </div>
            <div className="inventory-modal-content">
              <p className="admin-inline-note">
                Download the Excel-friendly CSV template, update only the <strong>On hand</strong> column with positive or negative numbers,
                then upload the saved CSV back here. Keep the Name, Unit, Max threshold, and Min threshold columns unchanged.
              </p>
              <div className="inventory-modal-actions">
                <Button variant="ghost" onClick={downloadTemplate}>
                  Download template
                </Button>
              </div>
              <label className="inventory-file-field">
                <span>Upload updated CSV</span>
                <input
                  type="file"
                  accept=".csv"
                  onChange={(event) => setSelectedFile(event.target.files?.[0] ?? null)}
                />
              </label>
              {selectedFile ? <p className="admin-inline-note">Selected file: {selectedFile.name}</p> : null}
              <div className="inventory-modal-actions">
                <Button variant="ghost" onClick={() => setUploaderOpen(false)}>
                  Cancel
                </Button>
                <Button onClick={() => setConfirmMode("import")}>Upload stock sheet</Button>
              </div>
            </div>
          </div>
        </div>
      ) : null}

      {confirmMode ? (
        <div className="inventory-modal-overlay" role="presentation">
          <div className="inventory-modal-card inventory-confirm-card" role="dialog" aria-modal="true" aria-labelledby="inventory-confirm-title">
            <div className="inventory-modal-header">
              <h3 id="inventory-confirm-title">{confirmMode === "apply" ? "Confirm stock update" : confirmMode === "cancel" ? "Discard stock edits" : "Confirm stock upload"}</h3>
            </div>
            <div className="inventory-modal-content">
              <p>
                {confirmMode === "apply"
                  ? "Apply the entered stock changes to this property's live inventory?"
                  : confirmMode === "cancel"
                    ? "Discard the stock changes you entered in this edit session?"
                    : "Upload this stock sheet and apply all listed stock changes?"}
              </p>
              <div className="inventory-modal-actions">
                <Button variant="ghost" onClick={() => setConfirmMode(null)} disabled={busy}>
                  Go back
                </Button>
                <Button onClick={() => void handleConfirm()} disabled={busy}>
                  {busy ? "Working..." : "Confirm"}
                </Button>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function buildAdjustmentsPayload(pendingAdjustments: Record<string, string>) {
  return Object.entries(pendingAdjustments).flatMap(([ingredientId, value]) => {
    const trimmed = value.trim();
    if (!trimmed) {
      return [];
    }
    const quantityDelta = Number(trimmed);
    if (!Number.isFinite(quantityDelta) || quantityDelta === 0) {
      return [];
    }
    return [{ ingredientId, quantityDelta: Math.trunc(quantityDelta) }];
  });
}

function inventoryHealthTone(stockHealth: string): "success" | "warning" | "danger" | "info" {
  if (stockHealth === "OUT_OF_STOCK") {
    return "danger";
  }
  if (stockHealth === "LOW_STOCK") {
    return "warning";
  }
  if (stockHealth === "OVER_CAPACITY") {
    return "info";
  }
  return "success";
}

function formatInventoryHealth(stockHealth: string) {
  if (stockHealth === "OUT_OF_STOCK") {
    return "Out of stock";
  }
  if (stockHealth === "LOW_STOCK") {
    return "Low stock";
  }
  if (stockHealth === "OVER_CAPACITY") {
    return "Over capacity";
  }
  return "Stable";
}

function escapeCsvValue(value: string) {
  if (value.includes(",") || value.includes("\"") || value.includes("\n")) {
    return `"${value.replaceAll("\"", "\"\"")}"`;
  }
  return value;
}
