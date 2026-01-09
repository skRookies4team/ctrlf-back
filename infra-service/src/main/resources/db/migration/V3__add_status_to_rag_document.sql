-- V3: RagDocument에 status, processed_at 필드 추가
-- 임베딩 처리 상태 추적용

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS status varchar(20) DEFAULT 'QUEUED';

ALTER TABLE infra.rag_document
    ADD COLUMN IF NOT EXISTS processed_at timestamp;

COMMENT ON COLUMN infra.rag_document.status IS '처리 상태 (QUEUED, PROCESSING, COMPLETED, FAILED)';
COMMENT ON COLUMN infra.rag_document.processed_at IS '처리 완료 시각';
