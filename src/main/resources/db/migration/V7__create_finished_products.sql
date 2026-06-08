CREATE TABLE IF NOT EXISTS finished_products (
    id UUID PRIMARY KEY,
    batch_id UUID REFERENCES production_batches(id),
    product_name VARCHAR(255),
    flavor VARCHAR(100),
    packaging_size VARCHAR(100),
    lot_number VARCHAR(100),
    quantity NUMERIC(19,4) NOT NULL,
    expiry_date DATE,
    storage_location VARCHAR(255),
    status VARCHAR(50),
    unit_cost NUMERIC(19,4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS finished_product_movements (
    id UUID PRIMARY KEY,
    finished_product_id UUID NOT NULL REFERENCES finished_products(id),
    recorded_by UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL,
    date TIMESTAMPTZ,
    notes TEXT,
    reference_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);
