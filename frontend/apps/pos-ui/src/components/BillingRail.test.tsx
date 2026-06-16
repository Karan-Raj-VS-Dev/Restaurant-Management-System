import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { BillingRail } from "./BillingRail";
import type { BillRecord } from "@restaurant/api";

const bills: BillRecord[] = [
  {
    billId: "bill-001",
    lastOrderId: "order-002",
    orderIds: ["order-001", "order-002"],
    tableId: "table-01",
    sessionId: "session-01",
    customerId: null,
    status: "DRAFT",
    settlementType: "STANDARD",
    cancellationReason: null,
    cancellationFee: 0,
    items: [
      { itemId: "item-001", itemName: "Margherita Pizza", quantity: 2, unitPrice: 299 },
      { itemId: "item-002", itemName: "Pasta Alfredo", quantity: 1, unitPrice: 249 }
    ],
    subtotal: 847,
    tax: 42.35,
    total: 889.35
  },
  {
    billId: "bill-002",
    lastOrderId: "order-003",
    orderIds: ["order-003"],
    tableId: "table-02",
    sessionId: "session-02",
    customerId: null,
    status: "FINALIZED",
    settlementType: "STANDARD",
    cancellationReason: null,
    cancellationFee: 0,
    items: [{ itemId: "item-003", itemName: "Garlic Bread", quantity: 1, unitPrice: 149 }],
    subtotal: 149,
    tax: 7.45,
    total: 156.45
  }
];

describe("BillingRail", () => {
  it("shows the empty state when there are no bills", () => {
    render(
      <BillingRail
        bills={[]}
        tableNameById={new Map()}
        busyBillId={null}
        onFinalize={vi.fn(async () => {})}
        onPay={vi.fn()}
      />
    );

    expect(screen.getByText("No bills yet")).toBeInTheDocument();
    expect(screen.getByText("Draft bills will appear here as orders are created.")).toBeInTheDocument();
  });

  it("renders friendly bill titles and linked orders without exposing bill ids", () => {
    render(
      <BillingRail
        bills={bills}
        tableNameById={new Map([
          ["table-01", "Main Table"],
          ["table-02", "Window 02"]
        ])}
        busyBillId={null}
        onFinalize={vi.fn(async () => {})}
        onPay={vi.fn()}
      />
    );

    expect(screen.getByText("Main Table bill")).toBeInTheDocument();
    expect(screen.getByText("2 linked orders")).toBeInTheDocument();
    expect(screen.queryByText("bill-001")).not.toBeInTheDocument();
    expect(screen.getByText("2× Margherita Pizza")).toBeInTheDocument();
    expect(screen.getByText("Rs 598.00")).toBeInTheDocument();
  });

  it("routes finalize and payment actions to their callbacks", () => {
    const onFinalize = vi.fn(async () => {});
    const onPay = vi.fn();

    render(
      <BillingRail
        bills={bills}
        tableNameById={new Map([
          ["table-01", "Main Table"],
          ["table-02", "Window 02"]
        ])}
        busyBillId={null}
        onFinalize={onFinalize}
        onPay={onPay}
      />
    );

    fireEvent.click(screen.getAllByRole("button", { name: "Finalize" })[0]);
    fireEvent.click(screen.getAllByRole("button", { name: "Collect payment" })[1]);

    expect(onFinalize).toHaveBeenCalledWith(bills[0]);
    expect(onPay).toHaveBeenCalledWith(bills[1]);
  });

  it("disables actions when the bill state does not allow them or the bill is busy", () => {
    render(
      <BillingRail
        bills={bills}
        tableNameById={new Map([
          ["table-01", "Main Table"],
          ["table-02", "Window 02"]
        ])}
        busyBillId="bill-001"
        onFinalize={vi.fn(async () => {})}
        onPay={vi.fn()}
      />
    );

    expect(screen.getAllByRole("button", { name: "Finalize" })[0]).toBeDisabled();
    expect(screen.getAllByRole("button", { name: "Collect payment" })[0]).toBeDisabled();
    expect(screen.getAllByRole("button", { name: "Collect payment" })[1]).toBeEnabled();
  });

  it("falls back to a walk-in title and last order id when linked orders are missing", () => {
    render(
      <BillingRail
        bills={[
          {
            ...bills[0],
            billId: "bill-003",
            lastOrderId: "order-777",
            orderIds: [],
            tableId: null
          }
        ]}
        tableNameById={new Map()}
        busyBillId={null}
        onFinalize={vi.fn(async () => {})}
        onPay={vi.fn()}
      />
    );

    expect(screen.getByText("Walk-in bill")).toBeInTheDocument();
    expect(screen.getByText("1 linked order")).toBeInTheDocument();
  });
});
