-- V22: education_script에 source_set_id 컬럼 추가 (D-2)
-- 멀티문서 지원: Script가 SourceSet과 연결됨

ALTER TABLE education.education_script
  ADD COLUMN IF NOT EXISTS source_set_id uuid;

COMMENT ON COLUMN education.education_script.source_set_id IS '연결된 소스셋 ID (멀티문서 지원)';

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_education_script_source_set_id ON education.education_script(source_set_id);
