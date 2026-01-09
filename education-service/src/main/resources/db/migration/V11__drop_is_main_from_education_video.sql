-- Drop is_main column; order_index will be used for ordering
ALTER TABLE education.education_video
    DROP COLUMN IF EXISTS is_main;