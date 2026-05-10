import { type AuthSession, type PropertyRecord } from "@restaurant/api";
import { buildDashboardHref } from "../../../../packages/operations/src";
import { Button, SectionCard, StatCard } from "@restaurant/ui";

interface RestaurantLandingPageProps {
  session: AuthSession;
  selectedProperty: PropertyRecord;
}

export function RestaurantLandingPage(props: RestaurantLandingPageProps) {
  const dinerHref = buildDashboardHref("http://127.0.0.1:5173", props.session, props.selectedProperty);
  const inventoryHref = buildDashboardHref("http://127.0.0.1:5177", props.session, props.selectedProperty);
  const employeesHref = buildDashboardHref("http://127.0.0.1:5178", props.session, props.selectedProperty);
  const propertySettingsHref = buildDashboardHref("http://127.0.0.1:5179", props.session, props.selectedProperty);
  const kitchenHref = buildDashboardHref("http://127.0.0.1:5174", props.session, props.selectedProperty);
  const reportsHref = buildDashboardHref("http://127.0.0.1:5180", props.session, props.selectedProperty);

  const cards = [
    {
      id: "diner",
      title: "Diner dashboard",
      description: "Open the floor-facing POS experience for dine-in orders, live tables, and billing.",
      actionLabel: "Open diner dashboard",
      href: dinerHref
    },
    {
      id: "inventory",
      title: "Inventory dashboard",
      description: "Track stock health, low-stock ingredients, and what is pressuring the menu right now.",
      actionLabel: "View inventory",
      href: inventoryHref
    },
    {
      id: "employees",
      title: "Employee management",
      description: "Create and manage restaurant employees, shifts, availability, and salary details for this property.",
      actionLabel: "Manage employees",
      href: employeesHref
    },
    {
      id: "property-settings",
      title: "Property settings",
      description: "Configure tables, dishes, recipes, ingredients, supplies, taxes, billing templates, and future Excel-based setup.",
      actionLabel: "Open property settings",
      href: propertySettingsHref
    },
    {
      id: "kitchen",
      title: "Kitchen dashboard",
      description: "Jump into the kitchen board to accept, prepare, and finish live tickets.",
      actionLabel: "Open kitchen dashboard",
      href: kitchenHref
    },
    {
      id: "reports",
      title: "Reports dashboard",
      description: "See daily order, billing, and stock signals from the operations insights side of the platform.",
      actionLabel: "Open reports",
      href: reportsHref
    }
  ];

  return (
    <div className="admin-landing">
      <div className="admin-landing-stats">
        <StatCard label="Signed in as" value={props.session.fullName} hint={props.session.username} tone="warm" />
        <StatCard label="Tenant scope" value={props.session.tenantId} hint="All actions stay inside this tenant" tone="cool" />
        <StatCard
          label="Selected property"
          value={props.selectedProperty.name}
          hint={`${props.selectedProperty.city}, ${props.selectedProperty.country}`}
          tone="neutral"
        />
      </div>

      <SectionCard
        title="Choose your dashboard"
        subtitle="This restaurant application is property-aware. Pick the workflow you need, and everything stays tied to the property you selected."
      >
        <div className="restaurant-dashboard-grid">
          {cards.map((card) => (
            <article key={card.id} className="admin-link-card restaurant-dashboard-card">
              <div className="restaurant-dashboard-copy">
                <h3>{card.title}</h3>
                <p>{card.description}</p>
              </div>
              <div className="restaurant-dashboard-card-action">
                <Button variant={card.id === "diner" || card.id === "kitchen" ? "secondary" : "primary"} onClick={() => (window.location.href = card.href!)}>
                  {card.actionLabel}
                </Button>
              </div>
            </article>
          ))}
        </div>
      </SectionCard>
    </div>
  );
}
