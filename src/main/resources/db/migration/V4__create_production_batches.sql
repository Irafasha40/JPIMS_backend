CREATE TABLE IF NOT EXISTS production_batches (
  id UUID PRIMARY KEY,
  batch_number VARCHAR(50) UNIQUE NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  target_quantity NUMERIC(19,4) NOT NULL,
  actual_yield NUMERIC(19,4),
  loss NUMERIC(19,4),
  loss_reason VARCHAR(255),
  status VARCHAR(50) NOT NULL,
  production_date DATE,
  start_time TIMESTAMPTZ,
  completion_time TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100)
);
