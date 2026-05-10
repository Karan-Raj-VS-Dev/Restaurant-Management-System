CREATE TABLE IF NOT EXISTS tenants (
    tenant_id VARCHAR(64) PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    company_slug VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS properties (
    property_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_code VARCHAR(50) NOT NULL UNIQUE,
    property_name VARCHAR(150) NOT NULL,
    address_line TEXT,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    timezone VARCHAR(100) NOT NULL DEFAULT 'Asia/Kolkata',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE properties ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE properties ADD COLUMN IF NOT EXISTS address_line TEXT;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE properties DROP COLUMN IF EXISTS total_tables;

INSERT INTO tenants (
    tenant_id,
    company_name,
    company_slug,
    status
)
SELECT
    'bikini-bottom',
    'Bikini Bottom',
    'bikini-bottom',
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM tenants
    WHERE tenant_id = 'bikini-bottom'
);

UPDATE properties
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

INSERT INTO properties (
    property_id,
    tenant_id,
    property_code,
    property_name,
    address_line,
    city,
    state,
    country,
    timezone,
    latitude,
    longitude,
    status
)
SELECT
    'krusty-krab',
    'bikini-bottom',
    'BB-KRUSTY-001',
    'Krusty Krab',
    'Sea Floor, Bikini Bottom',
    'Bikini Bottom',
    'Pacific Ocean',
    'Ocean',
    'Pacific/Honolulu',
    8.000000,
    -160.000000,
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM properties
    WHERE property_id = 'krusty-krab'
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_properties_tenant'
    ) THEN
        ALTER TABLE properties
            ADD CONSTRAINT fk_properties_tenant
            FOREIGN KEY (tenant_id)
            REFERENCES tenants (tenant_id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS property_contacts (
    contact_id VARCHAR(64) PRIMARY KEY,
    property_id VARCHAR(64) NOT NULL REFERENCES properties(property_id) ON DELETE CASCADE,
    contact_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(32),
    email VARCHAR(255),
    role_name VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS property_area_sections (
    area_section_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    property_id VARCHAR(64) NOT NULL REFERENCES properties(property_id) ON DELETE CASCADE,
    floor_name VARCHAR(120) NOT NULL,
    section_name VARCHAR(120) NOT NULL,
    max_table_count INTEGER NOT NULL DEFAULT 0 CHECK (max_table_count >= 0),
    waiter_names TEXT NOT NULL DEFAULT '',
    cleaner_names TEXT NOT NULL DEFAULT '',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, property_id, floor_name, section_name)
);

CREATE TABLE IF NOT EXISTS business_hours (
    schedule_id VARCHAR(64) PRIMARY KEY,
    property_id VARCHAR(64) NOT NULL REFERENCES properties(property_id) ON DELETE CASCADE,
    weekday SMALLINT NOT NULL CHECK (weekday BETWEEN 0 AND 6),
    opens_at TIME NOT NULL,
    closes_at TIME NOT NULL,
    UNIQUE (property_id, weekday)
);

INSERT INTO property_area_sections (
    area_section_id,
    tenant_id,
    property_id,
    floor_name,
    section_name,
    max_table_count,
    waiter_names,
    cleaner_names,
    status
)
SELECT
    'main-floor__dining',
    'bikini-bottom',
    'krusty-krab',
    'Main floor',
    'Dining',
    12,
    'Neha,Anu',
    'Suresh',
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM property_area_sections
    WHERE area_section_id = 'main-floor__dining'
);

INSERT INTO property_area_sections (
    area_section_id,
    tenant_id,
    property_id,
    floor_name,
    section_name,
    max_table_count,
    waiter_names,
    cleaner_names,
    status
)
SELECT
    'main-floor__patio',
    'bikini-bottom',
    'krusty-krab',
    'Main floor',
    'Patio',
    6,
    'Karthi',
    'Suresh',
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM property_area_sections
    WHERE area_section_id = 'main-floor__patio'
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

CREATE INDEX IF NOT EXISTS idx_tenants_slug_status ON tenants(company_slug, status);
CREATE INDEX IF NOT EXISTS idx_properties_tenant_status_city ON properties(tenant_id, status, city);
CREATE INDEX IF NOT EXISTS idx_property_contacts_property ON property_contacts(property_id);
CREATE INDEX IF NOT EXISTS idx_property_area_sections_scope ON property_area_sections(tenant_id, property_id, status);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
