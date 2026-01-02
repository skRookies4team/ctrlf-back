-- V9: 전처리 상태, 검토 정보, 히스토리 필드 추가

-- rag_document 테이블에 전처리 및 검토 관련 필드 추가
ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS preprocess_status varchar(20) DEFAULT 'IDLE';

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS preprocess_pages integer;

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS preprocess_chars integer;

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS preprocess_excerpt text;

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS preprocess_error text;

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS review_requested_at timestamp;

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS review_item_id varchar(100);

-- preprocess_status 체크 제약 조건 추가
ALTER TABLE infra.rag_document
    DROP CONSTRAINT IF EXISTS ck_rag_document_preprocess_status;

ALTER TABLE infra.rag_document
    ADD CONSTRAINT ck_rag_document_preprocess_status
    CHECK (preprocess_status IN ('IDLE', 'PROCESSING', 'READY', 'FAILED'));

-- 히스토리 테이블 생성
CREATE TABLE IF NOT EXISTS infra.rag_document_history (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id varchar(50) NOT NULL,
    version integer NOT NULL,
    action varchar(50) NOT NULL,
    actor varchar(100),
    message text,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rag_document_history_doc_version 
    ON infra.rag_document_history(document_id, version);

COMMENT ON COLUMN infra.rag_document.preprocess_status IS '전처리 상태: IDLE, PROCESSING, READY, FAILED';
COMMENT ON COLUMN infra.rag_document.preprocess_pages IS '전처리된 페이지 수';
COMMENT ON COLUMN infra.rag_document.preprocess_chars IS '전처리된 문자 수';
COMMENT ON COLUMN infra.rag_document.preprocess_excerpt IS '전처리 미리보기 텍스트';
COMMENT ON COLUMN infra.rag_document.preprocess_error IS '전처리 실패 사유';
COMMENT ON COLUMN infra.rag_document.review_requested_at IS '검토 요청 시각';
COMMENT ON COLUMN infra.rag_document.review_item_id IS '검토 항목 ID';
COMMENT ON TABLE infra.rag_document_history IS '사규 문서 히스토리 테이블';

