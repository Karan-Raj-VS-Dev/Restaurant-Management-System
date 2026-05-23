CREATE TABLE IF NOT EXISTS kitchen_tickets (
    ticket_id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL UNIQUE,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    table_id VARCHAR(64),
    ticket_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    assigned_cook_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    accepted_at TIMESTAMPTZ,
    ready_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ
);

ALTER TABLE kitchen_tickets ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE kitchen_tickets
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

CREATE TABLE IF NOT EXISTS kitchen_ticket_items (
    ticket_item_id VARCHAR(64) PRIMARY KEY,
    ticket_id VARCHAR(64) NOT NULL REFERENCES kitchen_tickets(ticket_id) ON DELETE CASCADE,
    order_item_id VARCHAR(64) NOT NULL,
    menu_item_id VARCHAR(64) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    prep_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    recipe_snapshot JSONB,
    started_at TIMESTAMPTZ,
    ready_at TIMESTAMPTZ
);

ALTER TABLE kitchen_tickets DROP COLUMN IF EXISTS station_name;
ALTER TABLE kitchen_tickets DROP COLUMN IF EXISTS priority_level;
DROP TABLE IF EXISTS kitchen_status_history;

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

CREATE INDEX IF NOT EXISTS idx_kitchen_tickets_property_status ON kitchen_tickets(property_id, ticket_status);
CREATE INDEX IF NOT EXISTS idx_kitchen_tickets_tenant_property_status ON kitchen_tickets(tenant_id, property_id, ticket_status);
CREATE INDEX IF NOT EXISTS idx_kitchen_tickets_cook_status ON kitchen_tickets(assigned_cook_id, ticket_status);
CREATE INDEX IF NOT EXISTS idx_kitchen_ticket_items_ticket_status ON kitchen_ticket_items(ticket_id, prep_status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
