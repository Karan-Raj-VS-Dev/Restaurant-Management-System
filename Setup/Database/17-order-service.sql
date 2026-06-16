CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    table_id VARCHAR(64),
    session_id VARCHAR(64),
    waiter_id VARCHAR(64),
    order_type VARCHAR(32) NOT NULL DEFAULT 'DINE_IN',
    order_status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    special_instructions TEXT,
    served_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    cancellation_reason TEXT,
    ordered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE orders ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS served_at TIMESTAMPTZ;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMPTZ;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;
ALTER TABLE orders DROP COLUMN IF EXISTS customer_id;
ALTER TABLE orders DROP COLUMN IF EXISTS guest_count;
ALTER TABLE orders DROP COLUMN IF EXISTS subtotal_amount;
ALTER TABLE orders DROP COLUMN IF EXISTS tax_amount;
ALTER TABLE orders DROP COLUMN IF EXISTS discount_amount;
ALTER TABLE orders DROP COLUMN IF EXISTS total_amount;

UPDATE orders
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    menu_item_id VARCHAR(64) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0),
    item_status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE order_items DROP COLUMN IF EXISTS tax_id;

DROP TABLE IF EXISTS order_status_history;

CREATE TABLE IF NOT EXISTS order_status_history (
    order_id VARCHAR(64) PRIMARY KEY REFERENCES orders(order_id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    status_trail JSONB NOT NULL DEFAULT '[]'::jsonb,
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

CREATE INDEX IF NOT EXISTS idx_orders_property_status ON orders(property_id, order_status);
CREATE INDEX IF NOT EXISTS idx_orders_tenant_property_status ON orders(tenant_id, property_id, order_status);
CREATE INDEX IF NOT EXISTS idx_orders_table_status ON orders(table_id, order_status);
CREATE INDEX IF NOT EXISTS idx_orders_waiter_ordered_at ON orders(waiter_id, ordered_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item ON order_items(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_order_status_history_updated_at ON order_status_history(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
