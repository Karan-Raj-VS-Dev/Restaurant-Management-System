import { Button, EmptyState, StatusPill } from "@restaurant/ui";
import type { KitchenTicket } from "@restaurant/api";

export function TicketLane(props: {
  title: string;
  tone: "info" | "warning" | "success";
  tickets: KitchenTicket[];
  onAccept: (ticketId: string) => Promise<void>;
  onReady: (ticketId: string) => Promise<void>;
  busyTicketId: string | null;
}) {
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
          {props.tickets.map((ticket) => (
            <article className="kitchen-ticket" key={ticket.ticketId}>
              <div className="kitchen-ticket-head">
                <div>
                  <h3>{ticket.ticketId}</h3>
                  <p>{ticket.orderId}</p>
                </div>
                <StatusPill tone={props.tone}>{ticket.status}</StatusPill>
              </div>
              <div className="kitchen-ticket-meta">
                <span>Cook {ticket.cookId}</span>
                <span>{new Date(ticket.updatedAt).toLocaleTimeString()}</span>
              </div>
              <div className="kitchen-ticket-actions">
                <Button
                  variant="ghost"
                  disabled={ticket.status !== "RECEIVED" || props.busyTicketId === ticket.ticketId}
                  onClick={() => props.onAccept(ticket.ticketId)}
                >
                  Accept
                </Button>
                <Button
                  variant="secondary"
                  disabled={(ticket.status !== "ACCEPTED" && ticket.status !== "PREPARING") || props.busyTicketId === ticket.ticketId}
                  onClick={() => props.onReady(ticket.ticketId)}
                >
                  Mark ready
                </Button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
