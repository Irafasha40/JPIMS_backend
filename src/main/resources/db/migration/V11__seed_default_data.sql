CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (
    id, full_name, email, password, phone, employee_id, department, role,
    is_active, is_locked, login_attempts, mfa_enabled, created_at, updated_at, created_by
)
SELECT
    '00000000-0000-0000-0000-000000000001'::uuid,
    'System Administrator',
    'admin@whizupp.com',
    '$2a$10$9QlnajLuMBh82nCKq6rgvuoCyH/ulMWcRMXqLhb/tCNJbuzZ3d.1S',
    NULL,
    'ADM-0001',
    'Administration',
    'ADMINISTRATOR',
    TRUE,
    FALSE,
    0,
    FALSE,
    NOW(),
    NOW(),
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@whizupp.com'
);

INSERT INTO quality_thresholds (
    id, product_name, ph_min, ph_max, brix_min, brix_max, is_default, created_at, updated_at, created_by
)
SELECT
    '00000000-0000-0000-0000-000000000002'::uuid,
    'DEFAULT',
    3.0,
    4.5,
    10.0,
    16.0,
    TRUE,
    NOW(),
    NOW(),
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM quality_thresholds WHERE is_default = TRUE
);

INSERT INTO permission_matrix (
    id, role, module, can_view, can_create, can_edit, can_delete, can_export, created_at, updated_at, created_by
)
SELECT
    gen_random_uuid(),
    roles.role,
    modules.module,
    TRUE,
    CASE WHEN roles.role = 'ADMINISTRATOR' THEN TRUE ELSE FALSE END,
    CASE WHEN roles.role = 'ADMINISTRATOR' THEN TRUE ELSE FALSE END,
    CASE WHEN roles.role = 'ADMINISTRATOR' THEN TRUE ELSE FALSE END,
    CASE WHEN roles.role IN ('ADMINISTRATOR', 'INVENTORY_MANAGER', 'PRODUCTION_MANAGER') THEN TRUE ELSE FALSE END,
    NOW(),
    NOW(),
    'SYSTEM'
FROM
    (VALUES
        ('ADMINISTRATOR'),
        ('PRODUCTION_MANAGER'),
        ('INVENTORY_MANAGER'),
        ('QC_OFFICER'),
        ('SALES_STAFF')
    ) AS roles(role)
CROSS JOIN
    (VALUES
        ('AUTHENTICATION'),
        ('USER_MANAGEMENT'),
        ('SUPPLIER_MANAGEMENT'),
        ('RAW_MATERIAL_INVENTORY'),
        ('RECIPE_FORMULATION'),
        ('PRODUCTION_BATCH_MANAGEMENT'),
        ('QUALITY_CONTROL'),
        ('FINISHED_PRODUCT_INVENTORY'),
        ('SALES_ORDER_MANAGEMENT'),
        ('CUSTOMER_MANAGEMENT'),
        ('NOTIFICATIONS_ALERTS'),
        ('REPORTING_ANALYTICS'),
        ('SECURITY_AUDIT')
    ) AS modules(module)
WHERE NOT EXISTS (
    SELECT 1 FROM permission_matrix
);
