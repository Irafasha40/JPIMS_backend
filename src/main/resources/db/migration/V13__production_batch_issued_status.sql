-- Allow ISSUED status (ingredients confirmed, not yet in production)
ALTER TABLE production_batches DROP CONSTRAINT IF EXISTS production_batches_status_check;

ALTER TABLE production_batches
    ADD CONSTRAINT production_batches_status_check
    CHECK (status IN ('PLANNED', 'ISSUED', 'IN_PROGRESS', 'QC_PENDING', 'COMPLETED', 'ON_HOLD'));
