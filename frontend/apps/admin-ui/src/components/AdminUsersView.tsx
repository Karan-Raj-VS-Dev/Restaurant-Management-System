import {
  createManagedUser,
  deleteManagedUser,
  isApiRequestError,
  loadManagedUsers,
  loadTenantProperties,
  updateManagedUser,
  type ManagedUserPayload,
  type ManagedUserRecord,
  type ManagedUserUpdatePayload,
  type PropertyRecord
} from "@restaurant/api";
import { Button, SectionCard, StatCard, StatusPill } from "@restaurant/ui";
import { FormEvent, useEffect, useMemo, useState } from "react";

interface AdminUsersViewProps {
  actorUsername: string;
}

interface UserFormState {
  firstName: string;
  lastName: string;
  username: string;
  temporaryPassword: string;
  phoneCountryCode: string;
  phoneNumber: string;
  email: string;
  addressLine: string;
  mappedPropertyIds: string[];
  latitude: string;
  longitude: string;
  status: string;
}

type PanelMode = "add" | "edit";
type BannerTone = "success" | "error";
type UserFormField = keyof UserFormState;
type UserFormErrors = Partial<Record<UserFormField | "form", string>>;

const countryCodes = ["+91", "+1", "+44", "+61", "+65", "+971"];
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const userFieldLabels: Record<UserFormField, string> = {
  firstName: "First name",
  lastName: "Last name",
  username: "Username",
  temporaryPassword: "Temporary password",
  phoneCountryCode: "Country code",
  phoneNumber: "Phone number",
  email: "Email",
  addressLine: "Address",
  mappedPropertyIds: "Property mapping",
  latitude: "Latitude",
  longitude: "Longitude",
  status: "Status"
};

function blankForm(): UserFormState {
  return {
    firstName: "",
    lastName: "",
    username: "",
    temporaryPassword: "",
    phoneCountryCode: "+91",
    phoneNumber: "",
    email: "",
    addressLine: "",
    mappedPropertyIds: [],
    latitude: "",
    longitude: "",
    status: "ACTIVE"
  };
}

export function AdminUsersView(props: AdminUsersViewProps) {
  const [users, setUsers] = useState<ManagedUserRecord[]>([]);
  const [properties, setProperties] = useState<PropertyRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);
  const [banner, setBanner] = useState<{ tone: BannerTone; message: string } | null>(null);
  const [panelMode, setPanelMode] = useState<PanelMode>("add");
  const [panelOpen, setPanelOpen] = useState(false);
  const [panelRevision, setPanelRevision] = useState(0);
  const [selectedUser, setSelectedUser] = useState<ManagedUserRecord | null>(null);
  const [propertyPickerValue, setPropertyPickerValue] = useState("");
  const [form, setForm] = useState<UserFormState>(blankForm());
  const [formErrors, setFormErrors] = useState<UserFormErrors>({});

  const activeUsers = useMemo(() => users.filter((user) => user.status === "ACTIVE").length, [users]);
  const activeProperties = useMemo(() => properties.filter((property) => property.status === "ACTIVE"), [properties]);

  const refreshData = async () => {
    setLoading(true);
    setPageError(null);
    try {
      const [usersResponse, propertyResponse] = await Promise.all([
        loadManagedUsers(props.actorUsername),
        loadTenantProperties()
      ]);
      setUsers(usersResponse.users);
      setProperties(propertyResponse);

      if (selectedUser) {
        const nextSelected = usersResponse.users.find((user) => user.userId === selectedUser.userId) ?? null;
        setSelectedUser(nextSelected);
        if (panelOpen && nextSelected) {
          setForm(toForm(nextSelected));
        }
      }
    } catch (caughtError) {
      setPageError(caughtError instanceof Error ? caughtError.message : "Unable to load admin user data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshData();
  }, [props.actorUsername]);

  const clearBannerIfError = () => {
    setBanner((current) => (current?.tone === "error" ? null : current));
  };

  const clearFormErrors = (...fields: Array<UserFormField | "form">) => {
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

  const updateField = <K extends UserFormField>(field: K, value: UserFormState[K]) => {
    setForm((current) => ({ ...current, [field]: value }));
    clearFormErrors(field, "form");
    clearBannerIfError();
  };

  const openAddPanel = () => {
    setSelectedUser(null);
    setPanelMode("add");
    setPanelOpen(true);
    setPanelRevision((current) => current + 1);
    setPropertyPickerValue("");
    setForm(blankForm());
    setFormErrors({});
    setBanner(null);
  };

  const openEditPanel = (user: ManagedUserRecord) => {
    setSelectedUser(user);
    setPanelMode("edit");
    setPanelOpen(true);
    setPanelRevision((current) => current + 1);
    setPropertyPickerValue("");
    setForm(toForm(user));
    setFormErrors({});
    setBanner(null);
  };

  const closePanel = () => {
    setPanelOpen(false);
    setSelectedUser(null);
    setPropertyPickerValue("");
    setFormErrors({});
    setBanner(null);
  };

  const addMappedProperty = (propertyId: string) => {
    if (!propertyId) {
      return;
    }
    setForm((current) => {
      if (current.mappedPropertyIds.includes(propertyId)) {
        return current;
      }
      return {
        ...current,
        mappedPropertyIds: [...current.mappedPropertyIds, propertyId]
      };
    });
    setPropertyPickerValue("");
    clearFormErrors("mappedPropertyIds", "form");
    clearBannerIfError();
  };

  const removeMappedProperty = (propertyId: string) => {
    setForm((current) => ({
      ...current,
      mappedPropertyIds: current.mappedPropertyIds.filter((value) => value !== propertyId)
    }));
    clearFormErrors("mappedPropertyIds", "form");
    clearBannerIfError();
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    const validationErrors = validateUserForm(form, panelMode);
    if (Object.keys(validationErrors).length > 0) {
      setFormErrors(validationErrors);
      setBanner({
        tone: "error",
        message: buildValidationBanner(validationErrors, userFieldLabels)
      });
      return;
    }

    setSubmitting(true);
    setBanner(null);
    clearFormErrors();
    try {
      if (panelMode === "add") {
        const payload: ManagedUserPayload = {
          firstName: form.firstName.trim(),
          lastName: form.lastName.trim(),
          username: form.username.trim(),
          temporaryPassword: form.temporaryPassword,
          phoneCountryCode: form.phoneCountryCode.trim(),
          phoneNumber: form.phoneNumber.trim(),
          email: form.email.trim(),
          addressLine: form.addressLine.trim(),
          mappedPropertyIds: form.mappedPropertyIds,
          latitude: parseNumber(form.latitude),
          longitude: parseNumber(form.longitude)
        };
        await createManagedUser(props.actorUsername, payload);
        setBanner({
          tone: "success",
          message: "Employee / User created successfully. The user can choose any mapped property after login."
        });
        setForm(blankForm());
        setPropertyPickerValue("");
      } else if (selectedUser) {
        const payload: ManagedUserUpdatePayload = {
          firstName: form.firstName.trim(),
          lastName: form.lastName.trim(),
          username: form.username.trim(),
          temporaryPassword: form.temporaryPassword.trim() || undefined,
          phoneCountryCode: form.phoneCountryCode.trim(),
          phoneNumber: form.phoneNumber.trim(),
          email: form.email.trim(),
          addressLine: form.addressLine.trim(),
          mappedPropertyIds: form.mappedPropertyIds,
          latitude: parseNumber(form.latitude),
          longitude: parseNumber(form.longitude),
          status: form.status
        };
        await updateManagedUser(props.actorUsername, selectedUser.userId, payload);
        setBanner({
          tone: "success",
          message: "Employee / User updated successfully."
        });
      }
      await refreshData();
    } catch (caughtError) {
      const nextErrors = mapUserFieldErrors(caughtError);
      if (Object.keys(nextErrors).length > 0) {
        setFormErrors(nextErrors);
      }
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to save user."
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedUser) {
      return;
    }
    const confirmed = window.confirm(`Delete ${selectedUser.fullName}?`);
    if (!confirmed) {
      return;
    }
    setSubmitting(true);
    setBanner(null);
    try {
      await deleteManagedUser(props.actorUsername, selectedUser.userId);
      await refreshData();
      openAddPanel();
      setBanner({
        tone: "success",
        message: "Employee / User deleted successfully."
      });
    } catch (caughtError) {
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to delete user."
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
      () => {
        setBanner({
          tone: "error",
          message: "Unable to capture your current location."
        });
      }
    );
  };

  return (
    <div className="admin-users-page">
      <div className="admin-users-stats">
        <StatCard label="Employees / Users" value={String(users.length)} hint="Operational accounts created by the admin console" tone="warm" />
        <StatCard label="Active accounts" value={String(activeUsers)} hint="Ready for sign-in today" tone="cool" />
        <StatCard
          label="Password resets pending"
          value={String(users.filter((user) => user.mustChangePassword).length)}
          hint="Temporary password users still need to rotate"
          tone="alert"
        />
      </div>

      {pageError ? <div className="admin-alert admin-alert-error">{pageError}</div> : null}

      <div className={`admin-users-layout ${panelOpen ? "panel-open" : "panel-closed"}`}>
        <SectionCard
          className="admin-users-table-card"
          title="Employee / User access dashboard"
          subtitle="Users can access all properties mapped here. After login, they choose one mapped property and continue with full dashboard access until role-based rules are added."
          action={
            <div className="admin-users-actions">
              <Button variant="ghost" onClick={() => void refreshData()}>
                Refresh
              </Button>
              <Button onClick={openAddPanel}>Add User</Button>
            </div>
          }
        >
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>User type</th>
                  <th>Username</th>
                  <th>Mapped properties</th>
                  <th>Email</th>
                  <th>Status</th>
                  <th>Last login</th>
                  <th className="admin-table-action-header" />
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={8} className="admin-empty-cell">
                      Loading users...
                    </td>
                  </tr>
                ) : users.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="admin-empty-cell">
                      No Employee / User accounts have been created yet.
                    </td>
                  </tr>
                ) : (
                  users.map((user) => (
                    <tr key={user.userId} className={selectedUser?.userId === user.userId && panelOpen ? "selected" : ""}>
                      <td>{user.fullName}</td>
                      <td>
                        <StatusPill tone={user.adminUser ? "warning" : "info"}>{user.adminUser ? "Admin" : "User"}</StatusPill>
                      </td>
                      <td>{user.username}</td>
                      <td>
                        <div className="admin-pill-list">
                          {user.mappedPropertyIds.map((propertyId) => (
                            <StatusPill key={`${user.userId}-${propertyId}`} tone="info">
                              {propertyNameFor(propertyId, properties)}
                            </StatusPill>
                          ))}
                        </div>
                      </td>
                      <td>{user.email}</td>
                      <td>
                        <StatusPill tone={user.status === "ACTIVE" ? "success" : "warning"}>{user.status}</StatusPill>
                      </td>
                      <td>{user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : "Never"}</td>
                      <td className="admin-table-action-cell">
                        <Button variant="ghost" onClick={() => openEditPanel(user)}>
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
            <div key={`${panelMode}-${selectedUser?.userId ?? "new"}-${panelRevision}`} className="admin-panel-frame">
              <button type="button" className="admin-panel-close" onClick={closePanel} aria-label="Close panel">
                ×
              </button>
              <SectionCard
                className="admin-panel-card"
                title={panelMode === "add" ? "Add Employee / User" : "Edit Employee / User"}
                subtitle={
                  panelMode === "add"
                    ? "Create operational access and map one or more properties."
                    : "Update operational access, mapped properties, and sign-in details."
                }
              >
                <form className="admin-form" onSubmit={handleSubmit}>
                  <div className="admin-form-grid">
                    <label>
                      First name
                      <input className={inputClass(formErrors.firstName)} value={form.firstName} onChange={(event) => updateField("firstName", event.target.value)} />
                      {formErrors.firstName ? <span className="admin-field-feedback">{formErrors.firstName}</span> : null}
                    </label>
                    <label>
                      Last name
                      <input className={inputClass(formErrors.lastName)} value={form.lastName} onChange={(event) => updateField("lastName", event.target.value)} />
                      {formErrors.lastName ? <span className="admin-field-feedback">{formErrors.lastName}</span> : null}
                    </label>
                  </div>

                  <label>
                    Username
                    <input className={inputClass(formErrors.username)} value={form.username} onChange={(event) => updateField("username", event.target.value)} />
                    {formErrors.username ? <span className="admin-field-feedback">{formErrors.username}</span> : null}
                  </label>

                  <label>
                    {panelMode === "add" ? "Temporary password" : "Temporary password (optional reset)"}
                    <input
                      className={inputClass(formErrors.temporaryPassword)}
                      type="password"
                      value={form.temporaryPassword}
                      onChange={(event) => updateField("temporaryPassword", event.target.value)}
                    />
                    {formErrors.temporaryPassword ? <span className="admin-field-feedback">{formErrors.temporaryPassword}</span> : null}
                  </label>

                  <label>
                    Phone number
                    <div className="admin-phone-input">
                      <select
                        className={inputClass(formErrors.phoneCountryCode)}
                        value={form.phoneCountryCode}
                        onChange={(event) => updateField("phoneCountryCode", event.target.value)}
                      >
                        {countryCodes.map((countryCode) => (
                          <option key={countryCode} value={countryCode}>
                            {countryCode}
                          </option>
                        ))}
                      </select>
                      <input
                        className={inputClass(formErrors.phoneNumber)}
                        value={form.phoneNumber}
                        onChange={(event) => updateField("phoneNumber", event.target.value)}
                      />
                    </div>
                    {formErrors.phoneCountryCode ? <span className="admin-field-feedback">{formErrors.phoneCountryCode}</span> : null}
                    {formErrors.phoneNumber ? <span className="admin-field-feedback">{formErrors.phoneNumber}</span> : null}
                  </label>

                  <label>
                    Email
                    <input className={inputClass(formErrors.email)} value={form.email} onChange={(event) => updateField("email", event.target.value)} />
                    {formErrors.email ? <span className="admin-field-feedback">{formErrors.email}</span> : null}
                  </label>

                  <label>
                    Address
                    <textarea className={`${inputClass(formErrors.addressLine)} admin-textarea`} value={form.addressLine} onChange={(event) => updateField("addressLine", event.target.value)} />
                    {formErrors.addressLine ? <span className="admin-field-feedback">{formErrors.addressLine}</span> : null}
                  </label>

                  <SectionCard className="admin-inline-section" title="Property mapping">
                    <div className="admin-property-chip-stack">
                      {form.mappedPropertyIds.length === 0 ? (
                        <p className="admin-inline-note">Select one or more properties to map this Employee / User.</p>
                      ) : (
                        form.mappedPropertyIds.map((propertyId) => (
                          <div key={propertyId} className="admin-selection-chip">
                            <div className="admin-selection-chip-copy">
                              <strong>{propertyNameFor(propertyId, properties)}</strong>
                            </div>
                            <button type="button" className="admin-selection-chip-close" onClick={() => removeMappedProperty(propertyId)} aria-label={`Remove ${propertyNameFor(propertyId, properties)}`}>
                              ×
                            </button>
                          </div>
                        ))
                      )}
                    </div>

                    <label>
                      Select property
                      <select
                        className={inputClass(formErrors.mappedPropertyIds)}
                        value={propertyPickerValue}
                        onChange={(event) => addMappedProperty(event.target.value)}
                      >
                        <option value="">Select property</option>
                        {activeProperties
                          .filter((property) => !form.mappedPropertyIds.includes(property.propertyId))
                          .map((property) => (
                            <option key={property.propertyId} value={property.propertyId}>
                              {property.name}
                            </option>
                          ))}
                      </select>
                      {formErrors.mappedPropertyIds ? <span className="admin-field-feedback">{formErrors.mappedPropertyIds}</span> : null}
                    </label>
                  </SectionCard>

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
                      {submitting ? "Saving..." : panelMode === "add" ? "Create User" : "Save changes"}
                    </Button>
                    <Button variant="ghost" onClick={panelMode === "add" ? openAddPanel : () => selectedUser && openEditPanel(selectedUser)}>
                      Clear
                    </Button>
                    {panelMode === "edit" ? (
                      <Button variant="ghost" onClick={handleDelete} disabled={submitting}>
                        Delete User
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

function toForm(user: ManagedUserRecord): UserFormState {
  return {
    firstName: user.firstName,
    lastName: user.lastName,
    username: user.username,
    temporaryPassword: "",
    phoneCountryCode: user.phoneCountryCode,
    phoneNumber: user.phoneNumber,
    email: user.email,
    addressLine: user.addressLine,
    mappedPropertyIds: user.mappedPropertyIds,
    latitude: user.latitude?.toString() ?? "",
    longitude: user.longitude?.toString() ?? "",
    status: user.status
  };
}

function propertyNameFor(propertyId: string | null, properties: PropertyRecord[]) {
  if (!propertyId) {
    return "Not mapped";
  }
  return properties.find((property) => property.propertyId === propertyId)?.name ?? propertyId;
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

function validateUserForm(form: UserFormState, panelMode: PanelMode): UserFormErrors {
  const errors: UserFormErrors = {};

  if (!form.firstName.trim()) {
    errors.firstName = "First name is required.";
  }
  if (!form.lastName.trim()) {
    errors.lastName = "Last name is required.";
  }
  if (!form.username.trim()) {
    errors.username = "Username is required.";
  }
  if (panelMode === "add" && form.temporaryPassword.trim().length < 8) {
    errors.temporaryPassword = "Temporary password must be at least 8 characters.";
  }
  if (panelMode === "edit" && form.temporaryPassword.trim() && form.temporaryPassword.trim().length < 8) {
    errors.temporaryPassword = "Temporary password must be at least 8 characters.";
  }
  if (!form.phoneCountryCode.trim()) {
    errors.phoneCountryCode = "Country code is required.";
  }
  if (!form.phoneNumber.trim()) {
    errors.phoneNumber = "Phone number is required.";
  }
  if (!form.email.trim()) {
    errors.email = "Email is required.";
  } else if (!emailPattern.test(form.email.trim())) {
    errors.email = "Enter a valid email address.";
  }
  if (!form.addressLine.trim()) {
    errors.addressLine = "Address is required.";
  }
  if (form.mappedPropertyIds.length === 0) {
    errors.mappedPropertyIds = "Select at least one property.";
  }
  if (form.latitude.trim() && parseNumber(form.latitude) === null) {
    errors.latitude = "Latitude must be a valid number.";
  }
  if (form.longitude.trim() && parseNumber(form.longitude) === null) {
    errors.longitude = "Longitude must be a valid number.";
  }

  return errors;
}

function buildValidationBanner(errors: UserFormErrors, labels: Record<UserFormField, string>) {
  const invalidFields = Object.keys(errors)
    .filter((key): key is UserFormField => key !== "form")
    .map((key) => labels[key]);

  if (invalidFields.length === 0) {
    return "Please correct the highlighted fields and try again.";
  }

  return `Please correct the highlighted fields: ${invalidFields.join(", ")}.`;
}

function mapUserFieldErrors(error: unknown): UserFormErrors {
  if (!isApiRequestError(error)) {
    return {};
  }

  return Object.entries(error.fieldErrors).reduce<UserFormErrors>((accumulator, [key, value]) => {
    if (key in userFieldLabels) {
      accumulator[key as UserFormField] = value;
    }
    return accumulator;
  }, {});
}
