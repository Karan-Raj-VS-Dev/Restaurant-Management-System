CREATE TABLE IF NOT EXISTS restaurant_tables (
    table_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    table_number VARCHAR(32) NOT NULL,
    floor_name VARCHAR(64),
    section_name VARCHAR(64),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    status VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, table_number)
);

CREATE TABLE IF NOT EXISTS table_sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    table_id VARCHAR(64) NOT NULL REFERENCES restaurant_tables(table_id) ON DELETE CASCADE,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    customer_count INTEGER NOT NULL DEFAULT 0 CHECK (customer_count >= 0),
    assigned_waiter_id VARCHAR(64),
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ,
    session_status VARCHAR(32) NOT NULL DEFAULT 'OPEN'
);

ALTER TABLE restaurant_tables ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE table_sessions ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE restaurant_tables
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE table_sessions
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

CREATE TABLE IF NOT EXISTS table_status_history (
    history_id VARCHAR(64) PRIMARY KEY,
    table_id VARCHAR(64) NOT NULL REFERENCES restaurant_tables(table_id) ON DELETE CASCADE,
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    changed_by VARCHAR(64),
    reason TEXT,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
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

CREATE INDEX IF NOT EXISTS idx_restaurant_tables_property_status ON restaurant_tables(property_id, status);
CREATE INDEX IF NOT EXISTS idx_restaurant_tables_tenant_property_status ON restaurant_tables(tenant_id, property_id, status);
CREATE INDEX IF NOT EXISTS idx_table_sessions_table_status ON table_sessions(table_id, session_status);
CREATE INDEX IF NOT EXISTS idx_table_sessions_property_waiter ON table_sessions(property_id, assigned_waiter_id);
CREATE INDEX IF NOT EXISTS idx_table_sessions_tenant_property_waiter ON table_sessions(tenant_id, property_id, assigned_waiter_id);
CREATE INDEX IF NOT EXISTS idx_table_status_history_table_time ON table_status_history(table_id, changed_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
