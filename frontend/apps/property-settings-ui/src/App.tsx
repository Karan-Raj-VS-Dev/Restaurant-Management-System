import { AppShell } from "@restaurant/ui";
import {
  buildScopedPropertyRecord,
  OperationalAccessGate,
  OperationalShellActions,
  useOperationalSessionBootstrap
} from "../../../packages/operations/src";
import { PropertySettingsPage } from "../../restaurant-ui/src/components/PropertySettingsPage";

export default function App() {
  const { session, bootLoading, bootError, dashboardContext } = useOperationalSessionBootstrap(
    "property-settings-ui",
    "Admin identities cannot enter property settings."
  );

  if (!session) {
    return (
      <OperationalAccessGate
        themeClassName="property-settings-theme"
        eyebrow="Restaurant Property Settings UI"
        title="Secure property settings access"
        subtitle="The property settings dashboard requires a valid restaurant user session from this platform."
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
        themeClassName="property-settings-theme"
        eyebrow="Restaurant Property Settings UI"
        title="Select a property first"
        subtitle="Choose a property from the restaurant application before opening property settings."
        bootLoading={false}
        bootError={null}
        dashboardContext={dashboardContext}
      />
    );
  }

  return (
    <div className="property-settings-theme">
      <AppShell
        eyebrow="Restaurant Property Settings UI"
        title="Property Settings"
        subtitle="Configure operational structures, recipes, ingredients, taxes, templates, and table layouts in a dedicated workspace."
        actions={<OperationalShellActions appId="property-settings-ui" session={session} dashboardContext={dashboardContext} />}
      >
        <PropertySettingsPage selectedProperty={selectedProperty} />
      </AppShell>
    </div>
  );
}
