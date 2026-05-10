CREATE TABLE IF NOT EXISTS daily_property_summary (
    summary_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    total_orders INTEGER NOT NULL DEFAULT 0 CHECK (total_orders >= 0),
    dine_in_orders INTEGER NOT NULL DEFAULT 0 CHECK (dine_in_orders >= 0),
    takeaway_orders INTEGER NOT NULL DEFAULT 0 CHECK (takeaway_orders >= 0),
    total_revenue NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (total_revenue >= 0),
    total_tax NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (total_tax >= 0),
    total_discounts NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (total_discounts >= 0),
    average_ticket_size NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (average_ticket_size >= 0),
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, business_date)
);

CREATE TABLE IF NOT EXISTS item_sales_summary (
    item_sales_summary_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    menu_item_id VARCHAR(64) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity_sold NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (quantity_sold >= 0),
    gross_sales NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (gross_sales >= 0),
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, business_date, menu_item_id)
);

CREATE TABLE IF NOT EXISTS server_performance_summary (
    server_performance_summary_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    employee_id VARCHAR(64) NOT NULL,
    total_orders_served INTEGER NOT NULL DEFAULT 0 CHECK (total_orders_served >= 0),
    total_guests_served INTEGER NOT NULL DEFAULT 0 CHECK (total_guests_served >= 0),
    total_sales NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (total_sales >= 0),
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, business_date, employee_id)
);

ALTER TABLE daily_property_summary ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE item_sales_summary ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE server_performance_summary ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE daily_property_summary
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE item_sales_summary
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE server_performance_summary
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

CREATE INDEX IF NOT EXISTS idx_daily_property_summary_property_date ON daily_property_summary(property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_daily_property_summary_tenant_property_date ON daily_property_summary(tenant_id, property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_item_sales_summary_property_date ON item_sales_summary(property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_item_sales_summary_tenant_property_date ON item_sales_summary(tenant_id, property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_server_performance_property_date ON server_performance_summary(property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_server_performance_tenant_property_date ON server_performance_summary(tenant_id, property_id, business_date);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
