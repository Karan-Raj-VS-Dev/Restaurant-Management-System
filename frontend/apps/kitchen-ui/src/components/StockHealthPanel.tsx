import { StatusPill } from "@restaurant/ui";
import type { StockItem } from "@restaurant/api";

export function StockHealthPanel(props: { stock: StockItem[] }) {
  return (
    <div className="kitchen-stock-layout">
      <div className="kitchen-stock-list">
        {props.stock.map((item) => (
          <article key={item.ingredientId} className="kitchen-stock-card">
            <div className="kitchen-stock-topline">
              <strong>{item.ingredientName}</strong>
              <StatusPill tone={item.stockHealth === "OUT_OF_STOCK" ? "danger" : item.stockHealth === "LOW_STOCK" ? "warning" : item.stockHealth === "OVER_CAPACITY" ? "info" : "success"}>
                {item.stockHealth === "OUT_OF_STOCK" ? "Out" : item.stockHealth === "LOW_STOCK" ? "Low" : item.stockHealth === "OVER_CAPACITY" ? "Over" : "Stable"}
              </StatusPill>
            </div>
            <div className="kitchen-stock-qty">
              {item.onHandQuantity} {item.unit}
            </div>
            <div className="kitchen-stock-detail">
              Reorder at {item.reorderThreshold} {item.unit}
            </div>
            <div className="kitchen-stock-detail">
              Max capacity {item.maximumCapacity} {item.unit}
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
