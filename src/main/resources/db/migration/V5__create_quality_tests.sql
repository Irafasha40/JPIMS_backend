CREATE TABLE IF NOT EXISTS quality_tests (
  id UUID PRIMARY KEY,
  batch_id UUID NOT NULL REFERENCES production_batches(id),
  tested_by UUID NOT NULL REFERENCES users(id),
  ph_level NUMERIC(10,4) NOT NULL,
  brix_level NUMERIC(10,4) NOT NULL,
  appearance VARCHAR(50),
  color VARCHAR(50),
  taste VARCHAR(50),
  result VARCHAR(20),
  notes TEXT,
  test_date TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100)
);
