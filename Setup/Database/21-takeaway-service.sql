CREATE TABLE IF NOT EXISTS takeaway_orders (
    takeaway_order_id VARCHAR(64) PRIMARY KEY,
    external_order_reference VARCHAR(100),
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    customer_id VARCHAR(64),
    customer_name VARCHAR(150) NOT NULL,
    customer_phone VARCHAR(32),
    fulfillment_type VARCHAR(32) NOT NULL DEFAULT 'DELIVERY',
    source_channel VARCHAR(32) NOT NULL DEFAULT 'DIRECT',
    takeaway_status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    subtotal_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (subtotal_amount >= 0),
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    delivery_fee NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (delivery_fee >= 0),
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    delivery_address JSONB,
    promised_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE takeaway_orders ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE takeaway_orders
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

CREATE TABLE IF NOT EXISTS takeaway_order_items (
    takeaway_order_item_id VARCHAR(64) PRIMARY KEY,
    takeaway_order_id VARCHAR(64) NOT NULL REFERENCES takeaway_orders(takeaway_order_id) ON DELETE CASCADE,
    menu_item_id VARCHAR(64) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0)
);

CREATE TABLE IF NOT EXISTS takeaway_status_history (
    history_id VARCHAR(64) PRIMARY KEY,
    takeaway_order_id VARCHAR(64) NOT NULL REFERENCES takeaway_orders(takeaway_order_id) ON DELETE CASCADE,
    previous_status VARCHAR(32),
    new_status VARCHAR(32) NOT NULL,
    remarks TEXT,
    changed_by VARCHAR(64),
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

CREATE INDEX IF NOT EXISTS idx_takeaway_orders_property_status ON takeaway_orders(property_id, takeaway_status);
CREATE INDEX IF NOT EXISTS idx_takeaway_orders_tenant_property_status ON takeaway_orders(tenant_id, property_id, takeaway_status);
CREATE INDEX IF NOT EXISTS idx_takeaway_orders_channel_status ON takeaway_orders(source_channel, takeaway_status);
CREATE INDEX IF NOT EXISTS idx_takeaway_order_items_order ON takeaway_order_items(takeaway_order_id);
CREATE INDEX IF NOT EXISTS idx_takeaway_status_history_order_time ON takeaway_status_history(takeaway_order_id, changed_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
