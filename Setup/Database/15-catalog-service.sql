CREATE TABLE IF NOT EXISTS menu_categories (
    category_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    category_name VARCHAR(150) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, category_name)
);

CREATE TABLE IF NOT EXISTS menu_items (
    menu_item_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    category_id VARCHAR(64) REFERENCES menu_categories(category_id) ON DELETE SET NULL,
    item_code VARCHAR(50) NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    is_vegetarian BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    prep_time_minutes INTEGER NOT NULL DEFAULT 0 CHECK (prep_time_minutes >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, item_code)
);

CREATE TABLE IF NOT EXISTS ingredients (
    ingredient_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    ingredient_code VARCHAR(50) NOT NULL,
    ingredient_name VARCHAR(150) NOT NULL,
    unit_of_measure VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (property_id, ingredient_code)
);

ALTER TABLE menu_categories ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE menu_items ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE ingredients ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE menu_categories
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE menu_items
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE ingredients
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

ALTER TABLE menu_items DROP COLUMN IF EXISTS tax_id;

CREATE TABLE IF NOT EXISTS recipe_ingredients (
    recipe_ingredient_id VARCHAR(64) PRIMARY KEY,
    menu_item_id VARCHAR(64) NOT NULL REFERENCES menu_items(menu_item_id) ON DELETE CASCADE,
    ingredient_id VARCHAR(64) NOT NULL REFERENCES ingredients(ingredient_id) ON DELETE CASCADE,
    quantity_required NUMERIC(14, 3) NOT NULL CHECK (quantity_required > 0),
    wastage_factor NUMERIC(6, 3) NOT NULL DEFAULT 0 CHECK (wastage_factor >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (menu_item_id, ingredient_id)
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

CREATE INDEX IF NOT EXISTS idx_menu_categories_property_active ON menu_categories(property_id, is_active);
CREATE INDEX IF NOT EXISTS idx_menu_categories_tenant_property_active ON menu_categories(tenant_id, property_id, is_active);
CREATE INDEX IF NOT EXISTS idx_menu_items_property_active ON menu_items(property_id, is_active);
CREATE INDEX IF NOT EXISTS idx_menu_items_tenant_property_active ON menu_items(tenant_id, property_id, is_active);
CREATE INDEX IF NOT EXISTS idx_menu_items_category ON menu_items(category_id);
CREATE INDEX IF NOT EXISTS idx_ingredients_property_active ON ingredients(property_id, is_active);
CREATE INDEX IF NOT EXISTS idx_ingredients_tenant_property_active ON ingredients(tenant_id, property_id, is_active);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_menu_item ON recipe_ingredients(menu_item_id);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_ingredient ON recipe_ingredients(ingredient_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);

INSERT INTO ingredients (
    ingredient_id, tenant_id, property_id, ingredient_code, ingredient_name, unit_of_measure, is_active
)
SELECT 'ing-001', 'bikini-bottom', 'krusty-krab', 'ing-001', 'Pizza Dough', 'bases', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE ingredient_id = 'ing-001');

INSERT INTO ingredients (
    ingredient_id, tenant_id, property_id, ingredient_code, ingredient_name, unit_of_measure, is_active
)
SELECT 'ing-002', 'bikini-bottom', 'krusty-krab', 'ing-002', 'Mozzarella', 'grams', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE ingredient_id = 'ing-002');

INSERT INTO ingredients (
    ingredient_id, tenant_id, property_id, ingredient_code, ingredient_name, unit_of_measure, is_active
)
SELECT 'ing-003', 'bikini-bottom', 'krusty-krab', 'ing-003', 'Tomato Sauce', 'grams', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE ingredient_id = 'ing-003');

INSERT INTO ingredients (
    ingredient_id, tenant_id, property_id, ingredient_code, ingredient_name, unit_of_measure, is_active
)
SELECT 'ing-010', 'bikini-bottom', 'krusty-krab', 'ing-010', 'Pasta', 'grams', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE ingredient_id = 'ing-010');

INSERT INTO ingredients (
    ingredient_id, tenant_id, property_id, ingredient_code, ingredient_name, unit_of_measure, is_active
)
SELECT 'ing-011', 'bikini-bottom', 'krusty-krab', 'ing-011', 'Cream', 'ml', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE ingredient_id = 'ing-011');

INSERT INTO ingredients (
    ingredient_id, tenant_id, property_id, ingredient_code, ingredient_name, unit_of_measure, is_active
)
SELECT 'ing-012', 'bikini-bottom', 'krusty-krab', 'ing-012', 'Parmesan', 'grams', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE ingredient_id = 'ing-012');

INSERT INTO menu_items (
    menu_item_id, tenant_id, property_id, item_code, item_name, description, price,
    is_vegetarian, is_active, prep_time_minutes
)
SELECT 'item-001', 'bikini-bottom', 'krusty-krab', 'PIZZA-MARGHERITA', 'Margherita Pizza',
       'Classic cheese pizza with basil and house tomato sauce.', 299.00, TRUE, TRUE, 18
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE menu_item_id = 'item-001');

INSERT INTO menu_items (
    menu_item_id, tenant_id, property_id, item_code, item_name, description, price,
    is_vegetarian, is_active, prep_time_minutes
)
SELECT 'item-002', 'bikini-bottom', 'krusty-krab', 'PASTA-ALFREDO', 'Pasta Alfredo',
       'Creamy pasta finished with parmesan and pepper.', 249.00, FALSE, TRUE, 14
WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE menu_item_id = 'item-002');

INSERT INTO recipe_ingredients (
    recipe_ingredient_id, menu_item_id, ingredient_id, quantity_required, wastage_factor
)
SELECT 'recipe-001-a', 'item-001', 'ing-001', 1.000, 0
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients WHERE recipe_ingredient_id = 'recipe-001-a');

INSERT INTO recipe_ingredients (
    recipe_ingredient_id, menu_item_id, ingredient_id, quantity_required, wastage_factor
)
SELECT 'recipe-001-b', 'item-001', 'ing-002', 120.000, 0
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients WHERE recipe_ingredient_id = 'recipe-001-b');

INSERT INTO recipe_ingredients (
    recipe_ingredient_id, menu_item_id, ingredient_id, quantity_required, wastage_factor
)
SELECT 'recipe-001-c', 'item-001', 'ing-003', 80.000, 0
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients WHERE recipe_ingredient_id = 'recipe-001-c');

INSERT INTO recipe_ingredients (
    recipe_ingredient_id, menu_item_id, ingredient_id, quantity_required, wastage_factor
)
SELECT 'recipe-002-a', 'item-002', 'ing-010', 100.000, 0
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients WHERE recipe_ingredient_id = 'recipe-002-a');

INSERT INTO recipe_ingredients (
    recipe_ingredient_id, menu_item_id, ingredient_id, quantity_required, wastage_factor
)
SELECT 'recipe-002-b', 'item-002', 'ing-011', 80.000, 0
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients WHERE recipe_ingredient_id = 'recipe-002-b');

INSERT INTO recipe_ingredients (
    recipe_ingredient_id, menu_item_id, ingredient_id, quantity_required, wastage_factor
)
SELECT 'recipe-002-c', 'item-002', 'ing-012', 25.000, 0
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredients WHERE recipe_ingredient_id = 'recipe-002-c');
