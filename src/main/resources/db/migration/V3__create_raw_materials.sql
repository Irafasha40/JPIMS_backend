CREATE TABLE IF NOT EXISTS suppliers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    phone VARCHAR(100),
    email VARCHAR(255),
    address TEXT,
    payment_terms VARCHAR(255),
    lead_time_days INTEGER,
    rating NUMERIC(3,2),
    is_active BOOLEAN DEFAULT TRUE,
    onboarding_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

ALTER TABLE raw_materials
    ADD COLUMN IF NOT EXISTS supplier_id UUID REFERENCES suppliers(id),
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS supplier_communications (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    logged_by UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    notes TEXT,
    follow_up_date DATE,
    communication_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS supplier_documents (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    uploaded_by UUID NOT NULL REFERENCES users(id),
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    expiry_date DATE,
    uploaded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id UUID PRIMARY KEY,
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    po_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    expected_delivery_date DATE,
    actual_delivery_date DATE,
    notes TEXT,
    total_cost NUMERIC(19,4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id UUID PRIMARY KEY,
    purchase_order_id UUID NOT NULL REFERENCES purchase_orders(id),
    raw_material_id UUID NOT NULL REFERENCES raw_materials(id),
    quantity NUMERIC(19,4) NOT NULL,
    unit_cost NUMERIC(19,4) NOT NULL,
    received_quantity NUMERIC(19,4) DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id UUID PRIMARY KEY,
    raw_material_id UUID NOT NULL REFERENCES raw_materials(id),
    recorded_by UUID NOT NULL REFERENCES users(id),
    production_batch_id UUID,
    purchase_order_id UUID REFERENCES purchase_orders(id),
    type VARCHAR(50) NOT NULL,
    quantity NUMERIC(19,4) NOT NULL,
    reference_number VARCHAR(100),
    date TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);
