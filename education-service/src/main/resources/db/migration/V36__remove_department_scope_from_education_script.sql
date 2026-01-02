-- Remove department_scope column from education_script table
-- department_scope는 Education 엔티티에서 가져올 수 있으므로 중복 제거

ALTER TABLE education.education_script
    DROP COLUMN IF EXISTS department_scope;

