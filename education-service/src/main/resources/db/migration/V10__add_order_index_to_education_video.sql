-- Add order_index column for video ordering per education
ALTER TABLE education.education_video
    ADD COLUMN IF NOT EXISTS order_index integer NOT NULL DEFAULT 0;

-- Backfill order_index per education by created_at asc
WITH ranked AS (
  SELECT id,
         ROW_NUMBER() OVER (PARTITION BY education_id ORDER BY created_at ASC, id ASC) - 1 AS rn
  FROM education.education_video
)
UPDATE education.education_video v
SET order_index = r.rn
FROM ranked r
WHERE v.id = r.id;

-- Helpful index for listing videos by education and order
CREATE INDEX IF NOT EXISTS idx_education_video_education_id_order
  ON education.education_video (education_id, order_index);
