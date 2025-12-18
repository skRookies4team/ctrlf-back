-- Add title column to education_video table
-- 교육 영상(컨텐츠) 제목

ALTER TABLE education.education_video
    ADD COLUMN IF NOT EXISTS title varchar(255);

COMMENT ON COLUMN education.education_video.title IS '교육 영상 제목';
