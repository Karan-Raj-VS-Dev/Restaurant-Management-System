import { AppShell } from "@restaurant/ui";
import {
  buildScopedPropertyRecord,
  OperationalAccessGate,
  OperationalShellActions,
  useOperationalSessionBootstrap
} from "../../../packages/operations/src";
import { InventoryManagementPage } from "../../restaurant-ui/src/components/InventoryManagementPage";

export default function App() {
  const { session, bootLoading, bootError, dashboardContext } = useOperationalSessionBootstrap(
    "inventory-ui",
    "Admin identities cannot enter the inventory dashboard."
  );

  if (!session) {
    return (
      <OperationalAccessGate
        themeClassName="inventory-theme"
        eyebrow="Restaurant Inventory UI"
        title="Secure inventory access"
        subtitle="The inventory dashboard requires a valid restaurant user session from this platform."
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
        themeClassName="inventory-theme"
        eyebrow="Restaurant Inventory UI"
        title="Select a property first"
        subtitle="Choose a property from the restaurant application before opening inventory management."
        bootLoading={false}
        bootError={null}
        dashboardContext={dashboardContext}
      />
    );
  }

  return (
    <div className="inventory-theme">
      <AppShell
        eyebrow="Restaurant Inventory UI"
        title="Inventory Control"
        subtitle="Track live stock, thresholds, and bulk updates for the selected property."
        actions={<OperationalShellActions appId="inventory-ui" session={session} dashboardContext={dashboardContext} />}
      >
        <InventoryManagementPage selectedProperty={selectedProperty} />
      </AppShell>
    </div>
  );
}
