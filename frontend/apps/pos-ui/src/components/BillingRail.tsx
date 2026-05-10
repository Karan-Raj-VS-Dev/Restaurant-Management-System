import { Button, EmptyState, StatusPill } from "@restaurant/ui";
import type { BillRecord } from "@restaurant/api";

export function BillingRail(props: {
  bills: BillRecord[];
  busyBillId: string | null;
  onFinalize: (billId: string) => Promise<void>;
  onPay: (bill: BillRecord) => Promise<void>;
}) {
  if (props.bills.length === 0) {
    return <EmptyState title="No bills yet" body="Draft bills will appear here as orders are created." />;
  }

  return (
    <div className="pos-billing-stack">
      {props.bills.map((bill) => (
        <article key={bill.billId} className="pos-bill-card">
          <div className="pos-bill-card-head">
            <div>
              <h3>{bill.billId}</h3>
              <p>{bill.orderId}</p>
            </div>
            <StatusPill tone={bill.status === "PAID" ? "success" : bill.status === "FINALIZED" ? "info" : "warning"}>
              {bill.status}
            </StatusPill>
          </div>
          <div className="pos-bill-total">Rs {bill.total}</div>
          <div className="pos-bill-actions">
            <Button
              variant="ghost"
              disabled={bill.status !== "DRAFT" || props.busyBillId === bill.billId}
              onClick={() => props.onFinalize(bill.billId)}
            >
              Finalize
            </Button>
            <Button
              variant="secondary"
              disabled={bill.status !== "FINALIZED" || props.busyBillId === bill.billId}
              onClick={() => props.onPay(bill)}
            >
              Collect UPI
            </Button>
          </div>
        </article>
      ))}
    </div>
  );
}
