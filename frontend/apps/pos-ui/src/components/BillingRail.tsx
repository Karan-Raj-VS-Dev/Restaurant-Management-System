import { Button, EmptyState, StatusPill } from "@restaurant/ui";
import type { BillRecord } from "@restaurant/api";

export function BillingRail(props: {
  bills: BillRecord[];
  tableNameById: Map<string, string>;
  busyBillId: string | null;
  onFinalize: (bill: BillRecord) => Promise<void>;
  onPay: (bill: BillRecord) => void;
}) {
  if (props.bills.length === 0) {
    return <EmptyState title="No bills yet" body="Draft bills will appear here as orders are created." />;
  }

  return (
    <div className="pos-billing-stack">
      {props.bills.map((bill) => {
        const linkedOrderIds = bill.orderIds ?? [bill.orderId];
        const tableName = bill.tableId ? props.tableNameById.get(bill.tableId) ?? null : null;
        const title = tableName ? `${tableName} bill` : "Walk-in bill";
        return (
          <article key={bill.billId} className="pos-bill-card">
            <div className="pos-bill-card-head">
              <div>
                <h3>{title}</h3>
                <p>{linkedOrderIds.length > 1 ? `${linkedOrderIds.length} linked orders` : "1 linked order"}</p>
              </div>
              <StatusPill tone={bill.status === "PAID" ? "success" : bill.status === "FINALIZED" ? "info" : "warning"}>
                {bill.status}
              </StatusPill>
            </div>
            <div className="pos-bill-line-items">
              {bill.items.map((item) => (
                <div key={item.itemId} className="pos-bill-line-item">
                  <span>
                    {item.quantity}× {item.itemName}
                  </span>
                  <strong>Rs {(item.quantity * item.unitPrice).toFixed(2)}</strong>
                </div>
              ))}
            </div>
            <div className="pos-bill-total">Rs {bill.total}</div>
            <div className="pos-bill-actions">
              <Button
                variant="ghost"
                disabled={bill.status !== "DRAFT" || props.busyBillId === bill.billId}
                onClick={() => props.onFinalize(bill)}
              >
                Finalize
              </Button>
              <Button
                variant="secondary"
                disabled={bill.status !== "FINALIZED" || props.busyBillId === bill.billId}
                onClick={() => props.onPay(bill)}
              >
                Collect payment
              </Button>
            </div>
          </article>
        );
      })}
    </div>
  );
}
