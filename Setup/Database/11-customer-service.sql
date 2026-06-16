CREATE TABLE IF NOT EXISTS customers (
    customer_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64),
    full_name VARCHAR(150),
    phone_number VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE customers ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE customers DROP COLUMN IF EXISTS loyalty_number;
ALTER TABLE customers DROP COLUMN IF EXISTS email;
ALTER TABLE customers DROP COLUMN IF EXISTS notes;
ALTER TABLE customers ALTER COLUMN full_name DROP NOT NULL;

UPDATE customers
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

DROP TABLE IF EXISTS customer_addresses;
DROP TABLE IF EXISTS customer_tags;

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

CREATE INDEX IF NOT EXISTS idx_customers_property ON customers(property_id);
CREATE INDEX IF NOT EXISTS idx_customers_tenant_property ON customers(tenant_id, property_id);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone_number);
CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_tenant_property_phone_unique ON customers(tenant_id, property_id, phone_number);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
