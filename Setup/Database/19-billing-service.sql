CREATE TABLE IF NOT EXISTS bills (
    bill_id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL UNIQUE,
    linked_order_ids TEXT NOT NULL DEFAULT '',
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    table_id VARCHAR(64),
    customer_id VARCHAR(64),
    billing_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    settlement_type VARCHAR(32) NOT NULL DEFAULT 'STANDARD',
    cancellation_reason TEXT,
    cancellation_fee_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (cancellation_fee_amount >= 0),
    subtotal_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (subtotal_amount >= 0),
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    service_charge_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (service_charge_amount >= 0),
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ
);

ALTER TABLE bills ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE bills ADD COLUMN IF NOT EXISTS linked_order_ids TEXT NOT NULL DEFAULT '';
ALTER TABLE bills ADD COLUMN IF NOT EXISTS settlement_type VARCHAR(32) NOT NULL DEFAULT 'STANDARD';
ALTER TABLE bills ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;
ALTER TABLE bills ADD COLUMN IF NOT EXISTS cancellation_fee_amount NUMERIC(12, 2) NOT NULL DEFAULT 0;

UPDATE bills
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

CREATE TABLE IF NOT EXISTS bill_items (
    bill_item_id VARCHAR(64) PRIMARY KEY,
    bill_id VARCHAR(64) NOT NULL REFERENCES bills(bill_id) ON DELETE CASCADE,
    order_item_id VARCHAR(64),
    menu_item_id VARCHAR(64),
    item_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    line_total NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0)
);

CREATE TABLE IF NOT EXISTS bill_adjustments (
    adjustment_id VARCHAR(64) PRIMARY KEY,
    bill_id VARCHAR(64) NOT NULL REFERENCES bills(bill_id) ON DELETE CASCADE,
    adjustment_type VARCHAR(32) NOT NULL,
    description TEXT,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS tax_definitions (
    tax_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    tax_name VARCHAR(150) NOT NULL,
    rate_percent NUMERIC(5, 2) NOT NULL CHECK (rate_percent >= 0),
    applies_to VARCHAR(64) NOT NULL DEFAULT 'MENU_ITEM',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS billing_templates (
    template_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    template_name VARCHAR(150) NOT NULL,
    description JSONB NOT NULL DEFAULT '{}'::jsonb,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'billing'
          AND table_name = 'billing_templates'
          AND column_name = 'description'
          AND udt_name <> 'jsonb'
    ) THEN
        ALTER TABLE billing.billing_templates
            ALTER COLUMN description
            TYPE JSONB
            USING CASE
                WHEN description IS NULL OR btrim(description) = '' THEN
                    jsonb_build_object(
                        'summary', 'Structured bill-template definition placeholder is ready.',
                        'sections', jsonb_build_array()
                    )
                ELSE
                    jsonb_build_object(
                        'summary', description,
                        'sections', jsonb_build_array()
                    )
            END;
    END IF;

    ALTER TABLE billing.billing_templates
        ALTER COLUMN description SET DEFAULT '{}'::jsonb;
END $$;

INSERT INTO tax_definitions (
    tax_id,
    tenant_id,
    property_id,
    tax_name,
    rate_percent,
    applies_to,
    status
)
SELECT
    'tax-gst-5',
    'bikini-bottom',
    'krusty-krab',
    'GST 5%',
    5.00,
    'MENU_ITEM',
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM tax_definitions
    WHERE tax_id = 'tax-gst-5'
);

INSERT INTO tax_definitions (
    tax_id,
    tenant_id,
    property_id,
    tax_name,
    rate_percent,
    applies_to,
    status
)
SELECT
    'tax-exempt',
    'bikini-bottom',
    'krusty-krab',
    'Tax Exempt',
    0.00,
    'MENU_ITEM',
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM tax_definitions
    WHERE tax_id = 'tax-exempt'
);

INSERT INTO billing_templates (
    template_id,
    tenant_id,
    property_id,
    template_name,
    description,
    status
)
SELECT
    'bill-template-standard',
    'bikini-bottom',
    'krusty-krab',
    'Standard dine-in receipt',
    jsonb_build_object(
        'summary', 'Default bill layout placeholder for dine-in guests.',
        'channel', 'DINE_IN',
        'headerText', 'Thank you for dining with us.',
        'footerText', 'Visit again soon.',
        'sections', jsonb_build_array('header', 'line_items', 'taxes', 'totals', 'footer')
    ),
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM billing_templates
    WHERE template_id = 'bill-template-standard'
);

INSERT INTO billing_templates (
    template_id,
    tenant_id,
    property_id,
    template_name,
    description,
    status
)
SELECT
    'bill-template-takeaway',
    'bikini-bottom',
    'krusty-krab',
    'Compact takeaway slip',
    jsonb_build_object(
        'summary', 'Future compact bill layout placeholder for takeaway and marketplace orders.',
        'channel', 'TAKEAWAY',
        'headerText', 'Packed fresh for takeaway.',
        'footerText', 'Order again anytime.',
        'sections', jsonb_build_array('header', 'line_items', 'totals', 'footer')
    ),
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM billing_templates
    WHERE template_id = 'bill-template-takeaway'
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'catalog'
          AND table_name = 'menu_items'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_catalog_menu_items_tax_id'
        ) THEN
            ALTER TABLE catalog.menu_items
                DROP CONSTRAINT fk_catalog_menu_items_tax_id;
        END IF;

        ALTER TABLE catalog.menu_items
            DROP COLUMN IF EXISTS tax_id,
            DROP COLUMN IF EXISTS tax_rate;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'ordering'
          AND table_name = 'order_items'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_ordering_order_items_tax_id'
        ) THEN
            ALTER TABLE ordering.order_items
                DROP CONSTRAINT fk_ordering_order_items_tax_id;
        END IF;

        ALTER TABLE ordering.order_items
            DROP COLUMN IF EXISTS tax_id,
            DROP COLUMN IF EXISTS tax_rate;
    END IF;
END $$;

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

CREATE INDEX IF NOT EXISTS idx_bills_property_status ON bills(property_id, billing_status);
CREATE INDEX IF NOT EXISTS idx_bills_tenant_property_status ON bills(tenant_id, property_id, billing_status);
CREATE INDEX IF NOT EXISTS idx_bills_customer_status ON bills(customer_id, billing_status);
CREATE INDEX IF NOT EXISTS idx_bill_items_bill ON bill_items(bill_id);
CREATE INDEX IF NOT EXISTS idx_bill_adjustments_bill ON bill_adjustments(bill_id);
CREATE INDEX IF NOT EXISTS idx_tax_definitions_scope_status ON tax_definitions(tenant_id, property_id, status);
CREATE INDEX IF NOT EXISTS idx_billing_templates_scope_status ON billing_templates(tenant_id, property_id, status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
