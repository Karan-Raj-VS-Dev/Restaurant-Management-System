import {
  bootstrapAuthenticatedSession,
  changeOwnPassword,
  clearFrontendAuthState,
  clearRuntimeScope,
  confirmPasswordResetOtp,
  DEFAULT_PROPERTY_ID,
  isFrontendSessionExpiredError,
  loginWithCredentials,
  logoutFrontendSession,
  requestPasswordResetOtp,
  PRODUCT_SLUG,
  saveRuntimeScope,
  type AuthSession
} from "@restaurant/api";
import { AppShell, Button, StatusPill } from "@restaurant/ui";
import { useEffect, useState } from "react";
import { AdminPropertiesView } from "./components/AdminPropertiesView";
import { AdminUsersView } from "./components/AdminUsersView";
import { AuthFlow } from "./components/AuthFlow";

type AdminView = "users" | "properties";

export default function App() {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [view, setView] = useState<AdminView>("properties");
  const [bootLoading, setBootLoading] = useState(true);
  const [bootError, setBootError] = useState<string | null>(null);
  const [authBusy, setAuthBusy] = useState(false);
  const [authInfo, setAuthInfo] = useState<string | null>(null);
  const [pendingPasswordChange, setPendingPasswordChange] = useState<{
    username: string;
    currentPassword?: string;
  } | null>(null);

  const applyAuthenticatedSession = (nextSession: AuthSession) => {
    saveRuntimeScope({
      productSlug: PRODUCT_SLUG,
      tenantId: nextSession.tenantId,
      propertyId: nextSession.defaultPropertyId ?? nextSession.mappedPropertyIds[0] ?? DEFAULT_PROPERTY_ID
    });
    setSession(nextSession);
    setBootError(null);
    setAuthInfo(null);
    setPendingPasswordChange(null);
  };

  useEffect(() => {
    let active = true;

    const bootstrap = async () => {
      setBootLoading(true);
      try {
        const result = await bootstrapAuthenticatedSession("admin-ui");
        if (!active) {
          return;
        }
        if (result.session && !result.session.adminUser) {
          clearFrontendAuthState("admin-ui");
          setSession(null);
          setPendingPasswordChange(null);
          setBootError("Operational users must sign in through the restaurant application, not the admin console.");
          return;
        }

        if (result.session) {
          if (result.session.mustChangePassword) {
            setSession(null);
            setPendingPasswordChange({ username: result.session.username });
            setBootError(null);
            setAuthInfo("Update the temporary password before entering the admin console.");
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
          if (isFrontendSessionExpiredError(caughtError)) {
            setBootError(null);
            setAuthInfo(caughtError.message);
          } else {
            setBootError(caughtError instanceof Error ? caughtError.message : "Unable to start the admin sign-in flow.");
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
      <div className="admin-theme">
        <AuthFlow
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
              if (!nextSession.adminUser) {
                await logoutFrontendSession("admin-ui");
                setBootError("Operational users must sign in through the restaurant application, not the admin console.");
                return;
              }
              if (nextSession.mustChangePassword) {
                setPendingPasswordChange({ username, currentPassword: password });
                setSession(null);
                setAuthInfo("Update the temporary password before entering the admin console.");
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
              await logoutFrontendSession("admin-ui");
              setPendingPasswordChange(null);
              setSession(null);
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

  return (
    <div className="admin-theme">
      <AppShell
        eyebrow="Superuser Control"
        title="Admin control center"
        subtitle="This page is separate from the restaurant application. Use it only for tenant property creation and employee or user access management."
        actions={
          <>
            <StatusPill tone="info">{session.fullName}</StatusPill>
            <StatusPill tone="warning">{session.tenantId}</StatusPill>
            <Button
              variant="ghost"
              onClick={() => {
                clearFrontendAuthState("admin-ui");
                clearRuntimeScope();
                setSession(null);
                setView("properties");
                void logoutFrontendSession("admin-ui", "http://127.0.0.1:5175");
              }}
            >
              Logout
            </Button>
          </>
        }
      >
        <div className="admin-dashboard-stack">
          <div className="admin-users-actions">
            <Button variant={view === "properties" ? "primary" : "ghost"} onClick={() => setView("properties")}>
              Properties
            </Button>
            <Button variant={view === "users" ? "primary" : "ghost"} onClick={() => setView("users")}>
              Employees / Users
            </Button>
          </div>
          {view === "properties" ? <AdminPropertiesView /> : <AdminUsersView actorUsername={session.username} />}
        </div>
      </AppShell>
    </div>
  );
}
