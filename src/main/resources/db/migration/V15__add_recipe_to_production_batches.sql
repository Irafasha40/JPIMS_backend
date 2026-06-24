CREATE TABLE IF NOT EXISTS recipes (
  id UUID PRIMARY KEY,
  name VARCHAR(255),
  product_name VARCHAR(255),
  version INTEGER,
  base_quantity NUMERIC(19,4),
  status VARCHAR(50),
  cost_per_batch NUMERIC(19,4),
  notes TEXT,
  approved_by UUID,
  approved_at TIMESTAMPTZ,
  calories NUMERIC(19,4),
  sugar_content NUMERIC(19,4),
  vitamin_c NUMERIC(19,4),
  shelf_life_days INTEGER,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS recipe_ingredients (
  id UUID PRIMARY KEY,
  recipe_id UUID NOT NULL REFERENCES recipes(id),
  raw_material_id UUID NOT NULL,
  quantity NUMERIC(19,4),
  percentage NUMERIC(19,4),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS recipe_versions (
  id UUID PRIMARY KEY,
  recipe_id UUID NOT NULL REFERENCES recipes(id),
  changed_by UUID,
  version INTEGER,
  change_description VARCHAR(255),
  changed_at TIMESTAMPTZ,
  snapshot_json TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100)
);

ALTER TABLE production_batches
  ADD COLUMN IF NOT EXISTS recipe_id UUID;

ALTER TABLE production_batches
  ADD CONSTRAINT fk_production_batches_recipe
  FOREIGN KEY (recipe_id) REFERENCES recipes(id);

UPDATE production_batches pb
SET recipe_id = r.id
FROM recipes r
WHERE pb.recipe_id IS NULL
  AND pb.product_name = r.product_name
  AND r.product_name IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM production_batches pb2
    WHERE pb2.id <> pb.id
      AND pb2.recipe_id = r.id
  );
