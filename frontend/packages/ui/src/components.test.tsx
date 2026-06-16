import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import {
  AppShell,
  Button,
  EmptyState,
  LivePulse,
  SectionCard,
  StatCard,
  StatusPill,
  WorkspaceIdentityBadges,
  usePollingResource
} from "./components";

function PollingHarness(props: { loader: () => Promise<{ value: string }>; intervalMs?: number }) {
  const resource = usePollingResource(props.loader, props.intervalMs ?? 1000);

  return (
    <div>
      <div data-testid="loading">{String(resource.loading)}</div>
      <div data-testid="refreshing">{String(resource.refreshing)}</div>
      <div data-testid="error">{resource.error ?? ""}</div>
      <div data-testid="value">{resource.data?.value ?? ""}</div>
      <div data-testid="updated">{resource.lastUpdated ? "yes" : "no"}</div>
      <button type="button" onClick={() => void resource.refresh()}>
        manual refresh
      </button>
    </div>
  );
}

describe("WorkspaceIdentityBadges", () => {
  it("renders user and property badges with the shared styling copy", () => {
    render(<WorkspaceIdentityBadges userName="Karan Raj" propertyName="Krusty Krab" />);

    expect(screen.getByText("Karan Raj")).toBeInTheDocument();
    expect(screen.getByText("Krusty Krab")).toBeInTheDocument();
  });

  it("renders nothing when no identity values are available", () => {
    const view = render(<WorkspaceIdentityBadges userName={null} propertyName={null} />);

    expect(view.container).toBeEmptyDOMElement();
  });
});

describe("shared UI primitives", () => {
  it("renders the app shell hero, actions, and content", () => {
    render(
      <AppShell
        eyebrow="Restaurant application"
        title="Krusty Krab"
        subtitle="Operate this property from one workspace."
        actions={<button type="button">Refresh</button>}
      >
        <div>Shell content</div>
      </AppShell>
    );

    expect(screen.getByText("Restaurant application")).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 1, name: "Krusty Krab" })).toBeInTheDocument();
    expect(screen.getByText("Operate this property from one workspace.")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Refresh" })).toBeInTheDocument();
    expect(screen.getByText("Shell content")).toBeInTheDocument();
  });

  it("renders section cards, stat cards, pills, and buttons with optional branches", () => {
    const onClick = vi.fn();

    render(
      <div>
        <SectionCard
          title="Inventory"
          subtitle="Track stock health"
          action={<span>Action slot</span>}
          className="custom-card"
        >
          <div>Section body</div>
        </SectionCard>
        <StatCard label="Orders" value="12" hint="Today" tone="alert" />
        <StatusPill tone="success">Active</StatusPill>
        <StatusPill>Muted</StatusPill>
        <Button variant="secondary" type="submit" onClick={onClick}>
          Save
        </Button>
        <Button disabled variant="ghost">
          Disabled
        </Button>
      </div>
    );

    expect(screen.getByRole("heading", { level: 2, name: "Inventory" })).toBeInTheDocument();
    expect(screen.getByText("Track stock health")).toBeInTheDocument();
    expect(screen.getByText("Action slot")).toBeInTheDocument();
    expect(screen.getByText("Section body").closest(".custom-card")).toBeInTheDocument();
    expect(screen.getByText("Orders").closest(".rm-tone-alert")).toBeInTheDocument();
    expect(screen.getByText("Today")).toBeInTheDocument();
    expect(screen.getByText("Active").className).toContain("rm-pill-success");
    expect(screen.getByText("Muted").className).toContain("rm-pill-muted");

    fireEvent.click(screen.getByRole("button", { name: "Save" }));
    expect(onClick).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("button", { name: "Save" })).toHaveAttribute("type", "submit");
    expect(screen.getByRole("button", { name: "Disabled" })).toBeDisabled();
  });

  it("shows an empty state title and body", () => {
    render(<EmptyState title="Nothing queued" body="Orders will appear here when service starts." />);

    expect(screen.getByRole("heading", { name: "Nothing queued" })).toBeInTheDocument();
    expect(screen.getByText("Orders will appear here when service starts.")).toBeInTheDocument();
  });

  it("formats live pulse timestamps relative to the current time", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-06-14T12:00:00Z"));

    const view = render(<LivePulse label="Board live" lastUpdated={new Date("2026-06-14T11:59:58Z")} />);

    expect(screen.getByText("Board live")).toBeInTheDocument();
    expect(screen.getByText("Updated just now")).toBeInTheDocument();

    view.rerender(<LivePulse label="Board live" lastUpdated={new Date("2026-06-14T11:59:20Z")} />);
    expect(screen.getByText("Updated 40s ago")).toBeInTheDocument();

    view.rerender(<LivePulse label="Board live" lastUpdated={new Date("2026-06-14T11:58:00Z")} />);
    expect(screen.getByText("Updated 2m ago")).toBeInTheDocument();

    view.rerender(<LivePulse label="Board live" lastUpdated={new Date("2026-06-14T10:00:00Z")} />);
    expect(screen.getByText("Updated 2h ago")).toBeInTheDocument();

    view.rerender(<LivePulse label="Board live" lastUpdated={null} />);
    expect(screen.getByText("Waiting for data")).toBeInTheDocument();

    vi.useRealTimers();
  });

  it("polls resources, refreshes on interval, and exposes loading state", async () => {
    vi.useFakeTimers();
    const loader = vi
      .fn<() => Promise<{ value: string }>>()
      .mockResolvedValueOnce({ value: "first" })
      .mockResolvedValueOnce({ value: "second" })
      .mockResolvedValueOnce({ value: "manual" });

    render(<PollingHarness loader={loader} intervalMs={1000} />);

    expect(screen.getByTestId("loading")).toHaveTextContent("true");
    await act(async () => {
      await Promise.resolve();
    });
    expect(screen.getByTestId("value")).toHaveTextContent("first");
    expect(screen.getByTestId("loading")).toHaveTextContent("false");
    expect(screen.getByTestId("updated")).toHaveTextContent("yes");

    await act(async () => {
      await vi.advanceTimersByTimeAsync(1000);
    });
    expect(screen.getByTestId("value")).toHaveTextContent("second");

    await act(async () => {
      fireEvent.click(screen.getByRole("button", { name: "manual refresh" }));
      await Promise.resolve();
    });
    expect(screen.getByTestId("value")).toHaveTextContent("manual");

    vi.useRealTimers();
  });

  it("surfaces loader errors from the polling hook", async () => {
    const loader = vi.fn<() => Promise<{ value: string }>>().mockRejectedValue(new Error("Load failed"));

    render(<PollingHarness loader={loader} intervalMs={1000} />);

    await act(async () => {
      await Promise.resolve();
    });
    expect(screen.getByTestId("error")).toHaveTextContent("Load failed");
    expect(screen.getByTestId("loading")).toHaveTextContent("false");
    expect(screen.getByTestId("refreshing")).toHaveTextContent("false");
  });
});
