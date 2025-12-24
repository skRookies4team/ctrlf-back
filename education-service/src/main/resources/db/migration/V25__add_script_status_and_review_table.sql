-- V25: 스크립트 승인 상태 및 리뷰 테이블 추가 (D-5)
-- 스크립트 승인 플로우 지원: DRAFT → REVIEW_REQUESTED → APPROVED/REJECTED

-- 1. education_script에 status 컬럼 추가
ALTER TABLE education.education_script
  ADD COLUMN IF NOT EXISTS status varchar(20) DEFAULT 'DRAFT';

COMMENT ON COLUMN education.education_script.status IS '스크립트 상태: DRAFT(초안), REVIEW_REQUESTED(검토요청), APPROVED(승인), REJECTED(반려)';

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_education_script_status ON education.education_script(status);

-- 2. education_script_review 테이블 생성 (education_video_review 패턴)
CREATE TABLE IF NOT EXISTS education.education_script_review (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  script_id uuid NOT NULL,
  reviewer_uuid uuid NOT NULL,
  status varchar(20) NOT NULL, -- PENDING, APPROVED, REJECTED
  comment text,
  created_at timestamp NOT NULL DEFAULT now(),
  deleted_at timestamp
);

COMMENT ON TABLE education.education_script_review IS '스크립트 검수(리뷰) 이력';
COMMENT ON COLUMN education.education_script_review.script_id IS '대상 스크립트 ID';
COMMENT ON COLUMN education.education_script_review.reviewer_uuid IS '리뷰어(검수자) UUID';
COMMENT ON COLUMN education.education_script_review.status IS '리뷰 상태: PENDING, APPROVED, REJECTED';
COMMENT ON COLUMN education.education_script_review.comment IS '검수 코멘트';

-- 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_education_script_review_script_id ON education.education_script_review(script_id);
CREATE INDEX IF NOT EXISTS idx_education_script_review_reviewer_uuid ON education.education_script_review(reviewer_uuid);
CREATE INDEX IF NOT EXISTS idx_education_script_review_status ON education.education_script_review(status);
