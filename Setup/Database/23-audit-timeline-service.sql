CREATE TABLE IF NOT EXISTS audit_events (
    audit_event_id VARCHAR(64) PRIMARY KEY,
    event_id VARCHAR(64),
    event_key VARCHAR(128) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64),
    property_id VARCHAR(64),
    correlation_id VARCHAR(64),
    causation_id VARCHAR(64),
    producer VARCHAR(128) NOT NULL,
    actor_id VARCHAR(64),
    actor_type VARCHAR(64),
    event_timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    payload JSONB NOT NULL
);

ALTER TABLE audit_events ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64);

CREATE TABLE IF NOT EXISTS audit_timeline (
    timeline_id VARCHAR(64) PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_key VARCHAR(128) NOT NULL,
    status_label VARCHAR(64),
    message TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    metadata JSONB
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

CREATE INDEX IF NOT EXISTS idx_audit_events_aggregate_time ON audit_events(aggregate_type, aggregate_id, event_timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_events_property_time ON audit_events(property_id, event_timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_events_tenant_property_time ON audit_events(tenant_id, property_id, event_timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_timeline_aggregate_time ON audit_timeline(aggregate_type, aggregate_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
