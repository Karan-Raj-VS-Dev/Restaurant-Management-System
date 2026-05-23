import { useEffect, useMemo, useState } from "react";
import { Button, EmptyState, StatusPill } from "@restaurant/ui";
import type { KitchenTicketDetail } from "@restaurant/api";

export function TicketLane(props: {
  title: string;
  tone: "info" | "warning" | "success";
  tickets: KitchenTicketDetail[];
  cookOptions: Array<{ employeeId: string; name: string }>;
  selectedCookByTicketId: Record<string, string>;
  onCookChange: (ticketId: string, cookId: string) => void;
  onAccept: (ticket: KitchenTicketDetail, cookId: string | null) => Promise<void>;
  onReady: (ticket: KitchenTicketDetail, cookId: string | null) => Promise<void>;
  busyTicketId: string | null;
  urgencyMode?: "received";
}) {
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    if (props.urgencyMode !== "received" || props.tickets.length === 0) {
      return;
    }
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, [props.tickets.length, props.urgencyMode]);

  const sortedTickets = useMemo(() => {
    const next = [...props.tickets];
    if (props.urgencyMode === "received") {
      next.sort((left, right) => getRemainingMs(left.updatedAt, now) - getRemainingMs(right.updatedAt, now));
    }
    return next;
  }, [now, props.tickets, props.urgencyMode]);

  return (
    <section className="kitchen-lane">
      <div className="kitchen-lane-head">
        <h2>{props.title}</h2>
        <StatusPill tone={props.tone}>{props.tickets.length} tickets</StatusPill>
      </div>

      {props.tickets.length === 0 ? (
        <EmptyState title="No tickets here" body="As new kitchen events arrive, they will stack in the correct lane." />
      ) : (
        <div className="kitchen-ticket-stack">
          {sortedTickets.map((ticket) => {
            const remainingMs = props.urgencyMode === "received" ? getRemainingMs(ticket.updatedAt, now) : null;
            const progressPercent =
              remainingMs === null ? null : remainingMs <= 0 ? 100 : Math.max(6, Math.min(100, (remainingMs / (5 * 60 * 1000)) * 100));
            const urgencyState = remainingMs === null ? "steady" : remainingMs <= 0 ? "overdue" : remainingMs <= 60_000 ? "urgent" : "steady";
            const itemSummary = summarizeItems(ticket.items);
            const selectedCookId = props.selectedCookByTicketId[ticket.ticketId] ?? (ticket.cookId !== "cook-pending" ? ticket.cookId : "");
            const selectedCookName =
              props.cookOptions.find((cook) => cook.employeeId === selectedCookId)?.name ??
              (ticket.cookName !== "Chef pending" ? ticket.cookName : "");
            return (
              <article className={`kitchen-ticket kitchen-ticket-${urgencyState}`} key={ticket.ticketId}>
                <div className="kitchen-ticket-head">
                  <div>
                    <h3>{itemSummary.headline}</h3>
                    <p>
                      {ticket.tableName} · {ticket.waiterName}
                    </p>
                  </div>
                  <StatusPill tone={props.tone}>{ticket.status}</StatusPill>
                </div>
                {ticket.items.length === 0 ? (
                  <p className="kitchen-inline-note">Dish details are syncing from the order.</p>
                ) : (
                  <ul className="kitchen-ticket-items">
                    {ticket.items.map((item) => (
                      <li key={`${ticket.ticketId}-${item.itemId}`}>{item.quantity}× {item.itemName}</li>
                    ))}
                  </ul>
                )}
                <div className="kitchen-ticket-meta">
                  <span>{selectedCookName ? `Chef ${selectedCookName}` : "Select a chef for this ticket"}</span>
                  <span>{new Date(ticket.updatedAt).toLocaleTimeString()}</span>
                </div>
                <label className="kitchen-chef-select">
                  <span>Chef</span>
                  <select value={selectedCookId} onChange={(event) => props.onCookChange(ticket.ticketId, event.target.value)}>
                    <option value="">{props.cookOptions.length === 0 ? "No chefs available" : "Select chef"}</option>
                    {props.cookOptions.map((cook) => (
                      <option key={cook.employeeId} value={cook.employeeId}>
                        {cook.name}
                      </option>
                    ))}
                  </select>
                </label>
                {remainingMs !== null ? (
                  <div className="kitchen-ticket-timer-block">
                    <div className="kitchen-ticket-timer-meta">
                      <span>{remainingMs <= 0 ? "Highest priority" : "Pickup in progress"}</span>
                      <strong>{remainingMs <= 0 ? "Overdue" : formatRemaining(remainingMs)}</strong>
                    </div>
                    <div className="kitchen-ticket-timer-track" aria-hidden="true">
                      <div
                        className={`kitchen-ticket-timer-fill kitchen-ticket-timer-fill-${urgencyState}`}
                        style={{ width: `${progressPercent}%` }}
                      />
                    </div>
                  </div>
                ) : null}
                <div className="kitchen-ticket-actions">
                  <Button
                    variant="ghost"
                    disabled={ticket.status !== "RECEIVED" || props.busyTicketId === ticket.ticketId || !selectedCookId}
                    onClick={() => props.onAccept(ticket, selectedCookId || null)}
                  >
                    Accept
                  </Button>
                  <Button
                    variant="secondary"
                    disabled={
                      (ticket.status !== "ACCEPTED" && ticket.status !== "PREPARING") ||
                      props.busyTicketId === ticket.ticketId ||
                      !selectedCookId
                    }
                    onClick={() => props.onReady(ticket, selectedCookId || null)}
                  >
                    Mark ready
                  </Button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

function summarizeItems(items: KitchenTicketDetail["items"]) {
  if (items.length === 0) {
    return {
      headline: "Dish details pending",
      details: "This ticket is waiting for dish details to sync from the order."
    };
  }

  const primary = items[0];
  const remainingCount = items.length - 1;
  return {
    headline: remainingCount > 0 ? `${primary.quantity}× ${primary.itemName} + ${remainingCount} more` : `${primary.quantity}× ${primary.itemName}`,
    details: items.map((item) => `${item.quantity}× ${item.itemName}`).join(", ")
  };
}

function getRemainingMs(updatedAt: string, now: number) {
  return new Date(updatedAt).getTime() + 5 * 60 * 1000 - now;
}

function formatRemaining(remainingMs: number) {
  const totalSeconds = Math.max(0, Math.ceil(remainingMs / 1000));
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  if (minutes <= 0) {
    return `${seconds}s`;
  }
  return `${minutes}m ${String(seconds).padStart(2, "0")}s`;
}
