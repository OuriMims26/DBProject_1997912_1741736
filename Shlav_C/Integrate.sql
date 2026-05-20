-- ==========================================
-- INTEGRATION SCRIPT: LOGISTIC DB -> SUPABASE
-- ==========================================

-- 1. CLEAN SLATE: Remove any previous connections or messy schemas
DROP SERVER IF EXISTS supabase_server CASCADE;
DROP SCHEMA IF EXISTS remote_shoppy CASCADE;

-- 2. SETUP: Enable the foreign data wrapper extension
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- 3. CONNECTION: Link to the Supabase cloud database
CREATE SERVER supabase_server
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (
        host 'aws-1-eu-west-1.pooler.supabase.com',
        port '5432',
        dbname 'postgres'
);

-- 4. AUTHENTICATION: Map your local 'postgres' user to the Supabase credentials
CREATE USER MAPPING FOR postgres
SERVER supabase_server
OPTIONS (
        user 'postgres.jcqlmkuusqdxplazmfgz',
        password 'DBProject_1935279_214631426'
);

-- 5. ORGANIZATION: Create a dedicated schema (folder) for the Shoppy tables
CREATE SCHEMA remote_shoppy;

-- 6. IMPORT: Pull the tables from Supabase's 'public' schema into the dedicated schema
IMPORT FOREIGN SCHEMA public
FROM SERVER supabase_server
INTO remote_shoppy;

-- TEST: Verify the connection works
-- SELECT * FROM remote_shoppy.categories LIMIT 5;
