-- V13: V10과 V11에서 누락된 컬럼 추가
-- V10과 V11이 체크섬 불일치로 인해 실제로 적용되지 않았을 수 있으므로
-- 누락된 컬럼들을 안전하게 추가합니다.

-- V10에서 추가해야 할 컬럼들
ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS preprocess_pages INTEGER;

ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS preprocess_chars INTEGER;

ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS preprocess_excerpt TEXT;

ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS content TEXT;

-- V11에서 추가해야 할 컬럼들
ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS reject_reason TEXT;

ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP;

-- 컬럼 설명 추가
COMMENT ON COLUMN infra.rag_document.preprocess_pages IS '전처리된 페이지 수';
COMMENT ON COLUMN infra.rag_document.preprocess_chars IS '전처리된 문자 수';
COMMENT ON COLUMN infra.rag_document.preprocess_excerpt IS '전처리 미리보기 텍스트';
COMMENT ON COLUMN infra.rag_document.content IS 'Milvus에서 조회한 문서 전체 텍스트 (임베딩 완료 후 원문)';
COMMENT ON COLUMN infra.rag_document.reject_reason IS '반려 사유';
COMMENT ON COLUMN infra.rag_document.rejected_at IS '반려 시각';

