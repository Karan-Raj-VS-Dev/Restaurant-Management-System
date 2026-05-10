\set ON_ERROR_STOP on

SELECT 'CREATE DATABASE "restaurantManagementSystem"'
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = 'restaurantManagementSystem'
) \gexec
