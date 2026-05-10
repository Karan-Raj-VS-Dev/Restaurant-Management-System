CREATE TABLE IF NOT EXISTS roles (
    role_code VARCHAR(50) PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS permissions (
    permission_code VARCHAR(100) PRIMARY KEY,
    permission_name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS app_users (
    user_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password TEXT,
    full_name VARCHAR(150) NOT NULL,
    first_name VARCHAR(75),
    last_name VARCHAR(75),
    phone_country_code VARCHAR(8),
    phone_number VARCHAR(32),
    phone_e164 VARCHAR(32),
    address_line TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    admin_user BOOLEAN NOT NULL DEFAULT FALSE,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE app_users ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS property_id VARCHAR(64);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS password TEXT;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS first_name VARCHAR(75);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS last_name VARCHAR(75);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS phone_country_code VARCHAR(8);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS phone_number VARCHAR(32);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS phone_e164 VARCHAR(32);
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS address_line TEXT;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS admin_user BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE app_users
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'app_users'
          AND column_name = 'password_hash'
    ) THEN
        UPDATE app_users
        SET password = COALESCE(password, password_hash)
        WHERE password IS NULL;

        ALTER TABLE app_users DROP COLUMN password_hash;
    END IF;
END $$;

ALTER TABLE app_users ALTER COLUMN password DROP NOT NULL;
ALTER TABLE app_users DROP COLUMN IF EXISTS keycloak_user_id;

INSERT INTO app_users (
    user_id,
    tenant_id,
    property_id,
    username,
    email,
    password,
    full_name,
    first_name,
    last_name,
    phone_country_code,
    phone_number,
    phone_e164,
    address_line,
    status,
    admin_user,
    must_change_password
)
SELECT
    'usr-kingchef-admin',
    'bikini-bottom',
    'krusty-krab',
    'kingChef',
    'kingchef@restaurant.local',
    'SUPER@secret45',
    'King Chef',
    'King',
    'Chef',
    '+91',
    '9876543210',
    '+919876543210',
    'Restaurant HQ',
    'ACTIVE',
    TRUE,
    FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM app_users
    WHERE LOWER(username) = LOWER('kingChef')
);

UPDATE app_users
SET tenant_id = 'bikini-bottom',
    property_id = 'krusty-krab',
    password = 'SUPER@secret45',
    admin_user = TRUE,
    must_change_password = FALSE,
    status = 'ACTIVE'
WHERE LOWER(username) = LOWER('kingChef');

CREATE TABLE IF NOT EXISTS user_property_access (
    mapping_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    tenant_id VARCHAR(64) NOT NULL,
    property_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, property_id)
);

DELETE FROM user_property_access
WHERE tenant_id <> 'bikini-bottom'
   OR property_id <> 'krusty-krab'
   OR user_id = 'usr-kingchef-admin';

INSERT INTO user_property_access (
    mapping_id,
    user_id,
    tenant_id,
    property_id
)
SELECT
    'upa-kingchef-krusty',
    user_id,
    'bikini-bottom',
    'krusty-krab'
FROM app_users
WHERE LOWER(username) = LOWER('kingChef')
  AND NOT EXISTS (
      SELECT 1
      FROM user_property_access
      WHERE user_id = app_users.user_id
        AND property_id = 'krusty-krab'
  );

CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(64) NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    role_code VARCHAR(50) NOT NULL REFERENCES roles(role_code) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_code)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_code VARCHAR(50) NOT NULL REFERENCES roles(role_code) ON DELETE CASCADE,
    permission_code VARCHAR(100) NOT NULL REFERENCES permissions(permission_code) ON DELETE CASCADE,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (role_code, permission_code)
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

CREATE TABLE IF NOT EXISTS password_reset_otps (
    otp_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    identifier VARCHAR(255) NOT NULL,
    delivery_channel VARCHAR(16) NOT NULL,
    otp_code VARCHAR(12) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_app_users_status ON app_users(status);
CREATE INDEX IF NOT EXISTS idx_app_users_tenant_property ON app_users(tenant_id, property_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_app_users_phone_e164 ON app_users(phone_e164) WHERE phone_e164 IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_user_property_access_scope ON user_property_access(tenant_id, property_id, user_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate ON outbox_events(aggregate_type, aggregate_id);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_otps_lookup ON password_reset_otps(user_id, otp_code, used_at, expires_at);
