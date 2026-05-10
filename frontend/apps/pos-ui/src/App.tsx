import {
  assignTableToWaiter,
  AreaSectionSettingRecord,
  BillRecord,
  buildAvailabilityMap,
  createDineInOrder,
  finalizeBill,
  isApiRequestError,
  loadPosSnapshot,
  processBillPayment,
  type TableStatus,
  updateTableStatus,
  type AuthSession,
  type EmployeeRecord,
  type MenuItem,
  type OrderLine
} from "@restaurant/api";
import { DashboardContext, OperationalAccessGate, OperationalShellActions, useOperationalSessionBootstrap } from "../../../packages/operations/src";
import { AppShell, Button, LivePulse, SectionCard, StatCard, StatusPill, usePollingResource } from "@restaurant/ui";
import { useEffect, useMemo, useState } from "react";
import { BillingRail } from "./components/BillingRail";
import { MenuCatalog } from "./components/MenuCatalog";
import { OrderComposer } from "./components/OrderComposer";
import { TableBoard } from "./components/TableBoard";

export default function App() {
  const { session, bootLoading, bootError, dashboardContext } = useOperationalSessionBootstrap(
    "pos-ui",
    "Admin identities cannot enter the diner dashboard."
  );

  if (!session) {
    return (
      <OperationalAccessGate
        themeClassName="pos-theme"
        eyebrow="React POS UI"
        title="Secure diner access"
        subtitle="The diner dashboard requires a valid restaurant user session from this platform."
        bootLoading={bootLoading}
        bootError={bootError}
        dashboardContext={dashboardContext}
      />
    );
  }

  return <AuthenticatedPosDashboard session={session} dashboardContext={dashboardContext} />;
}

function AuthenticatedPosDashboard(props: { session: AuthSession; dashboardContext: DashboardContext }) {
  const { data, loading, refreshing, error, lastUpdated, refresh } = usePollingResource(loadPosSnapshot, 5000);
  const [selectedTableId, setSelectedTableId] = useState<string | null>(null);
  const [selectedFloorName, setSelectedFloorName] = useState("");
  const [selectedSectionName, setSelectedSectionName] = useState("");
  const [selectedWaiterId, setSelectedWaiterId] = useState("");
  const [selectedCleanerId, setSelectedCleanerId] = useState("");
  const [partySize, setPartySize] = useState(1);
  const [reservationPartySize, setReservationPartySize] = useState(1);
  const [reservationTime, setReservationTime] = useState("");
  const [cart, setCart] = useState<OrderLine[]>([]);
  const [orderNote, setOrderNote] = useState<string | null>(null);
  const [tableStatusMessage, setTableStatusMessage] = useState<string | null>(null);
  const [reservationOverridePending, setReservationOverridePending] = useState(false);
  const [busyOrder, setBusyOrder] = useState(false);
  const [busyBillId, setBusyBillId] = useState<string | null>(null);
  const [busyTableStatus, setBusyTableStatus] = useState(false);

  const availability = buildAvailabilityMap(data?.availability ?? []);
  const menuById = new Map((data?.menu ?? []).map((item) => [item.itemId, item]));
  const floorOptions = useMemo(
    () => Array.from(new Set((data?.tables ?? []).map((table) => table.floorName).filter((value): value is string => Boolean(value)))).sort((left, right) => left.localeCompare(right)),
    [data?.tables]
  );
  const sectionOptions = useMemo(
    () =>
      Array.from(
        new Set(
          (data?.tables ?? [])
            .filter((table) => !selectedFloorName || table.floorName === selectedFloorName)
            .map((table) => table.sectionName)
            .filter((value): value is string => Boolean(value))
        )
      ).sort((left, right) => left.localeCompare(right)),
    [data?.tables, selectedFloorName]
  );
  const filteredTables = useMemo(
    () =>
      (data?.tables ?? []).filter(
        (table) =>
          (!selectedFloorName || table.floorName === selectedFloorName) &&
          (!selectedSectionName || table.sectionName === selectedSectionName)
      ),
    [data?.tables, selectedFloorName, selectedSectionName]
  );
  const selectedTable = filteredTables.find((table) => table.tableId === selectedTableId);
  const selectedAreaSection = useMemo(
    () =>
      (data?.areaSections ?? []).find(
        (record) => record.floorName === selectedFloorName && record.sectionName === selectedSectionName
      ) ?? null,
    [data?.areaSections, selectedFloorName, selectedSectionName]
  );
  const waiterNameById = useMemo(
    () => new Map((data?.employees ?? []).map((employee) => [employee.employeeId, employee.name])),
    [data?.employees]
  );
  const cleanerNameById = useMemo(
    () => new Map((data?.employees ?? []).map((employee) => [employee.employeeId, employee.name])),
    [data?.employees]
  );
  const availableWaiters = useMemo(
    () => resolveAvailableWaiters(data?.employees ?? [], selectedAreaSection, selectedTableId ? (data?.tables ?? []).find((table) => table.tableId === selectedTableId)?.waiterId ?? null : null),
    [data?.employees, data?.tables, selectedAreaSection, selectedTableId]
  );
  const availableCleaners = useMemo(
    () => resolveAvailableCleaners(data?.employees ?? [], selectedAreaSection, selectedTable?.cleanerId ?? null),
    [data?.employees, selectedAreaSection, selectedTable?.cleanerId]
  );
  const selectedWaiter = availableWaiters.find((waiter) => waiter.employeeId === selectedWaiterId) ?? null;

  useEffect(() => {
    if (!selectedFloorName && floorOptions.length > 0) {
      setSelectedFloorName(floorOptions[0]);
    } else if (selectedFloorName && !floorOptions.includes(selectedFloorName)) {
      setSelectedFloorName(floorOptions[0] ?? "");
    }
  }, [floorOptions, selectedFloorName]);

  useEffect(() => {
    if (!selectedSectionName && sectionOptions.length > 0) {
      setSelectedSectionName(sectionOptions[0]);
    } else if (selectedSectionName && !sectionOptions.includes(selectedSectionName)) {
      setSelectedSectionName(sectionOptions[0] ?? "");
    }
  }, [sectionOptions, selectedSectionName]);

  useEffect(() => {
    if (!selectedTableId && filteredTables.length > 0) {
      setSelectedTableId(filteredTables[0].tableId);
      return;
    }
    if (selectedTableId && !filteredTables.some((table) => table.tableId === selectedTableId)) {
      setSelectedTableId(filteredTables[0]?.tableId ?? null);
    }
  }, [filteredTables, selectedTableId]);

  useEffect(() => {
    if (selectedTable?.waiterId && availableWaiters.some((waiter) => waiter.employeeId === selectedTable.waiterId)) {
      setSelectedWaiterId(selectedTable.waiterId);
      return;
    }
    if (selectedWaiterId && availableWaiters.some((waiter) => waiter.employeeId === selectedWaiterId)) {
      return;
    }
    setSelectedWaiterId(availableWaiters[0]?.employeeId ?? "");
  }, [availableWaiters, selectedTable?.waiterId, selectedWaiterId]);

  useEffect(() => {
    if (selectedTable?.cleanerId && availableCleaners.some((cleaner) => cleaner.employeeId === selectedTable.cleanerId)) {
      setSelectedCleanerId(selectedTable.cleanerId);
      return;
    }
    if (selectedCleanerId && availableCleaners.some((cleaner) => cleaner.employeeId === selectedCleanerId)) {
      return;
    }
    setSelectedCleanerId(availableCleaners[0]?.employeeId ?? "");
  }, [availableCleaners, selectedCleanerId, selectedTable?.cleanerId]);

  useEffect(() => {
    if (!selectedTable) {
      return;
    }
    setTableStatusMessage(null);
    setReservationOverridePending(false);
    setPartySize(selectedTable.currentPartySize ?? 1);
    setReservationPartySize(selectedTable.reservationPartySize ?? 1);
    setReservationTime(selectedTable.reservationTime ? toDateTimeLocalInput(selectedTable.reservationTime) : "");
  }, [selectedTable?.tableId, selectedTable?.currentPartySize, selectedTable?.reservationPartySize, selectedTable?.reservationTime]);

  const addToCart = (item: MenuItem) => {
    setCart((current) => {
      const existing = current.find((line) => line.itemId === item.itemId);
      if (!existing) {
        return [...current, { itemId: item.itemId, itemName: item.name, quantity: 1 }];
      }
      return current.map((line) => (line.itemId === item.itemId ? { ...line, quantity: line.quantity + 1 } : line));
    });
  };

  const increment = (itemId: string) => {
    const item = data?.menu.find((entry) => entry.itemId === itemId);
    if (item) {
      addToCart(item);
    }
  };

  const decrement = (itemId: string) => {
    setCart((current) =>
      current
        .map((line) => (line.itemId === itemId ? { ...line, quantity: Math.max(0, line.quantity - 1) } : line))
        .filter((line) => line.quantity > 0)
    );
  };

  const applyTableStatus = async (
    targetStatus: TableStatus,
    options: {
      immediate?: boolean;
      overrideReservationWarning?: boolean;
      silentSuccess?: boolean;
    } = {}
  ) => {
    if (!selectedTable) {
      setTableStatusMessage("Choose a table before updating its status.");
      return null;
    }

    setBusyTableStatus(true);
    try {
      const updated = await updateTableStatus(selectedTable.tableId, {
        targetStatus,
        partySize,
        waiterId: selectedWaiterId || null,
        cleanerId: selectedCleanerId || null,
        reservationPartySize,
        reservationTime: reservationTime ? new Date(reservationTime).toISOString() : null,
        immediate: options.immediate,
        overrideReservationWarning: options.overrideReservationWarning
      });
      if (!options.silentSuccess) {
        setTableStatusMessage(statusSuccessMessage(updated, options.immediate ?? false, waiterNameById, cleanerNameById));
      }
      setReservationOverridePending(false);
      await refresh();
      return updated;
    } catch (caughtError) {
      if (isApiRequestError(caughtError)) {
        if (caughtError.status === 409 && targetStatus === "OCCUPIED" && !options.overrideReservationWarning) {
          setReservationOverridePending(true);
          setTableStatusMessage(`${caughtError.message} Use "Occupy now" again after confirming the override.`);
        } else {
          setReservationOverridePending(false);
          setTableStatusMessage(caughtError.message);
        }
      } else {
        setReservationOverridePending(false);
        setTableStatusMessage(caughtError instanceof Error ? caughtError.message : "Unable to update the table status.");
      }
      return null;
    } finally {
      setBusyTableStatus(false);
    }
  };

  const handleSubmitOrder = async () => {
    if (!selectedTableId || !selectedTable || !selectedWaiterId || cart.length === 0) {
      setOrderNote("Choose a floor, section, table, and server before sending the order.");
      return;
    }
    if (selectedTable.status === "UNAVAILABLE") {
      setOrderNote("This table is unavailable in property settings and cannot accept a floor order.");
      return;
    }

    setBusyOrder(true);
    try {
      if (selectedTable.status !== "OCCUPIED" || selectedTable.waiterId !== selectedWaiterId || !selectedTable.currentPartySize) {
        const occupiedTable = await applyTableStatus("OCCUPIED", { immediate: true, silentSuccess: true });
        if (!occupiedTable) {
          return;
        }
      } else {
        await assignTableToWaiter({
          tableId: selectedTableId,
          propertyId: selectedTable.propertyId,
          capacity: selectedTable.currentPartySize ?? selectedTable.capacity,
          waiterId: selectedWaiterId
        });
      }
      const order = await createDineInOrder({
        tableId: selectedTableId,
        waiterId: selectedWaiterId,
        items: cart
      });
      setCart([]);
      setOrderNote(`Order ${order.orderId} has been created and pushed into the kitchen flow.`);
      await refresh();
    } catch (caughtError) {
      setOrderNote(caughtError instanceof Error ? caughtError.message : "Failed to create order.");
    } finally {
      setBusyOrder(false);
    }
  };

  const handleFinalizeBill = async (billId: string) => {
    setBusyBillId(billId);
    try {
      await finalizeBill(billId);
      await refresh();
    } finally {
      setBusyBillId(null);
    }
  };

  const handlePayBill = async (bill: BillRecord) => {
    setBusyBillId(bill.billId);
    try {
      await processBillPayment(bill);
      if (bill.tableId) {
        await updateTableStatus(bill.tableId, {
          targetStatus: "NEEDS_CLEANING",
          immediate: false
        });
        setTableStatusMessage("Payment received. This table will move to needs cleaning in 2 minutes unless you change it earlier.");
      }
      await refresh();
    } finally {
      setBusyBillId(null);
    }
  };

  return (
    <div className="pos-theme">
      <AppShell
        eyebrow="React POS UI"
        title="Floor Control"
        subtitle="A component-driven service console for tables, live menu availability, open orders, and bill collection."
        actions={<OperationalShellActions appId="pos-ui" session={props.session} dashboardContext={props.dashboardContext} />}
      >
        <div className="pos-toolbar">
          <LivePulse label={refreshing ? "Syncing services" : "Live polling active"} lastUpdated={lastUpdated} />
          <Button variant="ghost" onClick={() => void refresh()}>
            Refresh now
          </Button>
        </div>

        <div className="pos-stats-grid">
          <StatCard label="Total orders today" value={String(data?.dailyInsight.totalOrdersToday ?? 0)} hint={`Busiest ${data?.dailyInsight.busiestTableId ?? "table"}`} tone="warm" />
          <StatCard label="Gross sales" value={`Rs ${data?.dailyInsight.grossSalesToday ?? 0}`} hint={`Top server ${data?.dailyInsight.topServerId ?? "pending"}`} tone="cool" />
          <StatCard label="Active bills" value={String(data?.bills.length ?? 0)} hint="Draft and finalized bills" />
          <StatCard label="Available tables" value={String((data?.tables ?? []).filter((table) => table.status === "AVAILABLE").length)} hint="Ready for new walk-ins" tone="neutral" />
        </div>

        <div className="pos-layout">
          <div className="pos-left-column">
            <SectionCard
              title="Table board"
              subtitle="Select a table to start or continue a live dine-in order."
              action={
                <StatusPill tone="info">
                  {selectedAreaSection ? `${availableWaiters.length} server${availableWaiters.length === 1 ? "" : "s"} on zone` : "Choose zone"}
                </StatusPill>
              }
            >
              {loading && !data ? (
                <p className="pos-inline-note">Loading table board...</p>
              ) : (
                <TableBoard
                  tables={filteredTables}
                  selectedTable={selectedTable ?? null}
                  selectedTableId={selectedTableId}
                  onSelect={setSelectedTableId}
                  floorOptions={floorOptions}
                  selectedFloorName={selectedFloorName}
                  onFloorChange={(value) => {
                    setSelectedFloorName(value);
                    setSelectedSectionName("");
                    setSelectedTableId(null);
                    setOrderNote(null);
                    setTableStatusMessage(null);
                  }}
                  sectionOptions={sectionOptions}
                  selectedSectionName={selectedSectionName}
                  onSectionChange={(value) => {
                    setSelectedSectionName(value);
                    setSelectedTableId(null);
                    setOrderNote(null);
                    setTableStatusMessage(null);
                  }}
                  waiterOptions={availableWaiters.map((waiter) => ({ employeeId: waiter.employeeId, name: waiter.name }))}
                  selectedWaiterId={selectedWaiterId}
                  onWaiterChange={setSelectedWaiterId}
                  cleanerOptions={availableCleaners.map((cleaner) => ({ employeeId: cleaner.employeeId, name: cleaner.name }))}
                  selectedCleanerId={selectedCleanerId}
                  onCleanerChange={setSelectedCleanerId}
                  partySize={partySize}
                  onPartySizeChange={setPartySize}
                  reservationPartySize={reservationPartySize}
                  onReservationPartySizeChange={setReservationPartySize}
                  reservationTime={reservationTime}
                  onReservationTimeChange={setReservationTime}
                  waiterNameById={waiterNameById}
                  cleanerNameById={cleanerNameById}
                  statusMessage={tableStatusMessage}
                  reservationOverridePending={reservationOverridePending}
                  busyStatusChange={busyTableStatus}
                  onOccupyNow={async (overrideReservationWarning) =>
                    !!(await applyTableStatus("OCCUPIED", {
                      immediate: true,
                      overrideReservationWarning: overrideReservationWarning ?? reservationOverridePending
                    }))
                  }
                  onReserveTable={async () => !!(await applyTableStatus("RESERVED", { immediate: true }))}
                  onScheduleNeedsCleaning={async () => !!(await applyTableStatus("NEEDS_CLEANING", { immediate: false }))}
                  onMarkNeedsCleaningNow={async () => !!(await applyTableStatus("NEEDS_CLEANING", { immediate: true }))}
                  onScheduleCleanerReady={async () => !!(await applyTableStatus("AVAILABLE", { immediate: false }))}
                  onMarkAvailableNow={async () => !!(await applyTableStatus("AVAILABLE", { immediate: true }))}
                />
              )}
            </SectionCard>

            <SectionCard title="Live bills" subtitle="Draft bills are created from the order flow and can be finalized or collected here.">
              <BillingRail bills={data?.bills ?? []} busyBillId={busyBillId} onFinalize={handleFinalizeBill} onPay={handlePayBill} />
            </SectionCard>
          </div>

          <div className="pos-middle-column">
            <SectionCard title="Stock-aware menu" subtitle="Only dishes with enough ingredient support should move through to order creation.">
              <MenuCatalog items={data?.menu ?? []} availability={availability} onAdd={addToCart} />
            </SectionCard>
          </div>

          <div className="pos-right-column">
            <SectionCard title="Order composer" subtitle="Build the order as components, then emit it into the event-driven backend.">
              <OrderComposer
                waiterName={selectedWaiter?.name ?? null}
                selectedTable={selectedTable}
                cart={cart}
                menuById={menuById}
                onIncrement={increment}
                onDecrement={decrement}
                onSubmit={handleSubmitOrder}
                busy={busyOrder}
                note={orderNote}
              />
            </SectionCard>

            <SectionCard title="Operational notes" subtitle="This panel stays useful even before every service has full persistence.">
              <div className="pos-notes-panel">
                <p>
                  <strong>Kitchen flow:</strong> orders are created, then submitted to kitchen, which triggers ticket creation and stock reservation.
                </p>
                <p>
                  <strong>Billing flow:</strong> draft bills appear automatically after order creation, then payment emits review-request events.
                </p>
                {error ? <p className="pos-error">Some live services are currently unavailable. {error}</p> : null}
              </div>
            </SectionCard>
          </div>
        </div>
      </AppShell>
    </div>
  );
}

function resolveAvailableWaiters(
  employees: EmployeeRecord[],
  areaSection: AreaSectionSettingRecord | null,
  assignedWaiterId: string | null
) {
  const assignedNames = new Set((areaSection?.waiterNames ?? []).map(normalizeText));
  return employees
    .filter((employee) => employee.role === "WAITER")
    .filter((employee) => (employee.employmentStatus ?? "ACTIVE") === "ACTIVE")
    .filter((employee) => employee.available || employee.employeeId === assignedWaiterId)
    .filter((employee) => assignedNames.size === 0 || assignedNames.has(normalizeText(employee.name)))
    .sort((left, right) => left.name.localeCompare(right.name));
}

function resolveAvailableCleaners(
  employees: EmployeeRecord[],
  areaSection: AreaSectionSettingRecord | null,
  assignedCleanerId: string | null
) {
  const assignedNames = new Set((areaSection?.cleanerNames ?? []).map(normalizeText));
  return employees
    .filter((employee) => employee.role === "CLEANER")
    .filter((employee) => (employee.employmentStatus ?? "ACTIVE") === "ACTIVE")
    .filter((employee) => employee.available || employee.employeeId === assignedCleanerId)
    .filter((employee) => assignedNames.size === 0 || assignedNames.has(normalizeText(employee.name)))
    .sort((left, right) => left.name.localeCompare(right.name));
}

function toDateTimeLocalInput(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hour}:${minute}`;
}

function statusSuccessMessage(
  table: { displayName: string; pendingStatus: TableStatus | null; waiterId: string | null; cleanerId: string | null },
  immediate: boolean,
  waiterNameById: Map<string, string>,
  cleanerNameById: Map<string, string>
) {
  if (table.pendingStatus === "NEEDS_CLEANING" && !immediate) {
    return `${table.displayName} will move to needs cleaning in 2 minutes after the payment cool-down.`;
  }
  if (table.pendingStatus === "AVAILABLE" && !immediate) {
    const cleanerName = table.cleanerId ? cleanerNameById.get(table.cleanerId) ?? table.cleanerId : "the assigned cleaner";
    return `${cleanerName} has been assigned. ${table.displayName} will return to available in 5 minutes.`;
  }
  if (table.waiterId) {
    const waiterName = waiterNameById.get(table.waiterId) ?? table.waiterId;
    return `${table.displayName} is now occupied and assigned to ${waiterName}.`;
  }
  return `${table.displayName} status has been updated.`;
}

function normalizeText(value: string) {
  return value.trim().toLowerCase();
}
