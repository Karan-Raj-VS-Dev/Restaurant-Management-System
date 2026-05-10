CREATE TABLE IF NOT EXISTS event_dispatches (
    dispatch_id VARCHAR(64) PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    event_key VARCHAR(128) NOT NULL,
    subscriber_name VARCHAR(128) NOT NULL,
    endpoint_url TEXT NOT NULL,
    dispatch_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    response_status_code INTEGER,
    attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    error_message TEXT,
    payload JSONB NOT NULL,
    dispatched_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS subscriber_endpoints (
    subscriber_id VARCHAR(64) PRIMARY KEY,
    subscriber_name VARCHAR(128) NOT NULL UNIQUE,
    endpoint_url TEXT NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_success_at TIMESTAMPTZ,
    last_failure_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
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

CREATE INDEX IF NOT EXISTS idx_event_dispatches_status_time ON event_dispatches(dispatch_status, dispatched_at);
CREATE INDEX IF NOT EXISTS idx_event_dispatches_event ON event_dispatches(event_id, event_key);
CREATE INDEX IF NOT EXISTS idx_subscriber_endpoints_active ON subscriber_endpoints(active);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
