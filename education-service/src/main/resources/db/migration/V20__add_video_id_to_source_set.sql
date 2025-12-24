-- V20: SourceSet에 videoId 필드 추가
-- Video가 먼저 생성되고, SourceSet 생성 시 해당 videoId를 연결

ALTER TABLE education.source_set
  ADD COLUMN IF NOT EXISTS video_id uuid;

COMMENT ON COLUMN education.source_set.video_id IS '연결된 영상(EducationVideo) ID';

-- Foreign Key 추가
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'fk_source_set_video'
      AND n.nspname = 'education'
      AND t.relname = 'source_set'
  ) THEN
    ALTER TABLE education.source_set
      ADD CONSTRAINT fk_source_set_video
      FOREIGN KEY (video_id) REFERENCES education.education_video(id);
  END IF;
END $$;

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_source_set_video_id ON education.source_set(video_id);
