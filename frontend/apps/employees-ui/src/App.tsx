import { AppShell } from "@restaurant/ui";
import {
  buildScopedPropertyRecord,
  OperationalAccessGate,
  OperationalShellActions,
  useOperationalSessionBootstrap
} from "../../../packages/operations/src";
import { EmployeeManagementPage } from "../../restaurant-ui/src/components/EmployeeManagementPage";

export default function App() {
  const { session, bootLoading, bootError, dashboardContext } = useOperationalSessionBootstrap(
    "employees-ui",
    "Admin identities cannot enter the employee dashboard."
  );

  if (!session) {
    return (
      <OperationalAccessGate
        themeClassName="employees-theme"
        eyebrow="Restaurant Employees UI"
        title="Secure employee access"
        subtitle="The employee dashboard requires a valid restaurant user session from this platform."
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
        themeClassName="employees-theme"
        eyebrow="Restaurant Employees UI"
        title="Select a property first"
        subtitle="Choose a property from the restaurant application before opening employee management."
        bootLoading={false}
        bootError={null}
        dashboardContext={dashboardContext}
      />
    );
  }

  return (
    <div className="employees-theme">
      <AppShell
        eyebrow="Restaurant Employees UI"
        title="Employee Management"
        subtitle="Manage the property team, availability, shifts, and salary visibility from a dedicated workspace."
        actions={<OperationalShellActions appId="employees-ui" session={session} dashboardContext={dashboardContext} />}
      >
        <EmployeeManagementPage selectedProperty={selectedProperty} />
      </AppShell>
    </div>
  );
}
