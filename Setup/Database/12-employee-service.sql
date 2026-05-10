CREATE TABLE IF NOT EXISTS employees (
    employee_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255),
    phone_number VARCHAR(32),
    role_code VARCHAR(50) NOT NULL,
    shift_name VARCHAR(64) NOT NULL DEFAULT 'Normal Shift',
    salary_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    employment_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    available BOOLEAN NOT NULL DEFAULT TRUE,
    hire_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS employee_shifts (
    shift_id VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    shift_name VARCHAR(64) NOT NULL DEFAULT 'Normal Shift',
    shift_date DATE NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    shift_status VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE employees ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE employees ADD COLUMN IF NOT EXISTS shift_name VARCHAR(64) NOT NULL DEFAULT 'Normal Shift';
ALTER TABLE employees ADD COLUMN IF NOT EXISTS salary_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE employee_shifts ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE employee_shifts ADD COLUMN IF NOT EXISTS shift_name VARCHAR(64) NOT NULL DEFAULT 'Normal Shift';

UPDATE employees
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE employee_shifts
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

INSERT INTO employees (
    employee_id,
    tenant_id,
    property_id,
    employee_code,
    full_name,
    email,
    phone_number,
    role_code,
    shift_name,
    salary_amount,
    employment_status,
    available,
    hire_date
) VALUES (
    'emp-neha-001',
    'bikini-bottom',
    'krusty-krab',
    'EMP-NEHA-001',
    'Neha',
    'neha@krustykrab.local',
    '+919999999999',
    'WAITER',
    'Normal Shift',
    15000.00,
    'ACTIVE',
    TRUE,
    CURRENT_DATE
)
ON CONFLICT (employee_id) DO UPDATE SET
    tenant_id = EXCLUDED.tenant_id,
    property_id = EXCLUDED.property_id,
    employee_code = EXCLUDED.employee_code,
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    phone_number = EXCLUDED.phone_number,
    role_code = EXCLUDED.role_code,
    shift_name = EXCLUDED.shift_name,
    salary_amount = EXCLUDED.salary_amount,
    employment_status = EXCLUDED.employment_status,
    available = EXCLUDED.available;

INSERT INTO employee_shifts (
    shift_id,
    employee_id,
    tenant_id,
    property_id,
    shift_name,
    shift_date,
    start_time,
    end_time,
    shift_status
) VALUES (
    'shift-neha-normal',
    'emp-neha-001',
    'bikini-bottom',
    'krusty-krab',
    'Normal Shift',
    CURRENT_DATE,
    date_trunc('day', NOW()) + INTERVAL '9 hours',
    date_trunc('day', NOW()) + INTERVAL '18 hours',
    'SCHEDULED'
)
ON CONFLICT (shift_id) DO UPDATE SET
    employee_id = EXCLUDED.employee_id,
    tenant_id = EXCLUDED.tenant_id,
    property_id = EXCLUDED.property_id,
    shift_name = EXCLUDED.shift_name,
    shift_date = EXCLUDED.shift_date,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    shift_status = EXCLUDED.shift_status;

CREATE TABLE IF NOT EXISTS employee_availability_history (
    history_id VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL REFERENCES employees(employee_id) ON DELETE CASCADE,
    previous_available BOOLEAN,
    new_available BOOLEAN NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    changed_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    event_id VARCHAR(64) PRIMARY KEY,
    event_key VARCHAR(128) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    partition_key VARCHAR(128),
    causation_id VARCHAR(64),
    correlation_id VARCHAR(64),
    payload JSONB NOT NULL,
    producer VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS inbox_events (
    event_id VARCHAR(64) PRIMARY KEY,
    event_key VARCHAR(128) NOT NULL,
    consumer VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    error_message TEXT,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_employees_property_role ON employees(property_id, role_code);
CREATE INDEX IF NOT EXISTS idx_employees_tenant_property_role ON employees(tenant_id, property_id, role_code);
CREATE INDEX IF NOT EXISTS idx_employees_status_available ON employees(employment_status, available);
CREATE INDEX IF NOT EXISTS idx_employee_shifts_employee_date ON employee_shifts(employee_id, shift_date);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
