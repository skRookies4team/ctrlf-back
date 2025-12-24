-- Drop department_scope column from education table
ALTER TABLE education.education DROP COLUMN IF EXISTS department_scope;
