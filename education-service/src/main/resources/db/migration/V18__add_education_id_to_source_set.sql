-- V18: source_set에 education_id 추가
-- 소스셋이 특정 education을 위해 생성되는 경우를 명시적으로 관리

ALTER TABLE education.source_set
  ADD COLUMN IF NOT EXISTS education_id uuid;

COMMENT ON COLUMN education.source_set.education_id IS '연결된 교육 ID (선택적)';

-- Foreign Key 추가
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'fk_source_set_education'
      AND n.nspname = 'education'
      AND t.relname = 'source_set'
  ) THEN
    ALTER TABLE education.source_set
      ADD CONSTRAINT fk_source_set_education
      FOREIGN KEY (education_id) REFERENCES education.education(id);
  END IF;
END $$;

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_source_set_education_id ON education.source_set(education_id);
