-- Backfill edu_type based on normalized category
-- Assumes V7 added edu_type column and V4 normalized category values.

UPDATE education.education
SET edu_type = CASE
  WHEN category = 'JOB_DUTY' THEN 'JOB'
  WHEN category IN ('SEXUAL_HARASSMENT_PREVENTION','PERSONAL_INFO_PROTECTION','WORKPLACE_BULLYING','DISABILITY_AWARENESS') THEN 'MANDATORY'
  ELSE COALESCE(edu_type, 'ETC')
END
WHERE edu_type IS NULL;

