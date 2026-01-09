-- V10: rag_document 테이블에 전처리 관련 컬럼 및 content 컬럼 추가
-- 검토자가 전처리 결과를 확인하기 위해 필요한 컬럼들

-- 전처리 관련 컬럼
ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS preprocess_pages INTEGER;

ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS preprocess_chars INTEGER;

ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS preprocess_excerpt TEXT;

-- Milvus에서 조회한 문서 전체 텍스트
ALTER TABLE infra.rag_document
ADD COLUMN IF NOT EXISTS content TEXT;

-- 컬럼 설명
COMMENT ON COLUMN infra.rag_document.preprocess_pages IS '전처리된 페이지 수';
COMMENT ON COLUMN infra.rag_document.preprocess_chars IS '전처리된 문자 수';
COMMENT ON COLUMN infra.rag_document.preprocess_excerpt IS '전처리 미리보기 텍스트';
COMMENT ON COLUMN infra.rag_document.content IS 'Milvus에서 조회한 문서 전체 텍스트 (임베딩 완료 후 원문)';
