import { Button, EmptyState, StatusPill } from "@restaurant/ui";
import type { MenuItem, OrderLine, TableRecord } from "@restaurant/api";

export function OrderComposer(props: {
  waiterName: string | null;
  selectedTable: TableRecord | undefined;
  cart: OrderLine[];
  menuById: Map<string, MenuItem>;
  onIncrement: (itemId: string) => void;
  onDecrement: (itemId: string) => void;
  onSubmit: () => Promise<void>;
  busy: boolean;
  note: string | null;
  alerts?: string[];
}) {
  const total = props.cart.reduce((sum, line) => {
    const price = props.menuById.get(line.itemId)?.price ?? 0;
    return sum + price * line.quantity;
  }, 0);

  return (
    <div className="pos-order-composer">
      <div className="pos-order-topline">
        <StatusPill tone="muted">
          {props.selectedTable ? props.selectedTable.displayName || props.selectedTable.tableNumber || props.selectedTable.tableId : "Select a table"}
        </StatusPill>
        <StatusPill tone="muted">{props.waiterName ? `Server ${props.waiterName}` : "Choose a server"}</StatusPill>
      </div>

      {props.cart.length === 0 ? (
        <EmptyState title="No items added" body="Choose dishes from the live menu and they will appear here for submission." />
      ) : (
        <div className="pos-order-lines">
          {props.cart.map((line) => {
            const price = props.menuById.get(line.itemId)?.price ?? 0;
            return (
              <div className="pos-order-line" key={line.itemId}>
                <div>
                  <strong>{line.itemName}</strong>
                  <p>Rs {price} each</p>
                </div>
                <div className="pos-order-actions">
                  <Button variant="ghost" onClick={() => props.onDecrement(line.itemId)}>
                    -
                  </Button>
                  <span>{line.quantity}</span>
                  <Button variant="ghost" onClick={() => props.onIncrement(line.itemId)}>
                    +
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <div className="pos-order-footer">
        <div>
          <div className="pos-order-total-label">Estimated total</div>
          <div className="pos-order-total">Rs {total}</div>
        </div>
        <Button
          disabled={
            !props.selectedTable ||
            props.selectedTable.status !== "OCCUPIED" ||
            !props.waiterName ||
            props.cart.length === 0 ||
            props.busy
          }
          onClick={props.onSubmit}
        >
          {props.busy ? "Sending order..." : "Send to kitchen"}
        </Button>
      </div>

      {props.selectedTable && props.selectedTable.status !== "OCCUPIED" ? (
        <p className="pos-note">
          Occupy this table before sending dishes to the kitchen.
        </p>
      ) : null}
      {props.note ? <p className="pos-note">{props.note}</p> : null}
      {props.alerts && props.alerts.length > 0 ? (
        <div className="pos-note-list">
          {props.alerts.map((alert, index) => (
            <div key={`${alert}-${index}`} className="pos-note-banner">
              {alert}
            </div>
          ))}
        </div>
      ) : null}
    </div>
  );
}
