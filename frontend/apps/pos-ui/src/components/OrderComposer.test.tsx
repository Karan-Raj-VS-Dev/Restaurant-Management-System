import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { OrderComposer } from "./OrderComposer";
import type { MenuItem, OrderLine, TableRecord } from "@restaurant/api";

const occupiedTable = {
  tableId: "table-01",
  tableNumber: "T-01",
  displayName: "Window 01",
  propertyId: "krusty-krab",
  floorName: "Main floor",
  sectionName: "Dining",
  capacity: 4,
  status: "OCCUPIED",
  waiterId: "emp-01",
  cleanerId: null,
  currentPartySize: 2,
  reservationPartySize: null,
  reservationTime: null,
  pendingStatus: null,
  pendingStatusAt: null,
  sessionId: "session-01",
  orderId: "order-01",
  customerId: null
} as TableRecord;

const availableTable = {
  ...occupiedTable,
  status: "AVAILABLE",
  waiterId: null,
  currentPartySize: null,
  orderId: null
} as TableRecord;

const cart = [
  { itemId: "item-001", itemName: "Margherita Pizza", quantity: 2 },
  { itemId: "item-002", itemName: "Pasta Alfredo", quantity: 1 }
] satisfies OrderLine[];

const menuById = new Map<string, MenuItem>([
  [
    "item-001",
    {
      itemId: "item-001",
      propertyId: "krusty-krab",
      name: "Margherita Pizza",
      categoryId: "cat-01",
      categoryName: "Italian dishes",
      price: 299,
      available: true,
      recipe: []
    }
  ],
  [
    "item-002",
    {
      itemId: "item-002",
      propertyId: "krusty-krab",
      name: "Pasta Alfredo",
      categoryId: "cat-01",
      categoryName: "Italian dishes",
      price: 249,
      available: true,
      recipe: []
    }
  ]
]);

describe("OrderComposer", () => {
  it("blocks submission for a table that is not occupied and explains why", () => {
    render(
      <OrderComposer
        waiterName="John Walker"
        selectedTable={availableTable}
        cart={cart}
        menuById={menuById}
        onIncrement={vi.fn()}
        onDecrement={vi.fn()}
        onSubmit={vi.fn()}
        busy={false}
        note={null}
      />
    );

    expect(screen.getByRole("button", { name: "Send to kitchen" })).toBeDisabled();
    expect(screen.getByText("Occupy this table before sending dishes to the kitchen.")).toBeInTheDocument();
  });

  it("renders the cart total, alerts, and allows submission when the table is occupied", () => {
    const onSubmit = vi.fn();

    render(
      <OrderComposer
        waiterName="John Walker"
        selectedTable={occupiedTable}
        cart={cart}
        menuById={menuById}
        onIncrement={vi.fn()}
        onDecrement={vi.fn()}
        onSubmit={onSubmit}
        busy={false}
        note="Ready for kitchen handoff."
        alerts={["Only 1 Pasta Alfredo can be served right now."]}
      />
    );

    expect(screen.getByText("Rs 847")).toBeInTheDocument();
    expect(screen.getByText("Ready for kitchen handoff.")).toBeInTheDocument();
    expect(screen.getByText("Only 1 Pasta Alfredo can be served right now.")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Send to kitchen" }));
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });

  it("keeps submission disabled until a server is selected for an occupied table", () => {
    render(
      <OrderComposer
        waiterName={null}
        selectedTable={occupiedTable}
        cart={cart}
        menuById={menuById}
        onIncrement={vi.fn()}
        onDecrement={vi.fn()}
        onSubmit={vi.fn()}
        busy={false}
        note={null}
      />
    );

    expect(screen.getByText("Choose a server")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Send to kitchen" })).toBeDisabled();
  });
});
