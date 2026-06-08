ALTER TABLE finished_products
    ADD COLUMN IF NOT EXISTS volume_liters NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS bottles_used INTEGER,
    ADD COLUMN IF NOT EXISTS boxes_used INTEGER;

CREATE UNIQUE INDEX IF NOT EXISTS uq_finished_products_batch_id
    ON finished_products (batch_id)
    WHERE batch_id IS NOT NULL;
