-- quiz_attempt 테이블에 version 컬럼 추가
ALTER TABLE education.quiz_attempt
ADD COLUMN IF NOT EXISTS version INTEGER;

COMMENT ON COLUMN education.quiz_attempt.version IS '교육 버전';

