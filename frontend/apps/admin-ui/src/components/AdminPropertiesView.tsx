import {
  createManagedProperty,
  deleteManagedProperty,
  isApiRequestError,
  loadTenantProperties,
  updateManagedProperty,
  type ManagedPropertyPayload,
  type ManagedPropertyUpdatePayload,
  type PropertyRecord
} from "@restaurant/api";
import { Button, SectionCard, StatCard, StatusPill } from "@restaurant/ui";
import { FormEvent, useEffect, useMemo, useState } from "react";

interface PropertyFormState {
  name: string;
  addressLine: string;
  city: string;
  state: string;
  country: string;
  timezone: string;
  latitude: string;
  longitude: string;
  status: string;
}

type PanelMode = "add" | "edit";
type BannerTone = "success" | "error";
type PropertyFormField = keyof PropertyFormState;
type PropertyFormErrors = Partial<Record<PropertyFormField | "form", string>>;

const propertyFieldLabels: Record<PropertyFormField, string> = {
  name: "Property name",
  addressLine: "Address",
  city: "City",
  state: "State",
  country: "Country",
  timezone: "Timezone",
  latitude: "Latitude",
  longitude: "Longitude",
  status: "Status"
};

function blankForm(): PropertyFormState {
  return {
    name: "",
    addressLine: "",
    city: "",
    state: "",
    country: "India",
    timezone: "Asia/Kolkata",
    latitude: "",
    longitude: "",
    status: "ACTIVE"
  };
}

export function AdminPropertiesView() {
  const [properties, setProperties] = useState<PropertyRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);
  const [banner, setBanner] = useState<{ tone: BannerTone; message: string } | null>(null);
  const [panelMode, setPanelMode] = useState<PanelMode>("add");
  const [panelOpen, setPanelOpen] = useState(false);
  const [panelRevision, setPanelRevision] = useState(0);
  const [selectedProperty, setSelectedProperty] = useState<PropertyRecord | null>(null);
  const [form, setForm] = useState<PropertyFormState>(blankForm());
  const [formErrors, setFormErrors] = useState<PropertyFormErrors>({});

  const activeProperties = useMemo(() => properties.filter((property) => property.status === "ACTIVE").length, [properties]);

  const refreshProperties = async () => {
    setLoading(true);
    setPageError(null);
    try {
      const response = await loadTenantProperties();
      setProperties(response);
      if (selectedProperty) {
        const nextSelected = response.find((property) => property.propertyId === selectedProperty.propertyId) ?? null;
        setSelectedProperty(nextSelected);
        if (panelOpen && nextSelected) {
          setForm(toForm(nextSelected));
        }
      }
    } catch (caughtError) {
      setPageError(caughtError instanceof Error ? caughtError.message : "Unable to load properties.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshProperties();
  }, []);

  const clearBannerIfError = () => {
    setBanner((current) => (current?.tone === "error" ? null : current));
  };

  const clearFormErrors = (...fields: Array<PropertyFormField | "form">) => {
    if (fields.length === 0) {
      setFormErrors({});
      return;
    }
    setFormErrors((current) => {
      const next = { ...current };
      for (const field of fields) {
        delete next[field];
      }
      return next;
    });
  };

  const updateField = <K extends PropertyFormField>(field: K, value: PropertyFormState[K]) => {
    setForm((current) => ({ ...current, [field]: value }));
    clearFormErrors(field, "form");
    clearBannerIfError();
  };

  const openAddPanel = () => {
    setSelectedProperty(null);
    setPanelMode("add");
    setPanelOpen(true);
    setPanelRevision((current) => current + 1);
    setForm(blankForm());
    setFormErrors({});
    setBanner(null);
  };

  const openEditPanel = (property: PropertyRecord) => {
    setSelectedProperty(property);
    setPanelMode("edit");
    setPanelOpen(true);
    setPanelRevision((current) => current + 1);
    setForm(toForm(property));
    setFormErrors({});
    setBanner(null);
  };

  const closePanel = () => {
    setPanelOpen(false);
    setSelectedProperty(null);
    setFormErrors({});
    setBanner(null);
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    const validationErrors = validatePropertyForm(form);
    if (Object.keys(validationErrors).length > 0) {
      setFormErrors(validationErrors);
      setBanner({
        tone: "error",
        message: buildValidationBanner(validationErrors, propertyFieldLabels)
      });
      return;
    }

    setSubmitting(true);
    setBanner(null);
    clearFormErrors();
    try {
      if (panelMode === "add") {
        const payload: ManagedPropertyPayload = {
          name: form.name.trim(),
          addressLine: form.addressLine.trim(),
          city: form.city.trim(),
          state: form.state.trim(),
          country: form.country.trim(),
          timezone: form.timezone.trim(),
          latitude: parseNumber(form.latitude),
          longitude: parseNumber(form.longitude)
        };
        await createManagedProperty(payload);
        setBanner({
          tone: "success",
          message: "Property created successfully."
        });
        setForm(blankForm());
      } else if (selectedProperty) {
        const payload: ManagedPropertyUpdatePayload = {
          name: form.name.trim(),
          addressLine: form.addressLine.trim(),
          city: form.city.trim(),
          state: form.state.trim(),
          country: form.country.trim(),
          timezone: form.timezone.trim(),
          latitude: parseNumber(form.latitude),
          longitude: parseNumber(form.longitude),
          status: form.status
        };
        await updateManagedProperty(selectedProperty.propertyId, payload);
        setBanner({
          tone: "success",
          message: "Property updated successfully."
        });
      }
      await refreshProperties();
    } catch (caughtError) {
      const nextErrors = mapPropertyFieldErrors(caughtError);
      if (Object.keys(nextErrors).length > 0) {
        setFormErrors(nextErrors);
      }
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to save property."
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedProperty) {
      return;
    }
    const confirmed = window.confirm(`Delete ${selectedProperty.name}?`);
    if (!confirmed) {
      return;
    }
    setSubmitting(true);
    setBanner(null);
    try {
      await deleteManagedProperty(selectedProperty.propertyId);
      await refreshProperties();
      openAddPanel();
      setBanner({
        tone: "success",
        message: "Property deleted successfully."
      });
    } catch (caughtError) {
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to delete property."
      });
    } finally {
      setSubmitting(false);
    }
  };

  const fillCurrentLocation = () => {
    if (!navigator.geolocation) {
      setBanner({
        tone: "error",
        message: "Geolocation is not available in this browser."
      });
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setForm((current) => ({
          ...current,
          latitude: position.coords.latitude.toFixed(6),
          longitude: position.coords.longitude.toFixed(6)
        }));
        clearFormErrors("latitude", "longitude", "form");
        setBanner({
          tone: "success",
          message: "Location coordinates captured."
        });
      },
      () =>
        setBanner({
          tone: "error",
          message: "Unable to capture your current location."
        })
    );
  };

  return (
    <div className="admin-users-page">
      <div className="admin-users-stats">
        <StatCard label="Tenant properties" value={String(properties.length)} hint="All configured outlets in this tenant" tone="warm" />
        <StatCard label="Active properties" value={String(activeProperties)} hint="Available to map and operate" tone="cool" />
        <StatCard label="Geo tagged" value={String(properties.filter((property) => property.latitude && property.longitude).length)} hint="Properties with coordinates" tone="neutral" />
      </div>

      {pageError ? <div className="admin-alert admin-alert-error">{pageError}</div> : null}

      <div className={`admin-users-layout ${panelOpen ? "panel-open" : "panel-closed"}`}>
        <SectionCard
          className="admin-users-table-card"
          title="Property dashboard"
          subtitle="Properties are tenant-owned locations. Each one can be mapped to users and selected before entering the operational dashboards."
          action={
            <div className="admin-users-actions">
              <Button variant="ghost" onClick={() => void refreshProperties()}>
                Refresh
              </Button>
              <Button onClick={openAddPanel}>Add property</Button>
            </div>
          }
        >
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Location</th>
                  <th>Geo</th>
                  <th>Status</th>
                  <th className="admin-table-action-header" />
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={5} className="admin-empty-cell">
                      Loading properties...
                    </td>
                  </tr>
                ) : properties.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="admin-empty-cell">
                      No properties have been created yet.
                    </td>
                  </tr>
                ) : (
                  properties.map((property) => (
                    <tr key={property.propertyId} className={selectedProperty?.propertyId === property.propertyId && panelOpen ? "selected" : ""}>
                      <td>{property.name}</td>
                      <td>{property.addressLine || `${property.city}, ${property.country}`}</td>
                      <td>
                        {property.latitude ?? "N/A"}, {property.longitude ?? "N/A"}
                      </td>
                      <td>
                        <StatusPill tone={property.status === "ACTIVE" ? "success" : "warning"}>{property.status}</StatusPill>
                      </td>
                      <td className="admin-table-action-cell">
                        <Button variant="ghost" onClick={() => openEditPanel(property)}>
                          Edit
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </SectionCard>

        {panelOpen ? (
          <aside className="admin-side-panel is-open">
            <div key={`${panelMode}-${selectedProperty?.propertyId ?? "new"}-${panelRevision}`} className="admin-panel-frame">
              <button type="button" className="admin-panel-close" onClick={closePanel} aria-label="Close panel">
                ×
              </button>
              <SectionCard
                className="admin-panel-card"
                title={panelMode === "add" ? "Add property" : "Edit property"}
                subtitle={
                  panelMode === "add"
                    ? "Create a new tenant property with location and geotag details."
                    : "Update property details without leaving the dashboard table."
                }
              >
                <form className="admin-form" onSubmit={handleSubmit}>
                  <label>
                    Property name
                    <input className={inputClass(formErrors.name)} value={form.name} onChange={(event) => updateField("name", event.target.value)} />
                    {formErrors.name ? <span className="admin-field-feedback">{formErrors.name}</span> : null}
                  </label>

                  <label>
                    Address
                    <textarea className={`${inputClass(formErrors.addressLine)} admin-textarea`} value={form.addressLine} onChange={(event) => updateField("addressLine", event.target.value)} />
                    {formErrors.addressLine ? <span className="admin-field-feedback">{formErrors.addressLine}</span> : null}
                  </label>

                  <div className="admin-form-grid">
                    <label>
                      City
                      <input className={inputClass(formErrors.city)} value={form.city} onChange={(event) => updateField("city", event.target.value)} />
                      {formErrors.city ? <span className="admin-field-feedback">{formErrors.city}</span> : null}
                    </label>
                    <label>
                      State
                      <input className={inputClass(formErrors.state)} value={form.state} onChange={(event) => updateField("state", event.target.value)} />
                      {formErrors.state ? <span className="admin-field-feedback">{formErrors.state}</span> : null}
                    </label>
                  </div>

                  <div className="admin-form-grid">
                    <label>
                      Country
                      <input className={inputClass(formErrors.country)} value={form.country} onChange={(event) => updateField("country", event.target.value)} />
                      {formErrors.country ? <span className="admin-field-feedback">{formErrors.country}</span> : null}
                    </label>
                    <label>
                      Timezone
                      <input className={inputClass(formErrors.timezone)} value={form.timezone} onChange={(event) => updateField("timezone", event.target.value)} />
                      {formErrors.timezone ? <span className="admin-field-feedback">{formErrors.timezone}</span> : null}
                    </label>
                  </div>

                  <div className="admin-location-row">
                    <div className="admin-form-grid">
                      <label>
                        Latitude
                        <input className={inputClass(formErrors.latitude)} value={form.latitude} onChange={(event) => updateField("latitude", event.target.value)} />
                        {formErrors.latitude ? <span className="admin-field-feedback">{formErrors.latitude}</span> : null}
                      </label>
                      <label>
                        Longitude
                        <input className={inputClass(formErrors.longitude)} value={form.longitude} onChange={(event) => updateField("longitude", event.target.value)} />
                        {formErrors.longitude ? <span className="admin-field-feedback">{formErrors.longitude}</span> : null}
                      </label>
                    </div>
                    <Button variant="ghost" onClick={fillCurrentLocation}>
                      Track current location
                    </Button>
                  </div>

                  {panelMode === "edit" ? (
                    <label>
                      Status
                      <select className={inputClass(formErrors.status)} value={form.status} onChange={(event) => updateField("status", event.target.value)}>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                      </select>
                      {formErrors.status ? <span className="admin-field-feedback">{formErrors.status}</span> : null}
                    </label>
                  ) : null}

                  {banner ? <div className={`admin-alert admin-form-alert ${banner.tone === "success" ? "admin-alert-info" : "admin-alert-error"}`}>{banner.message}</div> : null}

                  <div className="admin-form-actions">
                    <Button type="submit" disabled={submitting}>
                      {submitting ? "Saving..." : panelMode === "add" ? "Create property" : "Save changes"}
                    </Button>
                    <Button variant="ghost" onClick={panelMode === "add" ? openAddPanel : () => selectedProperty && openEditPanel(selectedProperty)}>
                      Clear
                    </Button>
                    {panelMode === "edit" ? (
                      <Button variant="ghost" onClick={handleDelete} disabled={submitting}>
                        Delete property
                      </Button>
                    ) : null}
                  </div>
                </form>
              </SectionCard>
            </div>
          </aside>
        ) : null}
      </div>
    </div>
  );
}

function toForm(property: PropertyRecord): PropertyFormState {
  return {
    name: property.name,
    addressLine: property.addressLine ?? "",
    city: property.city,
    state: property.state ?? "",
    country: property.country,
    timezone: "Asia/Kolkata",
    latitude: property.latitude?.toString() ?? "",
    longitude: property.longitude?.toString() ?? "",
    status: property.status
  };
}

function inputClass(error?: string) {
  return error ? "admin-input admin-input-invalid" : "admin-input";
}

function parseNumber(value: string) {
  if (!value.trim()) {
    return null;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

function validatePropertyForm(form: PropertyFormState): PropertyFormErrors {
  const errors: PropertyFormErrors = {};

  if (!form.name.trim()) {
    errors.name = "Property name is required.";
  }
  if (!form.addressLine.trim()) {
    errors.addressLine = "Address is required.";
  }
  if (!form.city.trim()) {
    errors.city = "City is required.";
  }
  if (form.latitude.trim() && parseNumber(form.latitude) === null) {
    errors.latitude = "Latitude must be a valid number.";
  }
  if (form.longitude.trim() && parseNumber(form.longitude) === null) {
    errors.longitude = "Longitude must be a valid number.";
  }

  return errors;
}

function buildValidationBanner(errors: PropertyFormErrors, labels: Record<PropertyFormField, string>) {
  const invalidFields = Object.keys(errors)
    .filter((key): key is PropertyFormField => key !== "form")
    .map((key) => labels[key]);

  if (invalidFields.length === 0) {
    return "Please correct the highlighted fields and try again.";
  }

  return `Please correct the highlighted fields: ${invalidFields.join(", ")}.`;
}

function mapPropertyFieldErrors(error: unknown): PropertyFormErrors {
  if (!isApiRequestError(error)) {
    return {};
  }

  return Object.entries(error.fieldErrors).reduce<PropertyFormErrors>((accumulator, [key, value]) => {
    if (key in propertyFieldLabels) {
      accumulator[key as PropertyFormField] = value;
    }
    return accumulator;
  }, {});
}
