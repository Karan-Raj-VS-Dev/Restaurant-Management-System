import {
  type PasswordResetRequestResult,
  type PasswordUpdateResult
} from "@restaurant/api";
import { Button, SectionCard, StatusPill } from "@restaurant/ui";
import { useEffect, useMemo, useState } from "react";

type AuthView = "login" | "reset-request" | "reset-confirm" | "change-password";

interface RestaurantAuthFlowProps {
  busy: boolean;
  error: string | null;
  info: string | null;
  forcePasswordChangeUsername?: string | null;
  initialUsername?: string;
  initialCurrentPassword?: string;
  onLogin: (username: string, password: string) => Promise<void>;
  onRequestPasswordReset: (identifier: string) => Promise<PasswordResetRequestResult>;
  onConfirmPasswordReset: (args: { identifier: string; otp: string; newPassword: string }) => Promise<PasswordUpdateResult>;
  onChangePassword: (args: { username: string; currentPassword: string; newPassword: string }) => Promise<PasswordUpdateResult>;
}

export function RestaurantAuthFlow(props: RestaurantAuthFlowProps) {
  const [view, setView] = useState<AuthView>("login");
  const [username, setUsername] = useState(props.initialUsername ?? "");
  const [password, setPassword] = useState("");
  const [identifier, setIdentifier] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [currentPassword, setCurrentPassword] = useState(props.initialCurrentPassword ?? "");
  const [inlineInfo, setInlineInfo] = useState<string | null>(null);
  const [inlineError, setInlineError] = useState<string | null>(null);

  useEffect(() => {
    if (props.forcePasswordChangeUsername) {
      setView("change-password");
      setUsername(props.forcePasswordChangeUsername);
      if (props.initialCurrentPassword) {
        setCurrentPassword(props.initialCurrentPassword);
      }
      setInlineError(null);
      return;
    }
    setView((currentView) => (currentView === "change-password" ? "login" : currentView));
  }, [props.forcePasswordChangeUsername, props.initialCurrentPassword]);

  const message = useMemo(() => props.info ?? inlineInfo, [props.info, inlineInfo]);

  const getErrorMessage = (error: unknown) => (error instanceof Error ? error.message : "Unable to complete the request right now.");

  const submitLogin = async () => {
    setInlineInfo(null);
    setInlineError(null);
    try {
      await props.onLogin(username.trim(), password);
    } catch (error) {
      setInlineError(getErrorMessage(error));
    }
  };

  const submitResetRequest = async () => {
    setInlineError(null);
    try {
      const result = await props.onRequestPasswordReset(identifier.trim());
      const otpHint = result.devOtp ? ` OTP for local testing: ${result.devOtp}` : "";
      setInlineInfo(`${result.message}${otpHint}`);
      if (result.accountFound) {
        setView("reset-confirm");
      }
    } catch (error) {
      setInlineError(getErrorMessage(error));
    }
  };

  const submitResetConfirm = async () => {
    setInlineError(null);
    if (newPassword !== confirmPassword) {
      setInlineInfo("New password and confirm password must match.");
      return;
    }

    try {
      const result = await props.onConfirmPasswordReset({
        identifier: identifier.trim(),
        otp: otp.trim(),
        newPassword
      });
      setInlineInfo(result.message);
      setView("login");
      setPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setOtp("");
    } catch (error) {
      setInlineError(getErrorMessage(error));
    }
  };

  const submitPasswordChange = async () => {
    setInlineError(null);
    if (newPassword !== confirmPassword) {
      setInlineInfo("New password and confirm password must match.");
      return;
    }

    try {
      const result = await props.onChangePassword({
        username: username.trim(),
        currentPassword,
        newPassword
      });
      setInlineInfo(result.message);
      setPassword("");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (error) {
      setInlineError(getErrorMessage(error));
    }
  };

  return (
    <div className="admin-auth-layout">
      <div className="admin-auth-hero">
        <div className="admin-auth-badge">Restaurant Application</div>
        <h1>Restaurant operations start with the mapped property.</h1>
        <p>
          This is the operational user application. Sign in with the username and password created inside our platform, then
          choose one of the properties mapped to that user and continue into the dashboards.
        </p>
        <div className="admin-auth-feature-list">
          <StatusPill tone="success">Platform login</StatusPill>
          <StatusPill tone="info">Property selection</StatusPill>
          <StatusPill tone="info">Secure cookie APIs</StatusPill>
          <StatusPill tone="warning">Admin users blocked here</StatusPill>
        </div>
      </div>

      <SectionCard
        className="admin-auth-card"
        title="Restaurant sign in"
        subtitle="Use the operational username and password here. Forgot-password and first-login password change stay inside this app flow."
      >
        {inlineError ? <div className="admin-alert admin-alert-error">{inlineError}</div> : null}
        {props.error ? <div className="admin-alert admin-alert-error">{props.error}</div> : null}
        {message ? <div className="admin-alert admin-alert-info">{message}</div> : null}

        {view === "login" ? (
          <div className="admin-form">
            <label>
              Username
              <input className="admin-input" value={username} onChange={(event) => setUsername(event.target.value)} />
            </label>
            <label>
              Password
              <input
                className="admin-input"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </label>
            <div className="admin-form-actions admin-form-actions-split">
              <Button type="button" disabled={props.busy} onClick={() => void submitLogin()}>
                {props.busy ? "Signing in..." : "Login"}
              </Button>
              {!props.forcePasswordChangeUsername ? (
                <button
                  className="admin-inline-link admin-inline-link-end"
                  type="button"
                  onClick={() => {
                    setView("reset-request");
                    setInlineError(null);
                    setInlineInfo(null);
                  }}
                >
                  Forgot password
                </button>
              ) : null}
            </div>
          </div>
        ) : null}

        {view === "reset-request" ? (
          <div className="admin-form">
            <label>
              Email or phone number
              <input
                className="admin-input"
                value={identifier}
                onChange={(event) => setIdentifier(event.target.value)}
                placeholder="name@example.com or +91..."
              />
            </label>
            <div className="admin-form-actions">
              <Button type="button" disabled={props.busy} onClick={() => void submitResetRequest()}>
                {props.busy ? "Requesting..." : "Send OTP"}
              </Button>
              <Button
                type="button"
                variant="ghost"
                onClick={() => {
                  setView("login");
                  setInlineInfo(null);
                  setInlineError(null);
                }}
              >
                Back to login
              </Button>
            </div>
          </div>
        ) : null}

        {view === "reset-confirm" ? (
          <div className="admin-form">
            <label>
              OTP
              <input className="admin-input" value={otp} onChange={(event) => setOtp(event.target.value)} />
            </label>
            <div className="admin-form-grid">
              <label>
                New password
                <input
                  className="admin-input"
                  type="password"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                />
              </label>
              <label>
                Confirm password
                <input
                  className="admin-input"
                  type="password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                />
              </label>
            </div>
            <div className="admin-form-actions">
              <Button type="button" disabled={props.busy} onClick={() => void submitResetConfirm()}>
                {props.busy ? "Updating..." : "Update password"}
              </Button>
              <Button
                type="button"
                variant="ghost"
                onClick={() => {
                  setView("reset-request");
                  setInlineError(null);
                }}
              >
                Back
              </Button>
            </div>
          </div>
        ) : null}

        {view === "change-password" ? (
          <div className="admin-form">
            <div className="admin-inline-note">
              First-time access uses a temporary password. Update it before entering the restaurant application.
            </div>
            <label>
              Username
              <input className="admin-input" value={username} disabled />
            </label>
            <label>
              Current password
              <input
                className="admin-input"
                type="password"
                value={currentPassword}
                onChange={(event) => setCurrentPassword(event.target.value)}
              />
            </label>
            <div className="admin-form-grid">
              <label>
                New password
                <input
                  className="admin-input"
                  type="password"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                />
              </label>
              <label>
                Confirm password
                <input
                  className="admin-input"
                  type="password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                />
              </label>
            </div>
            <div className="admin-form-actions">
              <Button type="button" disabled={props.busy} onClick={() => void submitPasswordChange()}>
                {props.busy ? "Saving..." : "Update password"}
              </Button>
            </div>
          </div>
        ) : null}
      </SectionCard>
    </div>
  );
}
