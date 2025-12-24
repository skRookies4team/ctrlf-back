-- video_generation_job 테이블에 duration 컬럼 추가
ALTER TABLE education.video_generation_job 
ADD COLUMN IF NOT EXISTS duration int;

COMMENT ON COLUMN education.video_generation_job.duration IS '생성된 영상 길이(초)';
