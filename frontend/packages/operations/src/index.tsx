import {
  bootstrapAuthenticatedSession,
  clearFrontendAuthState,
  clearRuntimeScope,
  clearStoredRestaurantSelection,
  logoutFrontendSession,
  PRODUCT_SLUG,
  saveRuntimeScope,
  type AuthSession,
  type FrontendAppId,
  type PropertyRecord
} from "@restaurant/api";
import { AppShell, Button, SectionCard, WorkspaceIdentityBadges } from "@restaurant/ui";
import { useEffect, useState, type ReactNode } from "react";

export interface DashboardContext {
  propertyName: string | null;
  propertyId: string | null;
  tenantId: string | null;
  autologin: boolean;
}

export function useOperationalSessionBootstrap(
  appId: FrontendAppId,
  adminBlockedMessage = "Admin identities cannot enter this dashboard."
) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [bootLoading, setBootLoading] = useState(true);
  const [bootError, setBootError] = useState<string | null>(null);
  const dashboardContext = readDashboardContext();

  useEffect(() => {
    if (dashboardContext.tenantId && dashboardContext.propertyId) {
      saveRuntimeScope({
        productSlug: PRODUCT_SLUG,
        tenantId: dashboardContext.tenantId,
        propertyId: dashboardContext.propertyId
      });
    }
  }, [dashboardContext.propertyId, dashboardContext.tenantId]);

  useEffect(() => {
    let active = true;

    const bootstrap = async () => {
      setBootLoading(true);
      try {
        const result = await bootstrapAuthenticatedSession(appId, {
          allowDashboardHandoff: dashboardContext.autologin
        });
        if (!active) {
          return;
        }
        if (result.session?.adminUser) {
          clearFrontendAuthState(appId);
          setBootError(adminBlockedMessage);
          setSession(null);
          return;
        }
        setSession(result.session);
        setBootError(null);
        if (dashboardContext.autologin) {
          clearDashboardAutologinFlag();
        }
      } catch (caughtError) {
        if (active) {
          setBootError(caughtError instanceof Error ? caughtError.message : "Unable to start dashboard authentication.");
          setSession(null);
        }
      } finally {
        if (active) {
          setBootLoading(false);
        }
      }
    };

    void bootstrap();
    return () => {
      active = false;
    };
  }, [adminBlockedMessage, appId]);

  return { session, bootLoading, bootError, dashboardContext };
}

export function OperationalAccessGate(props: {
  themeClassName: string;
  eyebrow: string;
  title: string;
  subtitle: string;
  bootLoading: boolean;
  bootError: string | null;
  dashboardContext: DashboardContext;
}) {
  return (
    <div className={props.themeClassName}>
      <AppShell eyebrow={props.eyebrow} title={props.title} subtitle={props.subtitle}>
        <SectionCard title="Sign in required" subtitle="Launch this dashboard from the restaurant application after signing in there first.">
          {props.bootError ? <p className="operations-error">{props.bootError}</p> : null}
          {!props.dashboardContext.propertyId ? (
            <p className="operations-inline-note">Choose a property from the restaurant application before opening this dashboard.</p>
          ) : null}
          <div className="operations-toolbar">
            <Button disabled>{props.bootLoading ? "Checking session..." : "Restaurant sign-in required"}</Button>
            <Button variant="ghost" onClick={() => (window.location.href = buildRestaurantAppUrl())}>
              Back to restaurant app
            </Button>
          </div>
        </SectionCard>
      </AppShell>
    </div>
  );
}

export function OperationalShellActions(props: {
  appId: FrontendAppId;
  session: AuthSession;
  dashboardContext: DashboardContext;
  backLabel?: string;
  extraActions?: ReactNode;
}) {
  return (
    <>
      <WorkspaceIdentityBadges userName={props.session.fullName} propertyName={props.dashboardContext.propertyName} />
      <Button variant="ghost" onClick={() => (window.location.href = buildRestaurantAppUrl())}>
        {props.backLabel ?? "Back to dashboards"}
      </Button>
      {props.dashboardContext.propertyName ? (
        <Button
          variant="ghost"
          onClick={() => {
            clearStoredRestaurantSelection();
            clearRuntimeScope();
            window.location.href = buildRestaurantAppUrl("change-property");
          }}
        >
          Change property
        </Button>
      ) : null}
      {props.extraActions}
      <Button
        variant="ghost"
        onClick={() => {
          clearFrontendAuthState(props.appId);
          clearStoredRestaurantSelection();
          clearRuntimeScope();
          void logoutFrontendSession(props.appId, buildRestaurantAppUrl("logout"));
        }}
      >
        Logout
      </Button>
    </>
  );
}

export function buildDashboardHref(baseUrl: string, session: AuthSession, selectedProperty: PropertyRecord) {
  const params = new URLSearchParams({
    autologin: "1",
    tenantId: session.tenantId,
    propertyId: selectedProperty.propertyId,
    propertyName: selectedProperty.name
  });
  return `${baseUrl}?${params.toString()}`;
}

export function buildRestaurantAppUrl(action?: "change-property" | "logout") {
  const url = new URL("http://127.0.0.1:5176");
  if (action) {
    url.searchParams.set("action", action);
  }
  return url.toString();
}

export function buildScopedPropertyRecord(session: AuthSession, dashboardContext: DashboardContext): PropertyRecord | null {
  if (!dashboardContext.propertyId || !dashboardContext.propertyName) {
    return null;
  }

  return {
    tenantId: dashboardContext.tenantId ?? session.tenantId,
    productSlug: PRODUCT_SLUG,
    propertyId: dashboardContext.propertyId,
    name: dashboardContext.propertyName,
    city: "",
    state: null,
    country: "",
    addressLine: null,
    latitude: null,
    longitude: null,
    status: "ACTIVE"
  };
}

export function readDashboardContext(): DashboardContext {
  if (typeof window === "undefined") {
    return {
      propertyName: null,
      propertyId: null,
      tenantId: null,
      autologin: false
    };
  }

  const params = new URLSearchParams(window.location.search);
  return {
    propertyName: params.get("propertyName"),
    propertyId: params.get("propertyId"),
    tenantId: params.get("tenantId"),
    autologin: params.get("autologin") === "1"
  };
}

function clearDashboardAutologinFlag() {
  if (typeof window === "undefined") {
    return;
  }

  const url = new URL(window.location.href);
  if (!url.searchParams.has("autologin")) {
    return;
  }
  url.searchParams.delete("autologin");
  window.history.replaceState({}, "", url.toString());
}
