-- Add edu_type column to education.education and backfill from category if missing
ALTER TABLE education.education
    ADD COLUMN IF NOT EXISTS edu_type varchar(50);

-- Optional backfill: assume legacy category stored edu type keywords
UPDATE education.education
SET edu_type = CASE
                  WHEN category IN ('MANDATORY','JOB','ETC') THEN category
                  ELSE edu_type
               END
WHERE edu_type IS NULL;

