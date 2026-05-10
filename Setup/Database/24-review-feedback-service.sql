CREATE TABLE IF NOT EXISTS review_requests (
    review_request_id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    bill_id VARCHAR(64),
    payment_id VARCHAR(64),
    customer_id VARCHAR(64),
    customer_contact VARCHAR(255),
    request_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id VARCHAR(64) PRIMARY KEY,
    review_request_id VARCHAR(64) REFERENCES review_requests(review_request_id) ON DELETE SET NULL,
    tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom',
    property_id VARCHAR(64) NOT NULL,
    customer_id VARCHAR(64),
    overall_rating SMALLINT NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    food_rating SMALLINT CHECK (food_rating BETWEEN 1 AND 5),
    service_rating SMALLINT CHECK (service_rating BETWEEN 1 AND 5),
    ambiance_rating SMALLINT CHECK (ambiance_rating BETWEEN 1 AND 5),
    comments TEXT,
    review_status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE review_requests ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64) NOT NULL DEFAULT 'bikini-bottom';

UPDATE review_requests
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

UPDATE reviews
SET tenant_id = 'bikini-bottom'
WHERE tenant_id IS NULL;

CREATE TABLE IF NOT EXISTS review_targets (
    review_target_id VARCHAR(64) PRIMARY KEY,
    review_id VARCHAR(64) NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    target_type VARCHAR(32) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    rating SMALLINT CHECK (rating BETWEEN 1 AND 5),
    remarks TEXT
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

CREATE INDEX IF NOT EXISTS idx_review_requests_property_status ON review_requests(property_id, request_status);
CREATE INDEX IF NOT EXISTS idx_review_requests_tenant_property_status ON review_requests(tenant_id, property_id, request_status);
CREATE INDEX IF NOT EXISTS idx_reviews_property_rating ON reviews(property_id, overall_rating);
CREATE INDEX IF NOT EXISTS idx_reviews_tenant_property_rating ON reviews(tenant_id, property_id, overall_rating);
CREATE INDEX IF NOT EXISTS idx_review_targets_review ON review_targets(review_id);
CREATE INDEX IF NOT EXISTS idx_outbox_events_status_time ON outbox_events(status, occurred_at);
CREATE INDEX IF NOT EXISTS idx_inbox_events_status_time ON inbox_events(status, received_at);
