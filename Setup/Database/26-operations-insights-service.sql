CREATE TABLE IF NOT EXISTS daily_operations_insights (
    insight_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    total_orders INTEGER NOT NULL DEFAULT 0 CHECK (total_orders >= 0),
    dine_in_orders INTEGER NOT NULL DEFAULT 0 CHECK (dine_in_orders >= 0),
    takeaway_orders INTEGER NOT NULL DEFAULT 0 CHECK (takeaway_orders >= 0),
    completed_payments INTEGER NOT NULL DEFAULT 0 CHECK (completed_payments >= 0),
    busiest_table_id VARCHAR(64),
    busiest_table_customer_count INTEGER NOT NULL DEFAULT 0 CHECK (busiest_table_customer_count >= 0),
    top_server_id VARCHAR(64),
    top_server_customer_count INTEGER NOT NULL DEFAULT 0 CHECK (top_server_customer_count >= 0),
    kitchen_active_tickets INTEGER NOT NULL DEFAULT 0 CHECK (kitchen_active_tickets >= 0),
    total_revenue NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (total_revenue >= 0),
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, business_date)
);

CREATE TABLE IF NOT EXISTS stock_insights (
    stock_insight_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    ingredient_id VARCHAR(64) NOT NULL,
    ingredient_name VARCHAR(150) NOT NULL,
    current_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (current_quantity >= 0),
    reserved_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    available_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    reorder_level NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (reorder_level >= 0),
    low_stock BOOLEAN NOT NULL DEFAULT FALSE,
    stock_out BOOLEAN NOT NULL DEFAULT FALSE,
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, business_date, ingredient_id)
);

CREATE TABLE IF NOT EXISTS menu_item_stock_impact (
    menu_item_stock_impact_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    menu_item_id VARCHAR(64) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    is_sellable BOOLEAN NOT NULL DEFAULT TRUE,
    blocking_ingredient_id VARCHAR(64),
    blocking_ingredient_name VARCHAR(150),
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, business_date, menu_item_id)
);

ALTER TABLE daily_operations_insights ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE stock_insights ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE menu_item_stock_impact ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE daily_operations_insights
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE stock_insights
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE menu_item_stock_impact
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

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

CREATE INDEX IF NOT EXISTS idx_daily_operations_insights_property_date ON daily_operations_insights(property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_daily_operations_insights_tenant_property_date ON daily_operations_insights(tenant_id, property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_stock_insights_property_date ON stock_insights(property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_stock_insights_tenant_property_date ON stock_insights(tenant_id, property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_stock_insights_property_flags ON stock_insights(property_id, low_stock, stock_out);
CREATE INDEX IF NOT EXISTS idx_stock_insights_tenant_property_flags ON stock_insights(tenant_id, property_id, low_stock, stock_out);
CREATE INDEX IF NOT EXISTS idx_menu_item_stock_impact_property_date ON menu_item_stock_impact(property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_menu_item_stock_impact_tenant_property_date ON menu_item_stock_impact(tenant_id, property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
