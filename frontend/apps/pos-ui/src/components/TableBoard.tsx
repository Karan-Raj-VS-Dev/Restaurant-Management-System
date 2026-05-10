import { useMemo, useState } from "react";
import { Button, StatusPill } from "@restaurant/ui";
import type { TableRecord } from "@restaurant/api";

export function TableBoard(props: {
  tables: TableRecord[];
  selectedTable: TableRecord | null;
  selectedTableId: string | null;
  onSelect: (tableId: string) => void;
  floorOptions: string[];
  selectedFloorName: string;
  onFloorChange: (value: string) => void;
  sectionOptions: string[];
  selectedSectionName: string;
  onSectionChange: (value: string) => void;
  waiterOptions: Array<{ employeeId: string; name: string }>;
  selectedWaiterId: string;
  onWaiterChange: (value: string) => void;
  cleanerOptions: Array<{ employeeId: string; name: string }>;
  selectedCleanerId: string;
  onCleanerChange: (value: string) => void;
  partySize: number;
  onPartySizeChange: (value: number) => void;
  reservationPartySize: number;
  onReservationPartySizeChange: (value: number) => void;
  reservationTime: string;
  onReservationTimeChange: (value: string) => void;
  waiterNameById: Map<string, string>;
  cleanerNameById: Map<string, string>;
  statusMessage: string | null;
  reservationOverridePending: boolean;
  busyStatusChange: boolean;
  onOccupyNow: (overrideReservationWarning?: boolean) => Promise<boolean>;
  onReserveTable: () => Promise<boolean>;
  onScheduleNeedsCleaning: () => Promise<boolean>;
  onMarkNeedsCleaningNow: () => Promise<boolean>;
  onScheduleCleanerReady: () => Promise<boolean>;
  onMarkAvailableNow: () => Promise<boolean>;
}) {
  const [activeModal, setActiveModal] = useState<"occupy" | "reserve" | "cleaning" | "available" | null>(null);
  const selectedWaiterName = useMemo(
    () => (props.selectedWaiterId ? props.waiterNameById.get(props.selectedWaiterId) ?? props.selectedWaiterId : null),
    [props.selectedWaiterId, props.waiterNameById]
  );
  const selectedCleanerName = useMemo(
    () => (props.selectedCleanerId ? props.cleanerNameById.get(props.selectedCleanerId) ?? props.selectedCleanerId : null),
    [props.selectedCleanerId, props.cleanerNameById]
  );

  const runAndCloseOnSuccess = async (action: () => Promise<boolean>) => {
    const success = await action();
    if (success) {
      setActiveModal(null);
    }
  };

  return (
    <div className="pos-table-board">
      <div className="pos-table-filters">
        <label className="pos-table-filter">
          <span>Floor</span>
          <select value={props.selectedFloorName} onChange={(event) => props.onFloorChange(event.target.value)}>
            <option value="">{props.floorOptions.length === 0 ? "No floors available" : "Select floor"}</option>
            {props.floorOptions.map((floor) => (
              <option key={floor} value={floor}>
                {floor}
              </option>
            ))}
          </select>
        </label>
        <label className="pos-table-filter">
          <span>Section</span>
          <select value={props.selectedSectionName} onChange={(event) => props.onSectionChange(event.target.value)}>
            <option value="">{props.sectionOptions.length === 0 ? "Select floor first" : "Select section"}</option>
            {props.sectionOptions.map((section) => (
              <option key={section} value={section}>
                {section}
              </option>
            ))}
          </select>
        </label>
        <label className="pos-table-filter">
          <span>Server</span>
          <select value={props.selectedWaiterId} onChange={(event) => props.onWaiterChange(event.target.value)}>
            <option value="">{props.waiterOptions.length === 0 ? "No assigned waiters" : "Select server"}</option>
            {props.waiterOptions.map((waiter) => (
              <option key={waiter.employeeId} value={waiter.employeeId}>
                {waiter.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      {props.selectedFloorName && props.selectedSectionName && props.waiterOptions.length === 0 ? (
        <p className="pos-inline-note">No available servers are assigned to this floor and section yet. Update the area-section assignment or employee availability first.</p>
      ) : null}

      {props.tables.length === 0 ? (
        <p className="pos-inline-note">No tables are configured for this floor and section yet.</p>
      ) : (
        <div className="pos-table-grid">
          {props.tables.map((table) => (
            <button
              key={table.tableId}
              className={`pos-table-tile ${props.selectedTableId === table.tableId ? "selected" : ""}`}
              onClick={() => props.onSelect(table.tableId)}
            >
              <div className="pos-table-head">
                <span>{table.displayName || table.tableNumber || table.tableId}</span>
                <StatusPill tone={toneForStatus(table.status)}>{formatStatus(table.status)}</StatusPill>
              </div>
              <div className="pos-table-meta">
                {(table.tableNumber || table.tableId).toUpperCase()} · {table.capacity} seats
              </div>
              <div className="pos-table-meta">
                {table.waiterId
                  ? `Assigned to ${props.waiterNameById.get(table.waiterId) ?? table.waiterId}`
                  : table.status === "UNAVAILABLE"
                    ? "Unavailable from property settings"
                    : "Choose a server to start a floor order"}
              </div>
              {table.reservationTime ? (
                <div className="pos-table-meta">
                  Reserved for {table.reservationPartySize ?? "?"} at {formatReservationTime(table.reservationTime)}
                </div>
              ) : null}
              {table.pendingStatus && table.pendingStatusAt ? (
                <div className="pos-table-meta">
                  Pending {formatStatus(table.pendingStatus).toLowerCase()} at {formatReservationTime(table.pendingStatusAt)}
                </div>
              ) : null}
            </button>
          ))}
        </div>
      )}

      {props.selectedTable ? (
        <div className="pos-table-status-panel">
          <div className="pos-table-status-head">
            <div>
              <h3>{props.selectedTable.displayName}</h3>
              <p>
                {(props.selectedTable.tableNumber || props.selectedTable.tableId).toUpperCase()} · {props.selectedTable.floorName ?? "No floor"} / {props.selectedTable.sectionName ?? "No section"}
              </p>
            </div>
            <StatusPill tone={toneForStatus(props.selectedTable.status)}>{formatStatus(props.selectedTable.status)}</StatusPill>
          </div>

          <div className="pos-table-status-notes">
            <p>Capacity: {props.selectedTable.capacity} guests maximum.</p>
            {props.selectedTable.currentPartySize ? <p>Current occupancy: {props.selectedTable.currentPartySize} guests.</p> : null}
            <p>
              {selectedWaiterName
                ? `Server selected for this table: ${selectedWaiterName}.`
                : "Choose a server above before starting a floor order."}
            </p>
            {selectedCleanerName ? <p>Cleaner selected for this zone: {selectedCleanerName}.</p> : null}
            {props.selectedTable.cleanerId ? <p>Cleaner assigned: {props.cleanerNameById.get(props.selectedTable.cleanerId) ?? props.selectedTable.cleanerId}.</p> : null}
            {props.selectedTable.reservationTime ? (
              <p>
                Reserved for {props.selectedTable.reservationPartySize ?? "?"} guests at {formatReservationTime(props.selectedTable.reservationTime)}.
              </p>
            ) : null}
            {props.selectedTable.pendingStatus && props.selectedTable.pendingStatusAt ? (
              <p>
                Pending {formatStatus(props.selectedTable.pendingStatus).toLowerCase()} at {formatReservationTime(props.selectedTable.pendingStatusAt)}.
              </p>
            ) : null}
          </div>

          {props.statusMessage ? <div className="pos-status-banner">{props.statusMessage}</div> : null}

          <div className="pos-table-status-actions">
            <Button
              variant="primary"
              disabled={props.busyStatusChange || props.selectedTable.status === "UNAVAILABLE"}
              onClick={() => setActiveModal("occupy")}
            >
              {props.reservationOverridePending ? "Override occupy" : "Occupy table"}
            </Button>
            <Button
              variant="ghost"
              disabled={props.busyStatusChange || props.selectedTable.status === "UNAVAILABLE"}
              onClick={() => setActiveModal("reserve")}
            >
              Reserve table
            </Button>
            <Button
              variant="secondary"
              disabled={props.busyStatusChange || props.selectedTable.status === "UNAVAILABLE"}
              onClick={() => setActiveModal("cleaning")}
            >
              Cleaning workflow
            </Button>
            <Button
              variant="ghost"
              disabled={props.busyStatusChange || props.selectedTable.status !== "NEEDS_CLEANING"}
              onClick={() => setActiveModal("available")}
            >
              Return available
            </Button>
          </div>
        </div>
      ) : null}

      {props.selectedTable && activeModal ? (
        <div className="pos-table-modal-overlay" role="presentation">
          <div className="pos-table-modal-card" role="dialog" aria-modal="true" aria-labelledby="pos-table-modal-title">
            <div className="pos-table-modal-header">
              <div>
                <h3 id="pos-table-modal-title">{modalTitle(activeModal, props.reservationOverridePending)}</h3>
                <p>
                  {props.selectedTable.displayName} · {(props.selectedTable.tableNumber || props.selectedTable.tableId).toUpperCase()}
                </p>
              </div>
              <Button variant="ghost" onClick={() => setActiveModal(null)}>
                Close
              </Button>
            </div>

            <div className="pos-table-modal-content">
              {activeModal === "occupy" ? (
                <>
                  <p className="pos-inline-note">
                    Mark this table occupied for a live floor order. Reservation time is not needed here.
                  </p>
                  <label className="pos-table-filter">
                    <span>Guests on table</span>
                    <input
                      type="number"
                      min={1}
                      max={props.selectedTable.capacity}
                      value={props.partySize}
                      onChange={(event) => props.onPartySizeChange(Number(event.target.value) || 0)}
                    />
                  </label>
                  <div className="pos-table-modal-note">
                    <strong>Server</strong>
                    <span>{selectedWaiterName ?? "Choose a server from the top selector before confirming."}</span>
                  </div>
                  {props.statusMessage ? <div className="pos-status-banner">{props.statusMessage}</div> : null}
                  <div className="pos-table-modal-actions">
                    <Button variant="ghost" onClick={() => setActiveModal(null)}>
                      Cancel
                    </Button>
                    <Button
                      disabled={props.busyStatusChange || !props.selectedWaiterId}
                      onClick={() => runAndCloseOnSuccess(() => props.onOccupyNow(props.reservationOverridePending))}
                    >
                      {props.reservationOverridePending ? "Override and occupy" : "Occupy now"}
                    </Button>
                  </div>
                </>
              ) : null}

              {activeModal === "reserve" ? (
                <>
                  <p className="pos-inline-note">
                    Use reservation only when you need to hold the table for a future booking.
                  </p>
                  <div className="pos-table-modal-grid">
                    <label className="pos-table-filter">
                      <span>Reservation party size</span>
                      <input
                        type="number"
                        min={1}
                        max={props.selectedTable.capacity}
                        value={props.reservationPartySize}
                        onChange={(event) => props.onReservationPartySizeChange(Number(event.target.value) || 0)}
                      />
                    </label>
                    <label className="pos-table-filter">
                      <span>Reservation time</span>
                      <input
                        type="datetime-local"
                        value={props.reservationTime}
                        onChange={(event) => props.onReservationTimeChange(event.target.value)}
                      />
                    </label>
                  </div>
                  {props.statusMessage ? <div className="pos-status-banner">{props.statusMessage}</div> : null}
                  <div className="pos-table-modal-actions">
                    <Button variant="ghost" onClick={() => setActiveModal(null)}>
                      Cancel
                    </Button>
                    <Button disabled={props.busyStatusChange} onClick={() => runAndCloseOnSuccess(props.onReserveTable)}>
                      Reserve table
                    </Button>
                  </div>
                </>
              ) : null}

              {activeModal === "cleaning" ? (
                <>
                  <p className="pos-inline-note">
                    Move the table into the cleaning workflow either immediately or after the short cool-down.
                  </p>
                  {props.statusMessage ? <div className="pos-status-banner">{props.statusMessage}</div> : null}
                  <div className="pos-table-modal-actions">
                    <Button variant="ghost" onClick={() => setActiveModal(null)}>
                      Cancel
                    </Button>
                    <Button
                      variant="secondary"
                      disabled={props.busyStatusChange}
                      onClick={() => runAndCloseOnSuccess(props.onScheduleNeedsCleaning)}
                    >
                      Cleaning in 2 min
                    </Button>
                    <Button disabled={props.busyStatusChange} onClick={() => runAndCloseOnSuccess(props.onMarkNeedsCleaningNow)}>
                      Needs cleaning now
                    </Button>
                  </div>
                </>
              ) : null}

              {activeModal === "available" ? (
                <>
                  <p className="pos-inline-note">
                    Choose the cleaner who is returning this table to service, then mark it available now or after the cleaner cycle.
                  </p>
                  <label className="pos-table-filter">
                    <span>Cleaner</span>
                    <select value={props.selectedCleanerId} onChange={(event) => props.onCleanerChange(event.target.value)}>
                      <option value="">{props.cleanerOptions.length === 0 ? "No assigned cleaners" : "Select cleaner"}</option>
                      {props.cleanerOptions.map((cleaner) => (
                        <option key={cleaner.employeeId} value={cleaner.employeeId}>
                          {cleaner.name}
                        </option>
                      ))}
                    </select>
                  </label>
                  {props.statusMessage ? <div className="pos-status-banner">{props.statusMessage}</div> : null}
                  <div className="pos-table-modal-actions">
                    <Button variant="ghost" onClick={() => setActiveModal(null)}>
                      Cancel
                    </Button>
                    <Button
                      variant="secondary"
                      disabled={props.busyStatusChange || !props.selectedCleanerId}
                      onClick={() => runAndCloseOnSuccess(props.onScheduleCleanerReady)}
                    >
                      Cleaner cycle 5 min
                    </Button>
                    <Button
                      disabled={props.busyStatusChange || !props.selectedCleanerId}
                      onClick={() => runAndCloseOnSuccess(props.onMarkAvailableNow)}
                    >
                      Available now
                    </Button>
                  </div>
                </>
              ) : null}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function modalTitle(mode: "occupy" | "reserve" | "cleaning" | "available", reservationOverridePending: boolean) {
  switch (mode) {
    case "occupy":
      return reservationOverridePending ? "Override reservation and occupy" : "Occupy table";
    case "reserve":
      return "Reserve table";
    case "cleaning":
      return "Cleaning workflow";
    case "available":
      return "Return table to available";
    default:
      return "Table action";
  }
}

function toneForStatus(status: TableRecord["status"]) {
  switch (status) {
    case "AVAILABLE":
      return "success" as const;
    case "OCCUPIED":
      return "info" as const;
    case "NEEDS_CLEANING":
      return "warning" as const;
    case "UNAVAILABLE":
      return "danger" as const;
    case "RESERVED":
      return "muted" as const;
    default:
      return "muted" as const;
  }
}

function formatStatus(status: TableRecord["status"]) {
  return status.replaceAll("_", " ").toLowerCase().replace(/(^|\s)\w/g, (match) => match.toUpperCase());
}

function formatReservationTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString([], {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
  });
}
