import { AppShell } from "@restaurant/ui";
import {
  buildScopedPropertyRecord,
  OperationalAccessGate,
  OperationalShellActions,
  useOperationalSessionBootstrap
} from "../../../packages/operations/src";
import { ReportsDashboardPage } from "../../restaurant-ui/src/components/ReportsDashboardPage";

export default function App() {
  const { session, bootLoading, bootError, dashboardContext } = useOperationalSessionBootstrap(
    "reports-ui",
    "Admin identities cannot enter the reports dashboard."
  );

  if (!session) {
    return (
      <OperationalAccessGate
        themeClassName="reports-theme"
        eyebrow="Restaurant Reports UI"
        title="Secure reports access"
        subtitle="The reports dashboard requires a valid restaurant user session from this platform."
        bootLoading={bootLoading}
        bootError={bootError}
        dashboardContext={dashboardContext}
      />
    );
  }

  const selectedProperty = buildScopedPropertyRecord(session, dashboardContext);
  if (!selectedProperty) {
    return (
      <OperationalAccessGate
        themeClassName="reports-theme"
        eyebrow="Restaurant Reports UI"
        title="Select a property first"
        subtitle="Choose a property from the restaurant application before opening reports."
        bootLoading={false}
        bootError={null}
        dashboardContext={dashboardContext}
      />
    );
  }

  return (
    <div className="reports-theme">
      <AppShell
        eyebrow="Restaurant Reports UI"
        title="Operations Reports"
        subtitle="Review property-level sales, stock impact, and recent order flow from a dedicated reporting surface."
        actions={<OperationalShellActions appId="reports-ui" session={session} dashboardContext={dashboardContext} />}
      >
        <ReportsDashboardPage />
      </AppShell>
    </div>
  );
}
