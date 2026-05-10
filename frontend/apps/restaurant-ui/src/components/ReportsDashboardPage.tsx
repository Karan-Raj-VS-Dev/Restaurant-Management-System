import { loadReportsDashboard } from "@restaurant/api";
import { Button, LivePulse, SectionCard, StatCard, StatusPill, usePollingResource } from "@restaurant/ui";

export function ReportsDashboardPage() {
  const { data, loading, refreshing, lastUpdated, error, refresh } = usePollingResource(loadReportsDashboard, 7000);
  const monthlySalaryLoad = (data?.employees ?? []).reduce((sum, employee) => sum + (employee.salaryAmount ?? 0), 0);

  return (
    <div className="admin-dashboard-stack">
      <div className="admin-toolbar">
        <LivePulse label={refreshing ? "Refreshing reports" : "Reports polling active"} lastUpdated={lastUpdated} />
        <Button variant="ghost" onClick={() => void refresh()}>
          Refresh now
        </Button>
      </div>

      <div className="admin-landing-stats">
        <StatCard label="Total orders today" value={String(data?.dailyInsight.totalOrdersToday ?? 0)} hint={`Busiest ${data?.dailyInsight.busiestTableId ?? "table"}`} tone="warm" />
        <StatCard label="Gross sales today" value={`Rs ${data?.dailyInsight.grossSalesToday ?? 0}`} hint="Billing + payment signals" tone="cool" />
        <StatCard label="Open bills" value={String((data?.bills ?? []).filter((bill) => bill.status !== "PAID").length)} hint={`Salary load Rs ${formatDashboardAmount(monthlySalaryLoad)}`} tone="neutral" />
      </div>

      <div className="admin-report-grid">
        <SectionCard title="Recent orders" subtitle="Dine-in order stream from the order service.">
          {loading && !data ? <p className="admin-inline-note">Loading recent orders...</p> : null}
          {error ? <p className="admin-alert admin-alert-error">{error}</p> : null}
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Table</th>
                  <th>Waiter</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {(data?.orders ?? []).map((order) => (
                  <tr key={order.orderId}>
                    <td>{order.orderId}</td>
                    <td>{order.tableId}</td>
                    <td>{order.waiterId}</td>
                    <td>{order.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </SectionCard>

        <SectionCard title="Stock impact" subtitle="Ingredient health that can influence reporting and sales.">
          <p className="admin-inline-note">
            Employee salary base for this property: <strong>Rs {formatDashboardAmount(monthlySalaryLoad)}</strong> across{" "}
            <strong>{data?.employees.length ?? 0}</strong> employees.
          </p>
          <div className="admin-pill-list">
            {(data?.stockInsights ?? []).map((item) => (
              <StatusPill key={`${item.ingredientName}-${item.stockHealth}`} tone={item.stockHealth === "OUT_OF_STOCK" ? "danger" : item.stockHealth === "LOW_STOCK" ? "warning" : item.stockHealth === "OVER_CAPACITY" ? "info" : "success"}>
                {item.ingredientName}: {item.stockHealth}
              </StatusPill>
            ))}
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

function formatDashboardAmount(amount: number) {
  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0
  }).format(amount);
}
