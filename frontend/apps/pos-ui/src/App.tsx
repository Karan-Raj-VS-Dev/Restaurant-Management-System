import {
  assignTableToWaiter,
  AreaSectionSettingRecord,
  BillRecord,
  buildPickupQueue,
  buildAvailabilityMap,
  createDineInOrder,
  finalizeBill,
  isApiRequestError,
  loadPosSnapshot,
  markOrderServed,
  validateDineInOrder,
  type PaymentMethod,
  processBillPayment,
  serveKitchenTicket,
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
  const showTableStatusMessage = (message: string) => {
    setTableStatusMessageFading(false);
    setTableStatusMessage(message);
  };
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
  const [orderAlerts, setOrderAlerts] = useState<string[]>([]);
  const [tableStatusMessage, setTableStatusMessage] = useState<string | null>(null);
  const [tableStatusMessageFading, setTableStatusMessageFading] = useState(false);
  const [pendingTableBanner, setPendingTableBanner] = useState<{
    tableId: string;
    pendingStatus: "NEEDS_CLEANING" | "AVAILABLE";
    actorName: string | null;
  } | null>(null);
  const [reservationOverridePending, setReservationOverridePending] = useState(false);
  const [busyOrder, setBusyOrder] = useState(false);
  const [busyBillId, setBusyBillId] = useState<string | null>(null);
  const [busyPickupTicketId, setBusyPickupTicketId] = useState<string | null>(null);
  const [busyTableStatus, setBusyTableStatus] = useState(false);
  const [paymentBill, setPaymentBill] = useState<BillRecord | null>(null);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("UPI");
  const [paymentMessage, setPaymentMessage] = useState<string | null>(null);

  const availability = buildAvailabilityMap(data?.availability ?? []);
  const menuById = new Map((data?.menu ?? []).map((item) => [item.itemId, item]));
  const tableNameById = useMemo(
    () => new Map((data?.tables ?? []).map((table) => [table.tableId, table.displayName || table.tableNumber || table.tableId])),
    [data?.tables]
  );
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
  const readyPickupQueue = useMemo(
    () => buildPickupQueue(data?.tickets ?? [], data?.orders ?? [], data?.tables ?? [], data?.employees ?? []),
    [data?.employees, data?.orders, data?.tables, data?.tickets]
  );
  const liveBills = useMemo(() => (data?.bills ?? []).filter((bill) => bill.status !== "PAID"), [data?.bills]);
  const availableTableCount = useMemo(() => countReadyTables(data?.tables ?? []), [data?.tables]);

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
      setPendingTableBanner(null);
      return;
    }
    setTableStatusMessage(null);
    setTableStatusMessageFading(false);
    setPendingTableBanner(null);
    setReservationOverridePending(false);
  }, [selectedTable?.tableId]);

  useEffect(() => {
    if (!tableStatusMessage) {
      setTableStatusMessageFading(false);
      return;
    }

    setTableStatusMessageFading(false);
    const fadeTimer = window.setTimeout(() => setTableStatusMessageFading(true), 4500);
    const clearTimer = window.setTimeout(() => {
      setTableStatusMessage(null);
      setTableStatusMessageFading(false);
    }, 5300);
    return () => {
      window.clearTimeout(fadeTimer);
      window.clearTimeout(clearTimer);
    };
  }, [tableStatusMessage]);

  useEffect(() => {
    if (!selectedTable) {
      return;
    }
    setPartySize(selectedTable.currentPartySize ?? 1);
    setReservationPartySize(selectedTable.reservationPartySize ?? 1);
    setReservationTime(selectedTable.reservationTime ? toDateTimeLocalInput(selectedTable.reservationTime) : "");
  }, [selectedTable?.currentPartySize, selectedTable?.reservationPartySize, selectedTable?.reservationTime, selectedTable?.tableId]);

  useEffect(() => {
    if (!selectedTable || !pendingTableBanner || pendingTableBanner.tableId !== selectedTable.tableId) {
      return;
    }

    const syncPendingBanner = () => {
      if (selectedTable.pendingStatus !== pendingTableBanner.pendingStatus || !selectedTable.pendingStatusAt) {
        setPendingTableBanner(null);
        setTableStatusMessage(null);
        setTableStatusMessageFading(false);
        return;
      }

      const remainingMs = new Date(selectedTable.pendingStatusAt).getTime() - Date.now();
      if (remainingMs <= 0) {
        setTableStatusMessage(null);
        setTableStatusMessageFading(false);
        return;
      }

      setTableStatusMessage(
        pendingBannerMessage(
          selectedTable.displayName,
          pendingTableBanner.pendingStatus,
          remainingMs,
          pendingTableBanner.actorName
        )
      );
    };

    syncPendingBanner();
    const timer = window.setInterval(syncPendingBanner, 1000);
    return () => window.clearInterval(timer);
  }, [
    pendingTableBanner,
    selectedTable?.displayName,
    selectedTable?.pendingStatus,
    selectedTable?.pendingStatusAt,
    selectedTable?.tableId
  ]);

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
      showTableStatusMessage("Choose a table before updating its status.");
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
        showTableStatusMessage(statusSuccessMessage(updated, options.immediate ?? false, waiterNameById, cleanerNameById));
      }
      if (updated.pendingStatus === "AVAILABLE" && !options.immediate) {
        setPendingTableBanner({
          tableId: updated.tableId,
          pendingStatus: "AVAILABLE",
          actorName: updated.cleanerId ? cleanerNameById.get(updated.cleanerId) ?? updated.cleanerId : null
        });
      } else if (updated.pendingStatus === "NEEDS_CLEANING" && !options.immediate) {
        setPendingTableBanner({
          tableId: updated.tableId,
          pendingStatus: "NEEDS_CLEANING",
          actorName: null
        });
      } else {
        setPendingTableBanner(null);
      }
      setReservationOverridePending(false);
      await refresh();
      return updated;
    } catch (caughtError) {
      if (isApiRequestError(caughtError)) {
        if (caughtError.status === 409 && targetStatus === "OCCUPIED" && !options.overrideReservationWarning) {
          setReservationOverridePending(true);
          showTableStatusMessage(`${caughtError.message} Use "Occupy now" again after confirming the override.`);
        } else {
          setReservationOverridePending(false);
          setPendingTableBanner(null);
          showTableStatusMessage(caughtError.message);
        }
      } else {
        setReservationOverridePending(false);
        setPendingTableBanner(null);
        showTableStatusMessage(caughtError instanceof Error ? caughtError.message : "Unable to update the table status.");
      }
      return null;
    } finally {
      setBusyTableStatus(false);
    }
  };

  const handleSubmitOrder = async () => {
    if (!selectedTableId || !selectedTable || !selectedWaiterId || cart.length === 0) {
      setOrderNote("Choose a floor, section, table, and server before sending the order.");
      setOrderAlerts([]);
      return;
    }
    if (selectedTable.status === "UNAVAILABLE") {
      setOrderNote("This table is unavailable in property settings and cannot accept a floor order.");
      setOrderAlerts([]);
      return;
    }

    setBusyOrder(true);
    try {
      const validation = await validateDineInOrder(cart);
      if (!validation.valid) {
        setOrderNote("We can’t send this order yet because some dishes do not have enough live stock.");
        setOrderAlerts(validation.issues.map((issue) => issue.message));
        return;
      }
      setOrderAlerts([]);
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
      setOrderAlerts([]);
      await refresh();
    } catch (caughtError) {
      setOrderNote(caughtError instanceof Error ? caughtError.message : "Failed to create order.");
      setOrderAlerts([]);
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

  const handleOpenPaymentModal = (bill: BillRecord) => {
    setPaymentBill(bill);
    setPaymentMethod("UPI");
    setPaymentMessage(null);
  };

  const handleConfirmPayment = async () => {
    if (!paymentBill) {
      return;
    }

    setBusyBillId(paymentBill.billId);
    try {
      await processBillPayment(paymentBill, paymentMethod);
      if (paymentBill.tableId) {
        await updateTableStatus(paymentBill.tableId, {
          targetStatus: "NEEDS_CLEANING",
          immediate: false
        });
        setPendingTableBanner({
          tableId: paymentBill.tableId,
          pendingStatus: "NEEDS_CLEANING",
          actorName: null
        });
      }
      setPaymentBill(null);
      setPaymentMessage(null);
      await refresh();
    } catch (caughtError) {
      setPaymentMessage(caughtError instanceof Error ? caughtError.message : "Unable to collect the payment.");
    } finally {
      setBusyBillId(null);
    }
  };

  const handleServeReadyPickup = async (ticketId: string, orderId: string, tableName: string) => {
    setBusyPickupTicketId(ticketId);
    try {
      await serveKitchenTicket(ticketId);
      await markOrderServed(orderId);
      showTableStatusMessage(`${tableName} is marked served and cleared from the pickup queue.`);
      await refresh();
    } catch (caughtError) {
      showTableStatusMessage(caughtError instanceof Error ? caughtError.message : "Unable to mark this dish as served.");
    } finally {
      setBusyPickupTicketId(null);
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
          <StatCard label="Active bills" value={String(liveBills.length)} hint="Draft and finalized bills" />
          <StatCard label="Available tables" value={String(availableTableCount)} hint="Ready for new walk-ins" tone="neutral" />
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
                    setOrderAlerts([]);
                    setTableStatusMessage(null);
                    setTableStatusMessageFading(false);
                  }}
                  sectionOptions={sectionOptions}
                  selectedSectionName={selectedSectionName}
                  onSectionChange={(value) => {
                    setSelectedSectionName(value);
                    setSelectedTableId(null);
                    setOrderNote(null);
                    setOrderAlerts([]);
                    setTableStatusMessage(null);
                    setTableStatusMessageFading(false);
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
                  statusMessageFading={tableStatusMessageFading}
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

            <SectionCard
              title="Ready to pick and serve"
              subtitle="Kitchen-ready dishes appear here with the table and server they belong to."
              action={<StatusPill tone="success">{readyPickupQueue.length} ready</StatusPill>}
            >
              {readyPickupQueue.length === 0 ? (
                <p className="pos-inline-note">Once the chef marks dishes ready, they will appear here for pickup and service.</p>
              ) : (
                <div className="pos-pickup-stack">
                  {readyPickupQueue.map((entry) => (
                    <article key={entry.ticketId} className="pos-pickup-card">
                      <div className="pos-pickup-head">
                        <div>
                          <h3>{entry.tableName}</h3>
                          <p>
                            {entry.tableNumber} · {entry.waiterName}
                          </p>
                        </div>
                        <StatusPill tone="success">Ready</StatusPill>
                      </div>
                      <p className="pos-pickup-items">
                        {entry.items.length === 0
                          ? "Dish details are syncing from the order."
                          : entry.items.map((item) => `${item.quantity}× ${item.itemName}`).join(", ")}
                      </p>
                      <div className="pos-pickup-footer">
                        <span>Ready at {new Date(entry.updatedAt).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })}</span>
                        <Button
                          variant="secondary"
                          disabled={busyPickupTicketId === entry.ticketId}
                          onClick={() => void handleServeReadyPickup(entry.ticketId, entry.orderId, entry.tableName)}
                        >
                          Mark served
                        </Button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </SectionCard>

            <SectionCard title="Live bills" subtitle="Draft bills are created from the order flow and can be finalized or collected here.">
              <BillingRail bills={liveBills} tableNameById={tableNameById} busyBillId={busyBillId} onFinalize={handleFinalizeBill} onPay={handleOpenPaymentModal} />
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
                alerts={orderAlerts}
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

        {paymentBill ? (
          <div className="pos-table-modal-overlay" role="presentation">
            <div className="pos-payment-modal" role="dialog" aria-modal="true" aria-labelledby="pos-payment-modal-title">
              <div className="pos-table-modal-header">
                <div>
                  <h3 id="pos-payment-modal-title">Collect payment</h3>
                  <p>
                    {(paymentBill.tableId ? `${tableNameById.get(paymentBill.tableId) ?? "Table"} bill` : "Walk-in bill")} · Rs {paymentBill.total}
                  </p>
                </div>
                <Button
                  variant="ghost"
                  onClick={() => {
                    setPaymentBill(null);
                    setPaymentMessage(null);
                  }}
                >
                  Close
                </Button>
              </div>
              <div className="pos-table-modal-content">
                <div className="pos-payment-methods">
                  {(["UPI", "CARD", "CASH", "WALLET"] as PaymentMethod[]).map((method) => (
                    <button
                      key={method}
                      type="button"
                      className={`pos-payment-method ${paymentMethod === method ? "selected" : ""}`}
                      onClick={() => setPaymentMethod(method)}
                    >
                      {method}
                    </button>
                  ))}
                </div>
                <div className="pos-table-modal-note">
                  <strong>Customer payment mode</strong>
                  <span>Choose how the customer settled this bill, then collect it through the payment service.</span>
                </div>
                {paymentMessage ? <div className="pos-status-banner">{paymentMessage}</div> : null}
                <div className="pos-table-modal-actions">
                  <Button
                    variant="ghost"
                    onClick={() => {
                      setPaymentBill(null);
                      setPaymentMessage(null);
                    }}
                  >
                    Cancel
                  </Button>
                  <Button disabled={busyBillId === paymentBill.billId} onClick={() => void handleConfirmPayment()}>
                    Confirm {paymentMethod}
                  </Button>
                </div>
              </div>
            </div>
          </div>
        ) : null}
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

function pendingBannerMessage(
  displayName: string,
  pendingStatus: "NEEDS_CLEANING" | "AVAILABLE",
  remainingMs: number,
  actorName: string | null
) {
  if (pendingStatus === "AVAILABLE") {
    const cleanerLead = actorName ? `${actorName} has been assigned. ` : "";
    return `${cleanerLead}${displayName} will return to available in ${formatRemaining(remainingMs)}.`;
  }
  return `${displayName} will move to needs cleaning in ${formatRemaining(remainingMs)}.`;
}

function formatRemaining(remainingMs: number) {
  const totalSeconds = Math.max(0, Math.ceil(remainingMs / 1000));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  if (minutes <= 0) {
    return `${seconds}s`;
  }
  if (seconds === 0) {
    return `${minutes}m`;
  }
  return `${minutes}m ${String(seconds).padStart(2, "0")}s`;
}

function normalizeText(value: string) {
  return value.trim().toLowerCase();
}

function countReadyTables(tables: Array<{ status: TableStatus; pendingStatus: TableStatus | null }>) {
  return tables.filter((table) => table.status === "AVAILABLE" && table.pendingStatus !== "NEEDS_CLEANING").length;
}
