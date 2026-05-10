import {
  createManagedEmployee,
  deleteManagedEmployee,
  isApiRequestError,
  loadPropertyEmployees,
  updateManagedEmployee,
  type EmployeeRecord,
  type ManagedEmployeePayload,
  type PropertyRecord
} from "@restaurant/api";
import { Button, SectionCard, StatCard, StatusPill } from "@restaurant/ui";
import { FormEvent, useEffect, useMemo, useState } from "react";

interface EmployeeManagementPageProps {
  selectedProperty: PropertyRecord;
}

interface EmployeeFormState {
  name: string;
  role: string;
  phoneCountryCode: string;
  email: string;
  phoneNumber: string;
  shiftName: string;
  salaryAmount: string;
  employmentStatus: string;
}

type PanelMode = "add" | "edit";
type BannerTone = "success" | "error";
type EmployeeFormField = keyof EmployeeFormState;
type EmployeeFormErrors = Partial<Record<EmployeeFormField | "form", string>>;

const roleOptions = ["HOST", "WAITER", "COOK", "CASHIER", "CLEANER"];
const countryCodes = ["+91", "+1", "+44", "+61", "+65", "+971"];
const shiftOptions = ["Normal Shift", "Morning Shift", "Evening Shift", "Night Shift", "Split Shift"];
const employeeFieldLabels: Record<EmployeeFormField, string> = {
  name: "Employee name",
  role: "Role",
  phoneCountryCode: "Country code",
  email: "Email",
  phoneNumber: "Phone number",
  shiftName: "Shift",
  salaryAmount: "Salary",
  employmentStatus: "Status"
};

function blankForm(): EmployeeFormState {
  return {
    name: "",
    role: "WAITER",
    phoneCountryCode: "+91",
    email: "",
    phoneNumber: "",
    shiftName: "Normal Shift",
    salaryAmount: "",
    employmentStatus: "ACTIVE"
  };
}

export function EmployeeManagementPage(props: EmployeeManagementPageProps) {
  const [employees, setEmployees] = useState<EmployeeRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);
  const [banner, setBanner] = useState<{ tone: BannerTone; message: string } | null>(null);
  const [panelMode, setPanelMode] = useState<PanelMode>("add");
  const [panelOpen, setPanelOpen] = useState(false);
  const [panelRevision, setPanelRevision] = useState(0);
  const [selectedEmployee, setSelectedEmployee] = useState<EmployeeRecord | null>(null);
  const [form, setForm] = useState<EmployeeFormState>(blankForm());
  const [formErrors, setFormErrors] = useState<EmployeeFormErrors>({});
  const [salaryVisibility, setSalaryVisibility] = useState<Record<string, boolean>>({});
  const [availabilityUpdatingId, setAvailabilityUpdatingId] = useState<string | null>(null);

  const activeEmployees = useMemo(
    () => employees.filter((employee) => (employee.employmentStatus ?? "ACTIVE") === "ACTIVE").length,
    [employees]
  );
  const availableEmployees = useMemo(() => employees.filter((employee) => employee.available).length, [employees]);
  const shiftCoverage = useMemo(() => {
    const counts = new Map<string, number>();
    for (const employee of employees) {
      const shiftName = employee.shiftName?.trim() || "Normal Shift";
      counts.set(shiftName, (counts.get(shiftName) ?? 0) + 1);
    }

    return Array.from(counts.entries()).sort(([left], [right]) => left.localeCompare(right));
  }, [employees]);

  const refreshData = async () => {
    setLoading(true);
    setPageError(null);
    try {
      const response = await loadPropertyEmployees();
      setEmployees(response);

      if (selectedEmployee) {
        const nextSelected = response.find((employee) => employee.employeeId === selectedEmployee.employeeId) ?? null;
        setSelectedEmployee(nextSelected);
        if (panelOpen && nextSelected) {
          setForm(toForm(nextSelected));
        }
      }
    } catch (caughtError) {
      setPageError(caughtError instanceof Error ? caughtError.message : "Unable to load employees for this property.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void refreshData();
  }, [props.selectedProperty.propertyId]);

  const updateField = <K extends EmployeeFormField>(field: K, value: EmployeeFormState[K]) => {
    setForm((current) => ({ ...current, [field]: value }));
    clearFormErrors(field, "form");
    clearBannerIfError();
  };

  const clearBannerIfError = () => {
    setBanner((current) => (current?.tone === "error" ? null : current));
  };

  const clearFormErrors = (...fields: Array<EmployeeFormField | "form">) => {
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

  const openAddPanel = () => {
    setSelectedEmployee(null);
    setPanelMode("add");
    setPanelOpen(true);
    setPanelRevision((current) => current + 1);
    setForm(blankForm());
    setFormErrors({});
    setBanner(null);
  };

  const openEditPanel = (employee: EmployeeRecord) => {
    setSelectedEmployee(employee);
    setPanelMode("edit");
    setPanelOpen(true);
    setPanelRevision((current) => current + 1);
    setForm(toForm(employee));
    setFormErrors({});
    setBanner(null);
  };

  const closePanel = () => {
    setPanelOpen(false);
    setSelectedEmployee(null);
    setFormErrors({});
    setBanner(null);
  };

  const toggleSalary = (employeeId: string) => {
    setSalaryVisibility((current) => ({ ...current, [employeeId]: !current[employeeId] }));
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    const validationErrors = validateEmployeeForm(form);
    if (Object.keys(validationErrors).length > 0) {
      setFormErrors(validationErrors);
      setBanner({
        tone: "error",
        message: buildValidationBanner(validationErrors)
      });
      return;
    }

    setSubmitting(true);
    setBanner(null);
    clearFormErrors();
    try {
      const payload: ManagedEmployeePayload = {
        name: form.name.trim(),
        role: form.role,
        email: form.email.trim() || null,
        phoneNumber: combinePhoneNumber(form.phoneCountryCode, form.phoneNumber),
        shiftName: form.shiftName.trim(),
        salaryAmount: Number(form.salaryAmount),
        available: selectedEmployee?.available ?? true,
        employmentStatus: form.employmentStatus
      };

      if (panelMode === "add") {
        await createManagedEmployee(payload);
        setBanner({
          tone: "success",
          message: "Employee created successfully."
        });
        setForm(blankForm());
      } else if (selectedEmployee) {
        await updateManagedEmployee(selectedEmployee.employeeId, payload);
        setBanner({
          tone: "success",
          message: "Employee updated successfully."
        });
      }

      await refreshData();
    } catch (caughtError) {
      const nextErrors = mapEmployeeFieldErrors(caughtError);
      if (Object.keys(nextErrors).length > 0) {
        setFormErrors(nextErrors);
      }
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to save employee."
      });
    } finally {
      setSubmitting(false);
    }
  };

  const handleAvailabilityToggle = async (employee: EmployeeRecord) => {
    setAvailabilityUpdatingId(employee.employeeId);
    setPageError(null);
    setBanner(null);
    try {
      await updateManagedEmployee(employee.employeeId, {
        name: employee.name,
        role: employee.role,
        email: employee.email ?? null,
        phoneNumber: employee.phoneNumber ?? null,
        shiftName: employee.shiftName ?? "Normal Shift",
        salaryAmount: employee.salaryAmount ?? 0,
        available: !employee.available,
        employmentStatus: employee.employmentStatus ?? "ACTIVE"
      });
      await refreshData();
      setBanner({
        tone: "success",
        message: `${employee.name} is now marked as ${employee.available ? "Unavailable" : "Available"}.`
      });
    } catch (caughtError) {
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to update employee availability."
      });
    } finally {
      setAvailabilityUpdatingId(null);
    }
  };

  const handleDelete = async () => {
    if (!selectedEmployee) {
      return;
    }
    const confirmed = window.confirm(`Delete ${selectedEmployee.name}?`);
    if (!confirmed) {
      return;
    }

    setSubmitting(true);
    setBanner(null);
    try {
      await deleteManagedEmployee(selectedEmployee.employeeId);
      await refreshData();
      openAddPanel();
      setBanner({
        tone: "success",
        message: "Employee deleted successfully."
      });
    } catch (caughtError) {
      setBanner({
        tone: "error",
        message: caughtError instanceof Error ? caughtError.message : "Unable to delete employee."
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="admin-users-page">
      <div className="admin-users-stats">
        <StatCard label="Employees" value={String(employees.length)} hint={`Current team for ${props.selectedProperty.name}`} tone="warm" />
        <StatCard label="Available now" value={String(availableEmployees)} hint="Ready for active assignments" tone="cool" />
        <StatCard
          label="Shift coverage"
          value={shiftCoverage.length === 0 ? "No shifts" : `${shiftCoverage.length} shifts`}
          hint={shiftCoverage.length === 0 ? "No employees have been assigned to shifts yet" : shiftCoverage.map(([shiftName, count]) => `${shiftName}: ${count}`).join(" · ")}
          tone="alert"
        />
      </div>

      {pageError ? <div className="admin-alert admin-alert-error">{pageError}</div> : null}

      <div className={`admin-users-layout ${panelOpen ? "panel-open" : "panel-closed"}`}>
        <SectionCard
          className="admin-users-table-card"
          title="Employee management"
          subtitle="Create and manage property-specific employees. Salary is hidden in the table until you choose to reveal it."
          action={
            <div className="admin-users-actions">
              <Button variant="ghost" onClick={() => void refreshData()}>
                Refresh
              </Button>
              <Button onClick={openAddPanel}>Add employee</Button>
            </div>
          }
        >
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Role</th>
                  <th>Shift</th>
                  <th>Availability</th>
                  <th>Salary</th>
                  <th>Contact number</th>
                  <th>Email</th>
                  <th>Status</th>
                  <th className="admin-table-action-header" />
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={9} className="admin-empty-cell">
                      Loading employees...
                    </td>
                  </tr>
                ) : employees.length === 0 ? (
                  <tr>
                    <td colSpan={9} className="admin-empty-cell">
                      No employees have been created for this property yet.
                    </td>
                  </tr>
                ) : (
                  employees.map((employee) => (
                    <tr key={employee.employeeId} className={selectedEmployee?.employeeId === employee.employeeId && panelOpen ? "selected" : ""}>
                      <td>{employee.name}</td>
                      <td>
                        <StatusPill tone="info">{employee.role}</StatusPill>
                      </td>
                      <td>{employee.shiftName ?? "Normal Shift"}</td>
                      <td>
                        <button
                          type="button"
                          className={`employee-availability-toggle ${employee.available ? "is-available" : "is-unavailable"}`}
                          onClick={() => void handleAvailabilityToggle(employee)}
                          disabled={availabilityUpdatingId === employee.employeeId}
                          aria-label={`${employee.available ? "Mark unavailable" : "Mark available"} for ${employee.name}`}
                        >
                          {availabilityUpdatingId === employee.employeeId ? "Updating..." : employee.available ? "Available" : "Unavailable"}
                        </button>
                      </td>
                      <td>
                        <button type="button" className="admin-inline-link" onClick={() => toggleSalary(employee.employeeId)}>
                          {salaryVisibility[employee.employeeId] ? `Rs ${formatSalary(employee.salaryAmount ?? 0)}` : "Show Salary"}
                        </button>
                      </td>
                      <td>{employee.phoneNumber || "Not added"}</td>
                      <td>{employee.email || "Not added"}</td>
                      <td>
                        <StatusPill tone={(employee.employmentStatus ?? "ACTIVE") === "ACTIVE" ? "success" : "warning"}>
                          {employee.employmentStatus ?? "ACTIVE"}
                        </StatusPill>
                      </td>
                      <td className="admin-table-action-cell">
                        <Button variant="ghost" onClick={() => openEditPanel(employee)}>
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
            <div key={`${panelMode}-${selectedEmployee?.employeeId ?? "new"}-${panelRevision}`} className="admin-panel-frame">
              <button type="button" className="admin-panel-close" onClick={closePanel} aria-label="Close panel">
                ×
              </button>
              <SectionCard
                className="admin-panel-card"
                title={panelMode === "add" ? "Add employee" : "Edit employee"}
                subtitle={
                  panelMode === "add"
                    ? `Create a new employee for ${props.selectedProperty.name}.`
                    : "Update the employee profile, shift, salary, and status."
                }
              >
                <form className="admin-form" onSubmit={handleSubmit}>
                  <label>
                    Employee name
                    <input className={inputClass(formErrors.name)} value={form.name} onChange={(event) => updateField("name", event.target.value)} />
                    {formErrors.name ? <span className="admin-field-feedback">{formErrors.name}</span> : null}
                  </label>

                  <div className="admin-form-grid">
                    <label>
                      Role
                      <select className={inputClass(formErrors.role)} value={form.role} onChange={(event) => updateField("role", event.target.value)}>
                        {roleOptions.map((role) => (
                          <option key={role} value={role}>
                            {role}
                          </option>
                        ))}
                      </select>
                      {formErrors.role ? <span className="admin-field-feedback">{formErrors.role}</span> : null}
                    </label>
                    <label>
                      Shift
                      <select className={inputClass(formErrors.shiftName)} value={form.shiftName} onChange={(event) => updateField("shiftName", event.target.value)}>
                        {shiftOptions.map((shift) => (
                          <option key={shift} value={shift}>
                            {shift}
                          </option>
                        ))}
                      </select>
                      {formErrors.shiftName ? <span className="admin-field-feedback">{formErrors.shiftName}</span> : null}
                    </label>
                  </div>

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
                      <input className={inputClass(formErrors.phoneNumber)} value={form.phoneNumber} onChange={(event) => updateField("phoneNumber", event.target.value)} />
                    </div>
                    {formErrors.phoneCountryCode ? <span className="admin-field-feedback">{formErrors.phoneCountryCode}</span> : null}
                    {formErrors.phoneNumber ? <span className="admin-field-feedback">{formErrors.phoneNumber}</span> : null}
                  </label>

                  <label>
                    Email
                    <input className={inputClass(formErrors.email)} value={form.email} onChange={(event) => updateField("email", event.target.value)} />
                    {formErrors.email ? <span className="admin-field-feedback">{formErrors.email}</span> : null}
                  </label>

                  <div className="admin-form-grid">
                    <label>
                      Salary (Rs)
                      <input className={inputClass(formErrors.salaryAmount)} value={form.salaryAmount} onChange={(event) => updateField("salaryAmount", event.target.value)} />
                      {formErrors.salaryAmount ? <span className="admin-field-feedback">{formErrors.salaryAmount}</span> : null}
                    </label>
                    <label>
                      Status
                      <select className={inputClass(formErrors.employmentStatus)} value={form.employmentStatus} onChange={(event) => updateField("employmentStatus", event.target.value)}>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                      </select>
                      {formErrors.employmentStatus ? <span className="admin-field-feedback">{formErrors.employmentStatus}</span> : null}
                    </label>
                  </div>

                  {banner ? <div className={`admin-alert admin-form-alert ${banner.tone === "success" ? "admin-alert-info" : "admin-alert-error"}`}>{banner.message}</div> : null}

                  <div className="admin-form-actions">
                    <Button type="submit" disabled={submitting}>
                      {submitting ? "Saving..." : panelMode === "add" ? "Create employee" : "Save changes"}
                    </Button>
                    <Button variant="ghost" onClick={panelMode === "add" ? openAddPanel : () => selectedEmployee && openEditPanel(selectedEmployee)}>
                      Clear
                    </Button>
                    {panelMode === "edit" ? (
                      <Button variant="ghost" onClick={handleDelete} disabled={submitting}>
                        Delete employee
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

function toForm(employee: EmployeeRecord): EmployeeFormState {
  const parsedPhone = splitPhoneNumber(employee.phoneNumber);
  return {
    name: employee.name,
    role: employee.role,
    phoneCountryCode: parsedPhone.countryCode,
    email: employee.email ?? "",
    phoneNumber: parsedPhone.localNumber,
    shiftName: employee.shiftName ?? "Normal Shift",
    salaryAmount: employee.salaryAmount != null ? String(employee.salaryAmount) : "",
    employmentStatus: employee.employmentStatus ?? "ACTIVE"
  };
}

function inputClass(error?: string) {
  return error ? "admin-input admin-input-invalid" : "admin-input";
}

function validateEmployeeForm(form: EmployeeFormState): EmployeeFormErrors {
  const errors: EmployeeFormErrors = {};

  if (!form.name.trim()) {
    errors.name = "Employee name is required.";
  }
  if (!form.role.trim()) {
    errors.role = "Role is required.";
  }
  if (form.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = "Enter a valid email address.";
  }
  if (!form.shiftName.trim()) {
    errors.shiftName = "Shift name is required.";
  }
  if (!form.salaryAmount.trim()) {
    errors.salaryAmount = "Salary is required.";
  } else {
    const salary = Number(form.salaryAmount);
    if (!Number.isFinite(salary) || salary < 0) {
      errors.salaryAmount = "Salary must be a valid positive number.";
    }
  }

  return errors;
}

function buildValidationBanner(errors: EmployeeFormErrors) {
  const invalidFields = Object.keys(errors)
    .filter((key): key is EmployeeFormField => key !== "form")
    .map((key) => employeeFieldLabels[key]);

  if (invalidFields.length === 0) {
    return "Please correct the highlighted fields and try again.";
  }

  return `Please correct the highlighted fields: ${invalidFields.join(", ")}.`;
}

function mapEmployeeFieldErrors(error: unknown): EmployeeFormErrors {
  if (!isApiRequestError(error)) {
    return {};
  }

  return Object.entries(error.fieldErrors).reduce<EmployeeFormErrors>((accumulator, [key, value]) => {
    if (key in employeeFieldLabels) {
      accumulator[key as EmployeeFormField] = value;
    }
    return accumulator;
  }, {});
}

function formatSalary(amount: number) {
  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0
  }).format(amount);
}

function combinePhoneNumber(countryCode: string, phoneNumber: string) {
  const trimmedNumber = phoneNumber.trim();
  if (!trimmedNumber) {
    return null;
  }
  const trimmedCode = countryCode.trim();
  return trimmedCode ? `${trimmedCode} ${trimmedNumber}` : trimmedNumber;
}

function splitPhoneNumber(phoneNumber?: string | null) {
  const raw = phoneNumber?.trim();
  if (!raw) {
    return {
      countryCode: "+91",
      localNumber: ""
    };
  }

  const matchedCode = countryCodes.find((countryCode) => raw === countryCode || raw.startsWith(`${countryCode} `));
  if (!matchedCode) {
    return {
      countryCode: "+91",
      localNumber: raw
    };
  }

  return {
    countryCode: matchedCode,
    localNumber: raw.slice(matchedCode.length).trim()
  };
}
