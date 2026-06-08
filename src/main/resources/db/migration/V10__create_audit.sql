CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    record_id VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(100),
    is_anomaly BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS data_retention_policies (
    id UUID PRIMARY KEY,
    module VARCHAR(100) NOT NULL,
    retention_days INTEGER NOT NULL,
    archive_enabled BOOLEAN DEFAULT FALSE,
    last_applied_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS access_requests (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL REFERENCES users(id),
    requested_role VARCHAR(50) NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL,
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS permission_matrix (
    id UUID PRIMARY KEY,
    role VARCHAR(50) NOT NULL,
    module VARCHAR(100) NOT NULL,
    can_view BOOLEAN DEFAULT FALSE,
    can_create BOOLEAN DEFAULT FALSE,
    can_edit BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    can_export BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);
