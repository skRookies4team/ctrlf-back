-- Normalize education.category to enum-like string values (no reference to edu_type here)

-- 2) Map Korean labels to new enum string tokens in category
UPDATE education.education SET category = 'JOB_DUTY'
WHERE category IN ('직무');

UPDATE education.education SET category = 'SEXUAL_HARASSMENT_PREVENTION'
WHERE category IN ('성희롱 예방');

UPDATE education.education SET category = 'PERSONAL_INFO_PROTECTION'
WHERE category IN ('개인 정보 보호','개인정보 보호');

UPDATE education.education SET category = 'WORKPLACE_BULLYING'
WHERE category IN ('직장 내 괴롭힘');

UPDATE education.education SET category = 'DISABILITY_AWARENESS'
WHERE category IN ('장애인 인식 개선');

-- 3) For rows still holding legacy edu_type values in category, set a safe default topic
UPDATE education.education
SET category = 'JOB_DUTY'
WHERE category IN ('MANDATORY','JOB','ETC') OR category IS NULL;

-- 4) Add a check constraint to keep category within allowed values
DO $$
BEGIN
  -- Drop existing constraint if present
  IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE constraint_schema = 'education'
      AND table_name = 'education'
      AND constraint_name = 'education_category_chk'
  ) THEN
    EXECUTE 'ALTER TABLE education.education DROP CONSTRAINT education_category_chk';
  END IF;
END
$$;

ALTER TABLE education.education
  ADD CONSTRAINT education_category_chk
  CHECK (category IN (
    'JOB_DUTY',
    'SEXUAL_HARASSMENT_PREVENTION',
    'PERSONAL_INFO_PROTECTION',
    'WORKPLACE_BULLYING',
    'DISABILITY_AWARENESS'
  ));

