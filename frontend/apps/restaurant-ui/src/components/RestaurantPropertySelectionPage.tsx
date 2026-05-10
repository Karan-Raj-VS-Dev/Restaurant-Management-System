import { loadTenantProperties, type AuthSession, type PropertyRecord } from "@restaurant/api";
import { Button, SectionCard, StatCard } from "@restaurant/ui";
import { useEffect, useMemo, useState } from "react";

interface RestaurantPropertySelectionPageProps {
  session: AuthSession;
  onSelect: (property: PropertyRecord) => void;
}

export function RestaurantPropertySelectionPage(props: RestaurantPropertySelectionPageProps) {
  const [properties, setProperties] = useState<PropertyRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    void loadTenantProperties()
      .then((result) => {
        if (active) {
          setProperties(result);
        }
      })
      .catch((caughtError) => {
        if (active) {
          setError(caughtError instanceof Error ? caughtError.message : "Unable to load properties.");
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const availableProperties = useMemo(() => {
    return properties
      .filter((property) => property.status === "ACTIVE" && props.session.mappedPropertyIds.includes(property.propertyId))
      .sort((left, right) => left.name.localeCompare(right.name));
  }, [properties, props.session.mappedPropertyIds]);

  return (
    <div className="admin-property-selection">
      <div className="admin-landing-stats">
        <StatCard label="Signed in as" value={props.session.fullName} hint={props.session.username} tone="warm" />
        <StatCard label="Tenant scope" value={props.session.tenantId} hint="Operational access stays inside this tenant" tone="cool" />
        <StatCard label="Mapped properties" value={String(availableProperties.length)} hint="Choose any property to continue" tone="neutral" />
      </div>

      <SectionCard
        title="Select a property"
        subtitle="This is the first step inside the restaurant application. Only the properties mapped by the admin console are shown here, and you can enter any one of them."
      >
        {loading ? <p className="admin-inline-note">Loading properties...</p> : null}
        {error ? <p className="admin-alert admin-alert-error">{error}</p> : null}
        {!loading && !error && availableProperties.length === 0 ? (
          <p className="admin-inline-note">No mapped properties are available for this account yet. Ask the admin to map your access first.</p>
        ) : null}

        <div className="admin-card-grid">
          {availableProperties.map((property) => (
            <article key={property.propertyId} className="admin-link-card">
              <div className="admin-property-card-meta">
                <h3>{property.name}</h3>
                <p>{property.addressLine || `${property.city}, ${property.country}`}</p>
                <p className="admin-inline-note">
                  Geo: {property.latitude ?? "N/A"}, {property.longitude ?? "N/A"}
                </p>
              </div>
              <Button onClick={() => props.onSelect(property)}>
                Enter {property.name}
              </Button>
            </article>
          ))}
        </div>
      </SectionCard>
    </div>
  );
}
