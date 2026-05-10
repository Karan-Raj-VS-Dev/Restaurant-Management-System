import { acceptKitchenTicket, loadKitchenSnapshot, readyKitchenTicket, type AuthSession } from "@restaurant/api";
import { DashboardContext, OperationalAccessGate, OperationalShellActions, useOperationalSessionBootstrap } from "../../../packages/operations/src";
import { AppShell, Button, LivePulse, SectionCard, StatCard, usePollingResource } from "@restaurant/ui";
import { useState } from "react";
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

  const handleAccept = async (ticketId: string) => {
    setBusyTicketId(ticketId);
    try {
      await acceptKitchenTicket(ticketId);
      await refresh();
    } finally {
      setBusyTicketId(null);
    }
  };

  const handleReady = async (ticketId: string) => {
    setBusyTicketId(ticketId);
    try {
      await readyKitchenTicket(ticketId);
      await refresh();
    } finally {
      setBusyTicketId(null);
    }
  };

  const received = (data?.tickets ?? []).filter((ticket) => ticket.status === "RECEIVED");
  const active = (data?.tickets ?? []).filter((ticket) => ticket.status === "ACCEPTED" || ticket.status === "PREPARING");
  const ready = (data?.tickets ?? []).filter((ticket) => ticket.status === "READY");

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
              <TicketLane title="Received" tone="info" tickets={received} onAccept={handleAccept} onReady={handleReady} busyTicketId={busyTicketId} />
              <TicketLane title="Preparing" tone="warning" tickets={active} onAccept={handleAccept} onReady={handleReady} busyTicketId={busyTicketId} />
              <TicketLane title="Ready" tone="success" tickets={ready} onAccept={handleAccept} onReady={handleReady} busyTicketId={busyTicketId} />
            </div>
          </SectionCard>

          <SectionCard title="Stock health" subtitle="Live ingredient stock from the inventory service, including low-threshold and over-capacity alerts.">
            {loading && !data ? <p className="kitchen-inline-note">Loading kitchen view...</p> : <StockHealthPanel stock={data?.stock ?? []} />}
            {error ? <p className="kitchen-error">Kitchen live services are partially unavailable. {error}</p> : null}
          </SectionCard>
        </div>
      </AppShell>
    </div>
  );
}
