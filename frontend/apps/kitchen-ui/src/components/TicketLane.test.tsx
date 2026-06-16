import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { TicketLane } from "./TicketLane";
import type { KitchenTicketDetail } from "@restaurant/api";

const cookOptions = [
  { employeeId: "chef-anu", name: "Anu" },
  { employeeId: "chef-bob", name: "Bob" }
];

const receivedTickets: KitchenTicketDetail[] = [
  {
    ticketId: "ticket-urgent",
    orderId: "order-urgent",
    tableId: "table-01",
    tableName: "Window 01",
    tableNumber: "T-01",
    waiterId: "emp-01",
    waiterName: "Neha",
    cookId: "cook-pending",
    cookName: "Chef pending",
    items: [
      { itemId: "item-001", itemName: "Margherita Pizza", quantity: 1 }
    ],
    status: "RECEIVED",
    updatedAt: "2026-06-14T11:56:00.000Z",
    cancellationReason: null,
    reusedForTicketId: null
  },
  {
    ticketId: "ticket-steady",
    orderId: "order-steady",
    tableId: "table-02",
    tableName: "Main Table",
    tableNumber: "T-02",
    waiterId: "emp-02",
    waiterName: "John Walker",
    cookId: "cook-pending",
    cookName: "Chef pending",
    items: [
      { itemId: "item-002", itemName: "Pasta Alfredo", quantity: 1 },
      { itemId: "item-003", itemName: "Garlic Bread", quantity: 1 }
    ],
    status: "RECEIVED",
    updatedAt: "2026-06-14T11:58:00.000Z",
    cancellationReason: null,
    reusedForTicketId: null
  }
];

describe("TicketLane", () => {
  it("shows an empty state when there are no tickets", () => {
    render(
      <TicketLane
        title="Received"
        tone="info"
        tickets={[]}
        cookOptions={cookOptions}
        selectedCookByTicketId={{}}
        onCookChange={vi.fn()}
        onAccept={vi.fn()}
        onReady={vi.fn()}
        busyTicketId={null}
      />
    );

    expect(screen.getByText("No tickets here")).toBeInTheDocument();
  });

  it("sorts received tickets by urgency and renders chef selectors", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-06-14T12:00:00.000Z"));

    render(
      <TicketLane
        title="Received"
        tone="info"
        tickets={receivedTickets}
        cookOptions={cookOptions}
        selectedCookByTicketId={{ "ticket-urgent": "chef-anu", "ticket-steady": "chef-bob" }}
        onCookChange={vi.fn()}
        onAccept={vi.fn()}
        onReady={vi.fn()}
        busyTicketId={null}
        urgencyMode="received"
      />
    );

    const headings = screen.getAllByRole("heading", { level: 3 }).map((heading) => heading.textContent);
    expect(headings).toContain("1× Margherita Pizza");
    expect(headings).toContain("1× Pasta Alfredo + 1 more");
    expect(headings.indexOf("1× Margherita Pizza")).toBeLessThan(headings.indexOf("1× Pasta Alfredo + 1 more"));
    expect(screen.getAllByRole("combobox")).toHaveLength(2);
    expect(screen.getByText("1m 00s")).toBeInTheDocument();

    vi.useRealTimers();
  });

  it("hides the chef selector once a ticket has moved beyond received", () => {
    render(
      <TicketLane
        title="Ready"
        tone="success"
        tickets={[
          {
            ...receivedTickets[0],
            status: "READY",
            cookId: "chef-anu",
            cookName: "Anu"
          }
        ]}
        cookOptions={cookOptions}
        selectedCookByTicketId={{}}
        onCookChange={vi.fn()}
        onAccept={vi.fn()}
        onReady={vi.fn()}
        busyTicketId={null}
      />
    );

    expect(screen.queryByRole("combobox")).not.toBeInTheDocument();
    expect(screen.getByText("Chef Anu")).toBeInTheDocument();
  });

  it("shows overdue urgency once a received ticket passes the five minute window", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-06-14T12:06:01.000Z"));

    render(
      <TicketLane
        title="Received"
        tone="info"
        tickets={[receivedTickets[0]]}
        cookOptions={cookOptions}
        selectedCookByTicketId={{ "ticket-urgent": "chef-anu" }}
        onCookChange={vi.fn()}
        onAccept={vi.fn()}
        onReady={vi.fn()}
        busyTicketId={null}
        urgencyMode="received"
      />
    );

    expect(screen.getByText("Highest priority")).toBeInTheDocument();
    expect(screen.getByText("Overdue")).toBeInTheDocument();

    vi.useRealTimers();
  });
});
