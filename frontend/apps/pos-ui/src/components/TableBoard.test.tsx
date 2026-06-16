import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { TableBoard } from "./TableBoard";
import type { TableRecord } from "@restaurant/api";

const waiterNameById = new Map([["emp-01", "John Walker"]]);
const cleanerNameById = new Map([["emp-99", "Pradeep"]]);

const occupiedTable = {
  tableId: "table-01",
  tableNumber: "T-01",
  displayName: "Main Table",
  propertyId: "krusty-krab",
  floorName: "Main floor",
  sectionName: "Dining",
  capacity: 10,
  status: "OCCUPIED",
  waiterId: "emp-01",
  cleanerId: "emp-99",
  currentPartySize: 4,
  reservationPartySize: null,
  reservationTime: null,
  pendingStatus: null,
  pendingStatusAt: null,
  sessionId: "session-01",
  orderId: "order-01",
  customerId: null
} as TableRecord;

describe("TableBoard", () => {
  it("disables occupy for already occupied tables and shows the assigned people", () => {
    render(
      <TableBoard
        tables={[occupiedTable]}
        selectedTable={occupiedTable}
        selectedTableId={occupiedTable.tableId}
        onSelect={vi.fn()}
        floorOptions={["Main floor"]}
        selectedFloorName="Main floor"
        onFloorChange={vi.fn()}
        sectionOptions={["Dining"]}
        selectedSectionName="Dining"
        onSectionChange={vi.fn()}
        waiterOptions={[{ employeeId: "emp-01", name: "John Walker" }]}
        selectedWaiterId="emp-01"
        onWaiterChange={vi.fn()}
        cleanerOptions={[{ employeeId: "emp-99", name: "Pradeep" }]}
        selectedCleanerId="emp-99"
        onCleanerChange={vi.fn()}
        partySize={4}
        onPartySizeChange={vi.fn()}
        reservationPartySize={1}
        onReservationPartySizeChange={vi.fn()}
        reservationTime=""
        onReservationTimeChange={vi.fn()}
        waiterNameById={waiterNameById}
        cleanerNameById={cleanerNameById}
        statusMessage={null}
        statusMessageFading={false}
        reservationOverridePending={false}
        busyStatusChange={false}
        onOccupyNow={vi.fn(async () => true)}
        onReserveTable={vi.fn(async () => true)}
        onScheduleNeedsCleaning={vi.fn(async () => true)}
        onMarkNeedsCleaningNow={vi.fn(async () => true)}
        onScheduleCleanerReady={vi.fn(async () => true)}
        onMarkAvailableNow={vi.fn(async () => true)}
      />
    );

    expect(screen.getByRole("button", { name: "Occupy table" })).toBeDisabled();
    expect(screen.getByText("Server selected for this table: John Walker.")).toBeInTheDocument();
    expect(screen.getByText("Cleaner selected for this zone: Pradeep.")).toBeInTheDocument();
  });

  it("shows the pending timer for a cleaner cycle", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-06-15T10:00:00Z"));
    const needsCleaningTable = {
      ...occupiedTable,
      tableId: "table-02",
      tableNumber: "T-02",
      displayName: "Window 02",
      status: "NEEDS_CLEANING",
      pendingStatus: "AVAILABLE",
      pendingStatusAt: new Date(Date.now() + 4 * 60 * 1000).toISOString()
    } as TableRecord;

    render(
      <TableBoard
        tables={[needsCleaningTable]}
        selectedTable={needsCleaningTable}
        selectedTableId={needsCleaningTable.tableId}
        onSelect={vi.fn()}
        floorOptions={["Main floor"]}
        selectedFloorName="Main floor"
        onFloorChange={vi.fn()}
        sectionOptions={["Dining"]}
        selectedSectionName="Dining"
        onSectionChange={vi.fn()}
        waiterOptions={[{ employeeId: "emp-01", name: "John Walker" }]}
        selectedWaiterId="emp-01"
        onWaiterChange={vi.fn()}
        cleanerOptions={[{ employeeId: "emp-99", name: "Pradeep" }]}
        selectedCleanerId="emp-99"
        onCleanerChange={vi.fn()}
        partySize={4}
        onPartySizeChange={vi.fn()}
        reservationPartySize={1}
        onReservationPartySizeChange={vi.fn()}
        reservationTime=""
        onReservationTimeChange={vi.fn()}
        waiterNameById={waiterNameById}
        cleanerNameById={cleanerNameById}
        statusMessage="Cleaner assigned"
        statusMessageFading={false}
        reservationOverridePending={false}
        busyStatusChange={false}
        onOccupyNow={vi.fn(async () => true)}
        onReserveTable={vi.fn(async () => true)}
        onScheduleNeedsCleaning={vi.fn(async () => true)}
        onMarkNeedsCleaningNow={vi.fn(async () => true)}
        onScheduleCleanerReady={vi.fn(async () => true)}
        onMarkAvailableNow={vi.fn(async () => true)}
      />
    );

    expect(screen.getByText("Cleaner cycle in progress")).toBeInTheDocument();
    expect(screen.getByText("4m")).toBeInTheDocument();
    expect(screen.getByText("Cleaner assigned")).toBeInTheDocument();

    vi.useRealTimers();
  });

  it("opens the reserve modal when the reserve action is clicked", () => {
    render(
      <TableBoard
        tables={[occupiedTable]}
        selectedTable={occupiedTable}
        selectedTableId={occupiedTable.tableId}
        onSelect={vi.fn()}
        floorOptions={["Main floor"]}
        selectedFloorName="Main floor"
        onFloorChange={vi.fn()}
        sectionOptions={["Dining"]}
        selectedSectionName="Dining"
        onSectionChange={vi.fn()}
        waiterOptions={[{ employeeId: "emp-01", name: "John Walker" }]}
        selectedWaiterId="emp-01"
        onWaiterChange={vi.fn()}
        cleanerOptions={[{ employeeId: "emp-99", name: "Pradeep" }]}
        selectedCleanerId="emp-99"
        onCleanerChange={vi.fn()}
        partySize={4}
        onPartySizeChange={vi.fn()}
        reservationPartySize={1}
        onReservationPartySizeChange={vi.fn()}
        reservationTime=""
        onReservationTimeChange={vi.fn()}
        waiterNameById={waiterNameById}
        cleanerNameById={cleanerNameById}
        statusMessage={null}
        statusMessageFading={false}
        reservationOverridePending={false}
        busyStatusChange={false}
        onOccupyNow={vi.fn(async () => true)}
        onReserveTable={vi.fn(async () => true)}
        onScheduleNeedsCleaning={vi.fn(async () => true)}
        onMarkNeedsCleaningNow={vi.fn(async () => true)}
        onScheduleCleanerReady={vi.fn(async () => true)}
        onMarkAvailableNow={vi.fn(async () => true)}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: "Reserve table" }));
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(screen.getByText("Use reservation only when you need to hold the table for a future booking.")).toBeInTheDocument();
  });

  it("shows the zone warning when no waiters are available for the selected floor and section", () => {
    render(
      <TableBoard
        tables={[occupiedTable]}
        selectedTable={occupiedTable}
        selectedTableId={occupiedTable.tableId}
        onSelect={vi.fn()}
        floorOptions={["Main floor"]}
        selectedFloorName="Main floor"
        onFloorChange={vi.fn()}
        sectionOptions={["Dining"]}
        selectedSectionName="Dining"
        onSectionChange={vi.fn()}
        waiterOptions={[]}
        selectedWaiterId=""
        onWaiterChange={vi.fn()}
        cleanerOptions={[{ employeeId: "emp-99", name: "Pradeep" }]}
        selectedCleanerId="emp-99"
        onCleanerChange={vi.fn()}
        partySize={4}
        onPartySizeChange={vi.fn()}
        reservationPartySize={1}
        onReservationPartySizeChange={vi.fn()}
        reservationTime=""
        onReservationTimeChange={vi.fn()}
        waiterNameById={waiterNameById}
        cleanerNameById={cleanerNameById}
        statusMessage={null}
        statusMessageFading={false}
        reservationOverridePending={false}
        busyStatusChange={false}
        onOccupyNow={vi.fn(async () => true)}
        onReserveTable={vi.fn(async () => true)}
        onScheduleNeedsCleaning={vi.fn(async () => true)}
        onMarkNeedsCleaningNow={vi.fn(async () => true)}
        onScheduleCleanerReady={vi.fn(async () => true)}
        onMarkAvailableNow={vi.fn(async () => true)}
      />
    );

    expect(
      screen.getByText("No available servers are assigned to this floor and section yet. Update the area-section assignment or employee availability first.")
    ).toBeInTheDocument();
  });
});
