\set ON_ERROR_STOP on

SELECT 'CREATE DATABASE rms_auth_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_auth_db') \gexec

SELECT 'CREATE DATABASE rms_customer_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_customer_db') \gexec

SELECT 'CREATE DATABASE rms_employee_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_employee_db') \gexec

SELECT 'CREATE DATABASE rms_property_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_property_db') \gexec

SELECT 'CREATE DATABASE rms_event_gateway_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_event_gateway_db') \gexec

SELECT 'CREATE DATABASE rms_table_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_table_db') \gexec

SELECT 'CREATE DATABASE rms_catalog_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_catalog_db') \gexec

SELECT 'CREATE DATABASE rms_inventory_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_inventory_db') \gexec

SELECT 'CREATE DATABASE rms_order_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_order_db') \gexec

SELECT 'CREATE DATABASE rms_kitchen_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_kitchen_db') \gexec

SELECT 'CREATE DATABASE rms_billing_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_billing_db') \gexec

SELECT 'CREATE DATABASE rms_payment_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_payment_db') \gexec

SELECT 'CREATE DATABASE rms_takeaway_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_takeaway_db') \gexec

SELECT 'CREATE DATABASE rms_marketplace_integration_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_marketplace_integration_db') \gexec

SELECT 'CREATE DATABASE rms_audit_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_audit_db') \gexec

SELECT 'CREATE DATABASE rms_review_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_review_db') \gexec

SELECT 'CREATE DATABASE rms_reporting_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_reporting_db') \gexec

SELECT 'CREATE DATABASE rms_operations_insights_db'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'rms_operations_insights_db') \gexec
