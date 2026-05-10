CREATE TABLE IF NOT EXISTS stock_items (
    stock_item_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    ingredient_id VARCHAR(64) NOT NULL,
    ingredient_name VARCHAR(150) NOT NULL,
    unit_of_measure VARCHAR(32) NOT NULL,
    reorder_threshold NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (reorder_threshold >= 0),
    maximum_capacity NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (maximum_capacity >= 0),
    market_unit_price NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (market_unit_price >= 0),
    current_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (current_quantity >= 0),
    reserved_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    available_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (available_quantity >= 0),
    last_stock_update_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, ingredient_id)
);

CREATE TABLE IF NOT EXISTS stock_batches (
    batch_id VARCHAR(64) PRIMARY KEY,
    stock_item_id VARCHAR(64) NOT NULL REFERENCES stock_items(stock_item_id) ON DELETE CASCADE,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    batch_code VARCHAR(64),
    received_quantity NUMERIC(14, 3) NOT NULL CHECK (received_quantity >= 0),
    remaining_quantity NUMERIC(14, 3) NOT NULL CHECK (remaining_quantity >= 0),
    unit_cost NUMERIC(12, 2) CHECK (unit_cost >= 0),
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS operational_supplies (
    supply_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    supply_code VARCHAR(50) NOT NULL,
    supply_name VARCHAR(150) NOT NULL,
    unit_of_measure VARCHAR(32) NOT NULL,
    reorder_level NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (reorder_level >= 0),
    market_unit_price NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (market_unit_price >= 0),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, supply_code)
);

CREATE TABLE IF NOT EXISTS stock_movements (
    movement_id VARCHAR(64) PRIMARY KEY,
    stock_item_id VARCHAR(64) NOT NULL REFERENCES stock_items(stock_item_id) ON DELETE CASCADE,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    reference_type VARCHAR(64) NOT NULL,
    reference_id VARCHAR(64),
    movement_type VARCHAR(32) NOT NULL,
    quantity NUMERIC(14, 3) NOT NULL CHECK (quantity > 0),
    notes TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS stock_reservations (
    reservation_id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    reservation_status VARCHAR(32) NOT NULL DEFAULT 'RESERVED',
    reserved_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ,
    UNIQUE (order_id)
);

CREATE TABLE IF NOT EXISTS stock_reservation_items (
    reservation_item_id VARCHAR(64) PRIMARY KEY,
    reservation_id VARCHAR(64) NOT NULL REFERENCES stock_reservations(reservation_id) ON DELETE CASCADE,
    stock_item_id VARCHAR(64) NOT NULL REFERENCES stock_items(stock_item_id) ON DELETE CASCADE,
    menu_item_id VARCHAR(64),
    quantity_reserved NUMERIC(14, 3) NOT NULL CHECK (quantity_reserved > 0),
    quantity_consumed NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (quantity_consumed >= 0)
);

CREATE TABLE IF NOT EXISTS menu_item_availability (
    availability_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    menu_item_id VARCHAR(64) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    unavailable_reason TEXT,
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, menu_item_id)
);

ALTER TABLE stock_items ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE stock_items ADD COLUMN IF NOT EXISTS market_unit_price NUMERIC(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE stock_items ADD COLUMN IF NOT EXISTS reorder_threshold NUMERIC(14, 3) NOT NULL DEFAULT 0;
ALTER TABLE stock_items ADD COLUMN IF NOT EXISTS maximum_capacity NUMERIC(14, 3) NOT NULL DEFAULT 0;
ALTER TABLE stock_batches ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE stock_movements ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE stock_reservations ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE menu_item_availability ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'stock_items'
          AND column_name = 'reorder_level'
    ) THEN
        EXECUTE '
            UPDATE stock_items
            SET reorder_threshold = reorder_level
            WHERE reorder_threshold = 0
              AND reorder_level IS NOT NULL
        ';
    END IF;
END $$;

UPDATE stock_items
SET maximum_capacity = current_quantity
WHERE maximum_capacity = 0
  AND current_quantity IS NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'stock_items'
          AND column_name = 'reorder_level'
    ) THEN
        ALTER TABLE stock_items DROP COLUMN IF EXISTS reorder_level;
    END IF;
END $$;

UPDATE stock_items
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE stock_batches
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE stock_movements
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE stock_reservations
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE menu_item_availability
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

INSERT INTO stock_items (
    stock_item_id, tenant_id, property_id, ingredient_id, ingredient_name, unit_of_measure,
    reorder_threshold, maximum_capacity, market_unit_price, current_quantity, reserved_quantity,
    available_quantity, last_stock_update_at
)
SELECT 'stock-ing-001', 'bikini-bottom', 'krusty-krab', 'ing-001', 'Pizza Dough', 'bases',
       5, 40, 45.00, 25, 0, 25, NOW()
WHERE NOT EXISTS (SELECT 1 FROM stock_items WHERE tenant_id = 'bikini-bottom' AND property_id = 'krusty-krab' AND ingredient_id = 'ing-001');

INSERT INTO stock_items (
    stock_item_id, tenant_id, property_id, ingredient_id, ingredient_name, unit_of_measure,
    reorder_threshold, maximum_capacity, market_unit_price, current_quantity, reserved_quantity,
    available_quantity, last_stock_update_at
)
SELECT 'stock-ing-002', 'bikini-bottom', 'krusty-krab', 'ing-002', 'Mozzarella', 'grams',
       300, 2200, 0.60, 1800, 0, 1800, NOW()
WHERE NOT EXISTS (SELECT 1 FROM stock_items WHERE tenant_id = 'bikini-bottom' AND property_id = 'krusty-krab' AND ingredient_id = 'ing-002');

INSERT INTO stock_items (
    stock_item_id, tenant_id, property_id, ingredient_id, ingredient_name, unit_of_measure,
    reorder_threshold, maximum_capacity, market_unit_price, current_quantity, reserved_quantity,
    available_quantity, last_stock_update_at
)
SELECT 'stock-ing-003', 'bikini-bottom', 'krusty-krab', 'ing-003', 'Tomato Sauce', 'grams',
       150, 1200, 0.18, 600, 0, 600, NOW()
WHERE NOT EXISTS (SELECT 1 FROM stock_items WHERE tenant_id = 'bikini-bottom' AND property_id = 'krusty-krab' AND ingredient_id = 'ing-003');

INSERT INTO stock_items (
    stock_item_id, tenant_id, property_id, ingredient_id, ingredient_name, unit_of_measure,
    reorder_threshold, maximum_capacity, market_unit_price, current_quantity, reserved_quantity,
    available_quantity, last_stock_update_at
)
SELECT 'stock-ing-010', 'bikini-bottom', 'krusty-krab', 'ing-010', 'Pasta', 'grams',
       200, 1600, 0.14, 1000, 0, 1000, NOW()
WHERE NOT EXISTS (SELECT 1 FROM stock_items WHERE tenant_id = 'bikini-bottom' AND property_id = 'krusty-krab' AND ingredient_id = 'ing-010');

INSERT INTO stock_items (
    stock_item_id, tenant_id, property_id, ingredient_id, ingredient_name, unit_of_measure,
    reorder_threshold, maximum_capacity, market_unit_price, current_quantity, reserved_quantity,
    available_quantity, last_stock_update_at
)
SELECT 'stock-ing-011', 'bikini-bottom', 'krusty-krab', 'ing-011', 'Cream', 'ml',
       200, 1200, 0.22, 900, 0, 900, NOW()
WHERE NOT EXISTS (SELECT 1 FROM stock_items WHERE tenant_id = 'bikini-bottom' AND property_id = 'krusty-krab' AND ingredient_id = 'ing-011');

INSERT INTO stock_items (
    stock_item_id, tenant_id, property_id, ingredient_id, ingredient_name, unit_of_measure,
    reorder_threshold, maximum_capacity, market_unit_price, current_quantity, reserved_quantity,
    available_quantity, last_stock_update_at
)
SELECT 'stock-ing-012', 'bikini-bottom', 'krusty-krab', 'ing-012', 'Parmesan', 'grams',
       80, 260, 1.10, 100, 0, 100, NOW()
WHERE NOT EXISTS (SELECT 1 FROM stock_items WHERE tenant_id = 'bikini-bottom' AND property_id = 'krusty-krab' AND ingredient_id = 'ing-012');

INSERT INTO operational_supplies (
    supply_id, tenant_id, property_id, supply_code, supply_name, unit_of_measure, reorder_level, market_unit_price, status
)
SELECT 'sup-001', 'bikini-bottom', 'krusty-krab', 'BROOM-01', 'Cleaning Broom', 'piece', 2, 350, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM operational_supplies WHERE supply_id = 'sup-001');

INSERT INTO operational_supplies (
    supply_id, tenant_id, property_id, supply_code, supply_name, unit_of_measure, reorder_level, market_unit_price, status
)
SELECT 'sup-002', 'bikini-bottom', 'krusty-krab', 'SANITIZER-01', 'Floor Sanitizer', 'bottle', 4, 180, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM operational_supplies WHERE supply_id = 'sup-002');

INSERT INTO operational_supplies (
    supply_id, tenant_id, property_id, supply_code, supply_name, unit_of_measure, reorder_level, market_unit_price, status
)
SELECT 'sup-003', 'bikini-bottom', 'krusty-krab', 'NAPKIN-01', 'Paper Napkins', 'pack', 6, 95, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM operational_supplies WHERE supply_id = 'sup-003');

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

CREATE INDEX IF NOT EXISTS idx_stock_items_property_ingredient ON stock_items(property_id, ingredient_id);
CREATE INDEX IF NOT EXISTS idx_stock_items_tenant_property_ingredient ON stock_items(tenant_id, property_id, ingredient_id);
CREATE INDEX IF NOT EXISTS idx_stock_items_property_availability ON stock_items(property_id, available_quantity);
CREATE INDEX IF NOT EXISTS idx_stock_items_tenant_property_availability ON stock_items(tenant_id, property_id, available_quantity);
CREATE INDEX IF NOT EXISTS idx_stock_batches_stock_item_expiry ON stock_batches(stock_item_id, expires_at);
CREATE INDEX IF NOT EXISTS idx_operational_supplies_tenant_property_status ON operational_supplies(tenant_id, property_id, status);
CREATE INDEX IF NOT EXISTS idx_stock_movements_stock_item_time ON stock_movements(stock_item_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_stock_reservations_property_status ON stock_reservations(property_id, reservation_status);
CREATE INDEX IF NOT EXISTS idx_stock_reservations_tenant_property_status ON stock_reservations(tenant_id, property_id, reservation_status);
CREATE INDEX IF NOT EXISTS idx_stock_reservation_items_reservation ON stock_reservation_items(reservation_id);
CREATE INDEX IF NOT EXISTS idx_menu_item_availability_property_item ON menu_item_availability(property_id, menu_item_id);
CREATE INDEX IF NOT EXISTS idx_menu_item_availability_tenant_property_item ON menu_item_availability(tenant_id, property_id, menu_item_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
