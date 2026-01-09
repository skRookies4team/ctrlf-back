-- V21: QuizAttempt에 department 필드 추가
-- 퀴즈 제출 시 JWT에서 추출한 부서 정보를 저장하여 부서별 통계 계산에 활용

ALTER TABLE education.quiz_attempt
  ADD COLUMN IF NOT EXISTS department VARCHAR(255);

COMMENT ON COLUMN education.quiz_attempt.department IS '응시자 부서 (제출 시 JWT에서 추출하여 저장)';

-- 인덱스 추가 (부서별 통계 조회 성능 향상)
CREATE INDEX IF NOT EXISTS idx_quiz_attempt_department ON education.quiz_attempt(department);
