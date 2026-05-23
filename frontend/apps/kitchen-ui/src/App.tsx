import { acceptKitchenTicket, buildKitchenTicketDetails, loadKitchenSnapshot, markOrderReadyToServe, readyKitchenTicket, type AuthSession, type KitchenTicketDetail } from "@restaurant/api";
import { DashboardContext, OperationalAccessGate, OperationalShellActions, useOperationalSessionBootstrap } from "../../../packages/operations/src";
import { AppShell, Button, LivePulse, SectionCard, StatCard, StatusPill, usePollingResource } from "@restaurant/ui";
import { useMemo, useState } from "react";
import { StockHealthPanel } from "./components/StockHealthPanel";
import { TicketLane } from "./components/TicketLane";

export default function App() {
  const { session, bootLoading, bootError, dashboardContext } = useOperationalSessionBootstrap(
    "kitchen-ui",
    "Admin identities cannot enter the kitchen dashboard."
  );

  if (!session) {
    return (
      <OperationalAccessGate
        themeClassName="kitchen-theme"
        eyebrow="React Kitchen UI"
        title="Secure kitchen access"
        subtitle="The kitchen board requires a valid restaurant user session from this platform."
        bootLoading={bootLoading}
        bootError={bootError}
        dashboardContext={dashboardContext}
      />
    );
  }

  return <AuthenticatedKitchenDashboard session={session} dashboardContext={dashboardContext} />;
}

function AuthenticatedKitchenDashboard(props: { session: AuthSession; dashboardContext: DashboardContext }) {
  const { data, loading, refreshing, error, lastUpdated, refresh } = usePollingResource(loadKitchenSnapshot, 4000);
  const [busyTicketId, setBusyTicketId] = useState<string | null>(null);
  const [selectedCookByTicketId, setSelectedCookByTicketId] = useState<Record<string, string>>({});
  const cookOptions = useMemo(
    () =>
      (data?.employees ?? [])
        .filter((employee) => employee.role === "COOK" && (employee.employmentStatus ?? "ACTIVE") === "ACTIVE")
        .sort((left, right) => left.name.localeCompare(right.name))
        .map((employee) => ({ employeeId: employee.employeeId, name: employee.name })),
    [data?.employees]
  );

  const handleAccept = async (ticket: KitchenTicketDetail, cookId: string | null) => {
    setBusyTicketId(ticket.ticketId);
    try {
      await acceptKitchenTicket(ticket.ticketId, cookId);
      await refresh();
    } finally {
      setBusyTicketId(null);
    }
  };

  const handleReady = async (ticket: KitchenTicketDetail, cookId: string | null) => {
    setBusyTicketId(ticket.ticketId);
    try {
      await readyKitchenTicket(ticket.ticketId, cookId);
      await markOrderReadyToServe(ticket.orderId);
      await refresh();
    } finally {
      setBusyTicketId(null);
    }
  };

  const ticketDetails = useMemo(
    () => buildKitchenTicketDetails(data?.tickets ?? [], data?.orders ?? [], data?.tables ?? [], data?.employees ?? []),
    [data?.employees, data?.orders, data?.tables, data?.tickets]
  );
  const received = ticketDetails.filter((ticket) => ticket.status === "RECEIVED");
  const active = ticketDetails.filter((ticket) => ticket.status === "ACCEPTED" || ticket.status === "PREPARING");
  const ready = ticketDetails.filter((ticket) => ticket.status === "READY");
  const completed = [...ticketDetails]
    .filter((ticket) => ticket.status === "SERVED")
    .sort((left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime());

  return (
    <div className="kitchen-theme">
      <AppShell
        eyebrow="React Kitchen UI"
        title="Expedite Board"
        subtitle="A live-prep console for kitchen tickets, ingredient health, and service pressure across the floor."
        actions={<OperationalShellActions appId="kitchen-ui" session={props.session} dashboardContext={props.dashboardContext} />}
      >
        <div className="kitchen-toolbar">
          <LivePulse label={refreshing ? "Refreshing board" : "Board live"} lastUpdated={lastUpdated} />
          <Button variant="ghost" onClick={() => void refresh()}>
            Refresh now
          </Button>
        </div>

        <div className="kitchen-stats-grid">
          <StatCard label="Tickets received" value={String(received.length)} hint="Fresh queue waiting for acceptance" tone="neutral" />
          <StatCard label="In prep" value={String(active.length)} hint="Cooks actively working the line" tone="cool" />
          <StatCard label="Ready for pass" value={String(ready.length)} hint="Waiting for pickup and service" tone="warm" />
          <StatCard label="Orders today" value={String(data?.dailyInsight.totalOrdersToday ?? 0)} hint={`Gross Rs ${data?.dailyInsight.grossSalesToday ?? 0}`} tone="alert" />
        </div>

        <div className="kitchen-layout">
          <SectionCard title="Kitchen lanes" subtitle="Tickets move left to right as the team accepts and finishes work.">
            <div className="kitchen-lanes-grid">
              <TicketLane
                title="Received"
                tone="info"
                tickets={received}
                cookOptions={cookOptions}
                selectedCookByTicketId={selectedCookByTicketId}
                onCookChange={(ticketId, cookId) => setSelectedCookByTicketId((current) => ({ ...current, [ticketId]: cookId }))}
                onAccept={handleAccept}
                onReady={handleReady}
                busyTicketId={busyTicketId}
                urgencyMode="received"
              />
              <TicketLane
                title="Preparing"
                tone="warning"
                tickets={active}
                cookOptions={cookOptions}
                selectedCookByTicketId={selectedCookByTicketId}
                onCookChange={(ticketId, cookId) => setSelectedCookByTicketId((current) => ({ ...current, [ticketId]: cookId }))}
                onAccept={handleAccept}
                onReady={handleReady}
                busyTicketId={busyTicketId}
              />
              <TicketLane
                title="Ready"
                tone="success"
                tickets={ready}
                cookOptions={cookOptions}
                selectedCookByTicketId={selectedCookByTicketId}
                onCookChange={(ticketId, cookId) => setSelectedCookByTicketId((current) => ({ ...current, [ticketId]: cookId }))}
                onAccept={handleAccept}
                onReady={handleReady}
                busyTicketId={busyTicketId}
              />
            </div>
          </SectionCard>

          <div className="kitchen-right-column">
            <SectionCard
              title="Ready for pickup"
              subtitle="Use this pass view to match ready dishes with the correct table and server."
              action={<StatusPill tone="success">{ready.length} waiting</StatusPill>}
            >
              {ready.length === 0 ? (
                <p className="kitchen-inline-note">Chef-ready dishes will appear here with their table and server details.</p>
              ) : (
                <div className="kitchen-pickup-stack">
                  {ready.map((entry) => (
                    <article key={entry.ticketId} className="kitchen-pickup-card">
                      <div className="kitchen-pickup-head">
                        <div>
                          <h3>{entry.tableName}</h3>
                          <p>
                            {entry.tableNumber} · {entry.waiterName}
                          </p>
                        </div>
                        <StatusPill tone="success">Ready</StatusPill>
                      </div>
                      {entry.items.length === 0 ? (
                        <p className="kitchen-inline-note">Dish details are syncing from the order.</p>
                      ) : (
                        <ul className="kitchen-pickup-items">
                          {entry.items.map((item) => (
                            <li key={`${entry.ticketId}-${item.itemId}`}>{item.quantity}× {item.itemName}</li>
                          ))}
                        </ul>
                      )}
                      <p className="kitchen-pickup-chef">Chef {entry.cookName}</p>
                    </article>
                  ))}
                </div>
              )}
            </SectionCard>

            <SectionCard
              title="Completed orders"
              subtitle="Served dishes land here so the kitchen can confirm what has already left the pass."
              action={<StatusPill tone="neutral">{completed.length} completed</StatusPill>}
            >
              {completed.length === 0 ? (
                <p className="kitchen-inline-note">Completed orders will appear here after service marks the ready dishes as served.</p>
              ) : (
                <div className="kitchen-pickup-stack">
                  {completed.map((entry) => (
                    <article key={entry.ticketId} className="kitchen-pickup-card kitchen-pickup-card-completed">
                      <div className="kitchen-pickup-head">
                        <div>
                          <h3>{entry.tableName}</h3>
                          <p>
                            {entry.tableNumber} · {entry.waiterName}
                          </p>
                        </div>
                        <StatusPill tone="neutral">Served</StatusPill>
                      </div>
                      {entry.items.length === 0 ? (
                        <p className="kitchen-inline-note">Dish details are syncing from the order.</p>
                      ) : (
                        <ul className="kitchen-pickup-items">
                          {entry.items.map((item) => (
                            <li key={`${entry.ticketId}-${item.itemId}`}>{item.quantity}× {item.itemName}</li>
                          ))}
                        </ul>
                      )}
                      <p className="kitchen-pickup-chef">Chef {entry.cookName}</p>
                      <p className="kitchen-pickup-timestamp">
                        Served at {new Date(entry.updatedAt).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })}
                      </p>
                    </article>
                  ))}
                </div>
              )}
            </SectionCard>

            <SectionCard title="Stock health" subtitle="Live ingredient stock from the inventory service, including low-threshold and over-capacity alerts.">
              {loading && !data ? <p className="kitchen-inline-note">Loading kitchen view...</p> : <StockHealthPanel stock={data?.stock ?? []} />}
              {error ? <p className="kitchen-error">Kitchen live services are partially unavailable. {error}</p> : null}
            </SectionCard>
          </div>
        </div>
      </AppShell>
    </div>
  );
}
