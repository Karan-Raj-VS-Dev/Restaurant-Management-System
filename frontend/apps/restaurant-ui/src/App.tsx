import {
  bootstrapAuthenticatedSession,
  changeOwnPassword,
  clearFrontendAuthState,
  clearRuntimeScope,
  clearStoredRestaurantSelection,
  confirmPasswordResetOtp,
  isFrontendSessionExpiredError,
  loadStoredRestaurantProperty,
  loginWithCredentials,
  logoutFrontendSession,
  PRODUCT_SLUG,
  requestPasswordResetOtp,
  saveRuntimeScope,
  type AuthSession,
  type PropertyRecord
} from "@restaurant/api";
import { AppShell, Button, WorkspaceIdentityBadges } from "@restaurant/ui";
import { useEffect, useState } from "react";
import { RestaurantAuthFlow } from "./components/RestaurantAuthFlow";
import { RestaurantLandingPage } from "./components/RestaurantLandingPage";
import { RestaurantPropertySelectionPage } from "./components/RestaurantPropertySelectionPage";

export default function App() {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [selectedProperty, setSelectedProperty] = useState<PropertyRecord | null>(() => loadStoredRestaurantProperty());
  const [bootLoading, setBootLoading] = useState(true);
  const [bootError, setBootError] = useState<string | null>(null);
  const [authBusy, setAuthBusy] = useState(false);
  const [authInfo, setAuthInfo] = useState<string | null>(null);
  const [pendingPasswordChange, setPendingPasswordChange] = useState<{
    username: string;
    currentPassword?: string;
  } | null>(null);

  const applyAuthenticatedSession = (nextSession: AuthSession) => {
    setSession(nextSession);
    if (selectedProperty && !nextSession.mappedPropertyIds.includes(selectedProperty.propertyId)) {
      clearStoredRestaurantSelection();
      clearRuntimeScope();
      setSelectedProperty(null);
    }
    setBootError(null);
    setAuthInfo(null);
    setPendingPasswordChange(null);
  };

  useEffect(() => {
    if (typeof window === "undefined") {
      return;
    }

    const url = new URL(window.location.href);
    const action = url.searchParams.get("action");
    if (!action) {
      return;
    }

    if (action === "logout") {
      clearFrontendAuthState("restaurant-ui");
      clearRuntimeScope();
      clearStoredRestaurantSelection();
      setSession(null);
      setSelectedProperty(null);
    } else if (action === "change-property") {
      clearRuntimeScope();
      clearStoredRestaurantSelection();
      setSelectedProperty(null);
    }

    url.searchParams.delete("action");
    window.history.replaceState({}, "", url.toString());
  }, []);

  useEffect(() => {
    if (selectedProperty) {
      window.sessionStorage.setItem("restaurant.ui.selectedProperty", JSON.stringify(selectedProperty));
      return;
    }
    window.sessionStorage.removeItem("restaurant.ui.selectedProperty");
  }, [selectedProperty]);

  useEffect(() => {
    let active = true;

    const bootstrap = async () => {
      setBootLoading(true);
      try {
        const result = await bootstrapAuthenticatedSession("restaurant-ui");
        if (!active) {
          return;
        }
        if (result.session && result.session.adminUser) {
          clearFrontendAuthState("restaurant-ui");
          setSession(null);
          setPendingPasswordChange(null);
          setSelectedProperty(null);
          setBootError("Superuser accounts must sign in through the admin console. Create an operational user for restaurant access.");
          return;
        }

        if (result.session) {
          if (result.session.mustChangePassword) {
            setSession(null);
            setSelectedProperty(null);
            setPendingPasswordChange({ username: result.session.username });
            setBootError(null);
            setAuthInfo("Update the temporary password before entering the restaurant application.");
            return;
          }
          applyAuthenticatedSession(result.session);
          return;
        }

        setSession(null);
        setPendingPasswordChange(null);
        setBootError(null);
      } catch (caughtError) {
        if (active) {
          setSession(null);
          setPendingPasswordChange(null);
          setSelectedProperty(null);
          if (isFrontendSessionExpiredError(caughtError)) {
            setBootError(null);
            setAuthInfo(caughtError.message);
          } else {
            setBootError(caughtError instanceof Error ? caughtError.message : "Unable to start restaurant authentication.");
          }
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
  }, []);

  if (!session) {
    return (
      <div className="restaurant-theme">
        <RestaurantAuthFlow
          busy={bootLoading || authBusy}
          error={bootError}
          info={authInfo}
          forcePasswordChangeUsername={pendingPasswordChange?.username ?? null}
          initialUsername={pendingPasswordChange?.username}
          initialCurrentPassword={pendingPasswordChange?.currentPassword}
          onLogin={async (username, password) => {
            setAuthBusy(true);
            setBootError(null);
            setAuthInfo(null);
            try {
              const nextSession = await loginWithCredentials(username, password);
              if (nextSession.adminUser) {
                await logoutFrontendSession("restaurant-ui");
                setBootError("Superuser accounts must sign in through the admin console. Create an operational user for restaurant access.");
                return;
              }
              if (nextSession.mustChangePassword) {
                setSession(null);
                setSelectedProperty(null);
                setPendingPasswordChange({ username, currentPassword: password });
                setAuthInfo("Update the temporary password before entering the restaurant application.");
                return;
              }
              applyAuthenticatedSession(nextSession);
            } finally {
              setAuthBusy(false);
            }
          }}
          onRequestPasswordReset={async (identifier) => {
            setAuthBusy(true);
            setBootError(null);
            try {
              return await requestPasswordResetOtp(identifier);
            } finally {
              setAuthBusy(false);
            }
          }}
          onConfirmPasswordReset={async (args) => {
            setAuthBusy(true);
            setBootError(null);
            try {
              return await confirmPasswordResetOtp(args);
            } finally {
              setAuthBusy(false);
            }
          }}
          onChangePassword={async (args) => {
            setAuthBusy(true);
            setBootError(null);
            try {
              const result = await changeOwnPassword(args);
              await logoutFrontendSession("restaurant-ui");
              setPendingPasswordChange(null);
              setSession(null);
              setSelectedProperty(null);
              setAuthInfo(`${result.message} Please sign in with the new password.`);
              return result;
            } finally {
              setAuthBusy(false);
            }
          }}
        />
      </div>
    );
  }

  const selectProperty = (property: PropertyRecord) => {
    setSelectedProperty(property);
    saveRuntimeScope({
      productSlug: PRODUCT_SLUG,
      tenantId: session.tenantId,
      propertyId: property.propertyId
    });
  };

  const content = !selectedProperty ? (
    <RestaurantPropertySelectionPage session={session} onSelect={selectProperty} />
  ) : (
    <RestaurantLandingPage session={session} selectedProperty={selectedProperty} />
  );

  return (
    <div className="restaurant-theme">
      <AppShell
        eyebrow="Restaurant Application"
        title={selectedProperty ? `${selectedProperty.name} workspace` : "Choose a property"}
        subtitle="This operational SPA is separate from the admin console. Sign in with a created user, choose any mapped property, and then continue into the service dashboards."
        actions={
          <>
            <WorkspaceIdentityBadges userName={session.fullName} propertyName={selectedProperty?.name} />
            {selectedProperty ? (
              <Button
                variant="ghost"
                onClick={() => {
                  clearStoredRestaurantSelection();
                  clearRuntimeScope();
                  setSelectedProperty(null);
                }}
              >
                Change property
              </Button>
            ) : null}
            <Button
              variant="ghost"
              onClick={() => {
                clearFrontendAuthState("restaurant-ui");
                clearRuntimeScope();
                clearStoredRestaurantSelection();
                setSession(null);
                setSelectedProperty(null);
                void logoutFrontendSession("restaurant-ui", "http://127.0.0.1:5176");
              }}
            >
              Logout
            </Button>
          </>
        }
      >
        {content}
      </AppShell>
    </div>
  );
}
