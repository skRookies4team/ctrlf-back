-- V24: education_video에 source_set_id 컬럼 추가 (D-4)
-- 멀티문서 지원: Video가 SourceSet과 연결됨

ALTER TABLE education.education_video
  ADD COLUMN IF NOT EXISTS source_set_id uuid;

COMMENT ON COLUMN education.education_video.source_set_id IS '연결된 소스셋 ID (멀티문서 지원)';
COMMENT ON COLUMN education.education_video.material_id IS '레거시: 단일 문서일 때만 사용 (deprecated)';

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_education_video_source_set_id ON education.education_video(source_set_id);
