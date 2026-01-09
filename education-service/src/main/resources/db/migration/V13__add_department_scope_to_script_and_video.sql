-- Add department_scope column to education_script and education_video tables
-- 수강 가능한 부서 목록(JSON)

ALTER TABLE education.education_script
    ADD COLUMN IF NOT EXISTS department_scope text;

COMMENT ON COLUMN education.education_script.department_scope IS '수강 가능한 부서 목록(JSON)';

ALTER TABLE education.education_video
    ADD COLUMN IF NOT EXISTS department_scope text;

COMMENT ON COLUMN education.education_video.department_scope IS '수강 가능한 부서 목록(JSON)';
