-- V15: EducationVideo에 scriptId, materialId 필드 추가
-- 스크립트 및 자료(RagDocument)와의 연결을 위한 FK 필드

ALTER TABLE education.education_video
    ADD COLUMN IF NOT EXISTS script_id uuid;

ALTER TABLE education.education_video
    ADD COLUMN IF NOT EXISTS material_id uuid;

COMMENT ON COLUMN education.education_video.script_id IS '연결된 스크립트 ID';
COMMENT ON COLUMN education.education_video.material_id IS '연결된 자료(RagDocument) ID';
