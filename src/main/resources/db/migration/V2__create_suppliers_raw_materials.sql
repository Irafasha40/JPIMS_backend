CREATE TABLE IF NOT EXISTS raw_materials (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  unit_of_measure VARCHAR(50),
  current_stock NUMERIC(19,4) NOT NULL,
  minimum_threshold NUMERIC(19,4) NOT NULL,
  cost_per_unit NUMERIC(19,4) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100)
);
