CREATE TABLE IF NOT EXISTS marketplace_connectors (
    connector_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    marketplace_name VARCHAR(64) NOT NULL,
    connector_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    external_store_id VARCHAR(100),
    credentials_ref VARCHAR(150),
    menu_sync_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    order_ingestion_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, marketplace_name)
);

ALTER TABLE marketplace_connectors ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE marketplace_connectors
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

CREATE TABLE IF NOT EXISTS marketplace_order_inbox (
    marketplace_order_inbox_id VARCHAR(64) PRIMARY KEY,
    connector_id VARCHAR(64) NOT NULL REFERENCES marketplace_connectors(connector_id) ON DELETE CASCADE,
    external_order_id VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    ingestion_status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    mapped_takeaway_order_id VARCHAR(64),
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ,
    UNIQUE (connector_id, external_order_id)
);

CREATE TABLE IF NOT EXISTS marketplace_sync_log (
    sync_log_id VARCHAR(64) PRIMARY KEY,
    connector_id VARCHAR(64) NOT NULL REFERENCES marketplace_connectors(connector_id) ON DELETE CASCADE,
    sync_type VARCHAR(32) NOT NULL,
    sync_status VARCHAR(32) NOT NULL DEFAULT 'STARTED',
    request_payload JSONB,
    response_payload JSONB,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
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

CREATE INDEX IF NOT EXISTS idx_marketplace_connectors_property_status ON marketplace_connectors(property_id, connector_status);
CREATE INDEX IF NOT EXISTS idx_marketplace_connectors_tenant_property_status ON marketplace_connectors(tenant_id, property_id, connector_status);
CREATE INDEX IF NOT EXISTS idx_marketplace_order_inbox_connector_status ON marketplace_order_inbox(connector_id, ingestion_status);
CREATE INDEX IF NOT EXISTS idx_marketplace_sync_log_connector_time ON marketplace_sync_log(connector_id, started_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
