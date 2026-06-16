import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { RestaurantAuthFlow } from "./RestaurantAuthFlow";

describe("RestaurantAuthFlow", () => {
  it("submits login credentials through the provided handler", async () => {
    const onLogin = vi.fn().mockResolvedValue(undefined);

    render(
      <RestaurantAuthFlow
        busy={false}
        error={null}
        info={null}
        onLogin={onLogin}
        onRequestPasswordReset={vi.fn()}
        onConfirmPasswordReset={vi.fn()}
        onChangePassword={vi.fn()}
      />
    );

    fireEvent.change(screen.getByLabelText("Username"), { target: { value: "KaranRaj" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "Karan@Raj" } });
    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    await waitFor(() => expect(onLogin).toHaveBeenCalledWith("KaranRaj", "Karan@Raj"));
  });

  it("switches into forgot-password flow and shows returned OTP info", async () => {
    const onRequestPasswordReset = vi.fn().mockResolvedValue({
      accountFound: true,
      message: "OTP sent",
      deliveryChannel: "SMS",
      maskedDestination: "89******23",
      devOtp: "123456"
    });

    render(
      <RestaurantAuthFlow
        busy={false}
        error={null}
        info={null}
        onLogin={vi.fn()}
        onRequestPasswordReset={onRequestPasswordReset}
        onConfirmPasswordReset={vi.fn()}
        onChangePassword={vi.fn()}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: "Forgot password" }));
    fireEvent.change(screen.getByLabelText("Email or phone number"), { target: { value: "8901913123" } });
    fireEvent.click(screen.getByRole("button", { name: "Send OTP" }));

    await waitFor(() => expect(onRequestPasswordReset).toHaveBeenCalledWith("8901913123"));
    expect(screen.getByText(/OTP sent/)).toBeInTheDocument();
    expect(screen.getByLabelText("OTP")).toBeInTheDocument();
  });

  it("does not submit reset confirmation when the new passwords do not match", async () => {
    const onConfirmPasswordReset = vi.fn();

    render(
      <RestaurantAuthFlow
        busy={false}
        error={null}
        info={null}
        onLogin={vi.fn()}
        onRequestPasswordReset={vi.fn().mockResolvedValue({
          accountFound: true,
          message: "OTP sent",
          deliveryChannel: "SMS",
          maskedDestination: "89******23"
        })}
        onConfirmPasswordReset={onConfirmPasswordReset}
        onChangePassword={vi.fn()}
      />
    );

    fireEvent.click(screen.getByRole("button", { name: "Forgot password" }));
    fireEvent.change(screen.getByLabelText("Email or phone number"), { target: { value: "8901913123" } });
    fireEvent.click(screen.getByRole("button", { name: "Send OTP" }));

    await waitFor(() => expect(screen.getByLabelText("OTP")).toBeInTheDocument());
    fireEvent.change(screen.getByLabelText("OTP"), { target: { value: "123456" } });
    fireEvent.change(screen.getByLabelText("New password"), { target: { value: "Password@1" } });
    fireEvent.change(screen.getByLabelText("Confirm password"), { target: { value: "Password@2" } });
    fireEvent.click(screen.getByRole("button", { name: "Update password" }));

    expect(onConfirmPasswordReset).not.toHaveBeenCalled();
    expect(screen.getByText("New password and confirm password must match.")).toBeInTheDocument();
  });

  it("enters forced password change mode and submits the change request", async () => {
    const onChangePassword = vi.fn().mockResolvedValue({
      updated: true,
      message: "Password updated"
    });

    render(
      <RestaurantAuthFlow
        busy={false}
        error={null}
        info={null}
        forcePasswordChangeUsername="KaranRaj"
        initialCurrentPassword="Temp@123"
        onLogin={vi.fn()}
        onRequestPasswordReset={vi.fn()}
        onConfirmPasswordReset={vi.fn()}
        onChangePassword={onChangePassword}
      />
    );

    expect(screen.getByDisplayValue("KaranRaj")).toBeDisabled();
    fireEvent.change(screen.getByLabelText("New password"), { target: { value: "Karan@Raj1" } });
    fireEvent.change(screen.getByLabelText("Confirm password"), { target: { value: "Karan@Raj1" } });
    fireEvent.click(screen.getByRole("button", { name: "Update password" }));

    await waitFor(() =>
      expect(onChangePassword).toHaveBeenCalledWith({
        username: "KaranRaj",
        currentPassword: "Temp@123",
        newPassword: "Karan@Raj1"
      })
    );
  });
});
