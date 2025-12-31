-- education 테이블에 version 컬럼 추가
ALTER TABLE education.education
ADD COLUMN IF NOT EXISTS version INTEGER;

COMMENT ON COLUMN education.education.version IS '교육 버전';

