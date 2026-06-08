CREATE TABLE IF NOT EXISTS quality_thresholds (
    id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    ph_min NUMERIC(10,4) NOT NULL,
    ph_max NUMERIC(10,4) NOT NULL,
    brix_min NUMERIC(10,4) NOT NULL,
    brix_max NUMERIC(10,4) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

ALTER TABLE quality_tests
    ADD COLUMN IF NOT EXISTS certificate_number VARCHAR(100);
