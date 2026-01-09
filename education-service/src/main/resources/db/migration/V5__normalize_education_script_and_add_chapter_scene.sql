-- Normalize education_script and add chapter/scene tables

-- 1) education_script: add new columns
ALTER TABLE education.education_script
  ADD COLUMN IF NOT EXISTS title varchar(255),
  ADD COLUMN IF NOT EXISTS total_duration_sec int,
  ADD COLUMN IF NOT EXISTS llm_model varchar(50),
  ADD COLUMN IF NOT EXISTS generation_prompt_hash char(64),
  ADD COLUMN IF NOT EXISTS raw_payload jsonb;

-- 2) optional: migrate legacy content (if present) into raw_payload as JSON string
--    Only when content column exists and contains valid JSON strings.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = 'education'
      AND table_name = 'education_script'
      AND column_name = 'content'
  ) THEN
    -- Try to cast content to jsonb; if invalid, wrap as JSON string
    BEGIN
      UPDATE education.education_script
      SET raw_payload = CASE
        WHEN content IS NULL THEN NULL
        WHEN content ~ '^[\\s\\r\\n]*\\{' THEN content::jsonb
        WHEN content ~ '^[\\s\\r\\n]*\\[' THEN content::jsonb
        ELSE to_jsonb(content)
      END;
    EXCEPTION WHEN others THEN
      -- Fallback: wrap everything as JSON string to avoid migration failure
      UPDATE education.education_script
      SET raw_payload = to_jsonb(content);
    END;
  END IF;
END $$;

-- 3) drop legacy content column if exists
ALTER TABLE education.education_script
  DROP COLUMN IF EXISTS content;

-- 4) create chapter table
CREATE TABLE IF NOT EXISTS education.education_script_chapter (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  script_id uuid,
  chapter_index int,
  title varchar(200),
  duration_sec int,
  created_at timestamp,
  deleted_at timestamp
);

-- 5) create scene table
CREATE TABLE IF NOT EXISTS education.education_script_scene (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  script_id uuid,
  chapter_id uuid,
  scene_index int,
  purpose varchar(30),
  narration text,
  caption text,
  visual text,
  duration_sec int,
  source_chunk_indexes int[],
  confidence_score float,
  created_at timestamp,
  deleted_at timestamp
);

-- 6) add foreign keys
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'fk_script_chapter_script'
      AND n.nspname = 'education'
  ) THEN
    ALTER TABLE education.education_script_chapter
      ADD CONSTRAINT fk_script_chapter_script
      FOREIGN KEY (script_id) REFERENCES education.education_script (id);
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'fk_script_scene_script'
      AND n.nspname = 'education'
  ) THEN
    ALTER TABLE education.education_script_scene
      ADD CONSTRAINT fk_script_scene_script
      FOREIGN KEY (script_id) REFERENCES education.education_script (id);
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'fk_script_scene_chapter'
      AND n.nspname = 'education'
  ) THEN
    ALTER TABLE education.education_script_scene
      ADD CONSTRAINT fk_script_scene_chapter
      FOREIGN KEY (chapter_id) REFERENCES education.education_script_chapter (id);
  END IF;
END $$;

