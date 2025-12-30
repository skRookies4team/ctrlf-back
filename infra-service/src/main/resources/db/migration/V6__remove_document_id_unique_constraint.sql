-- V6: document_id의 UNIQUE 제약 제거
-- 같은 documentId에 여러 버전이 있을 수 있으므로 UNIQUE 제약을 제거합니다.

-- 기존 UNIQUE 인덱스 삭제
DROP INDEX IF EXISTS infra.idx_rag_document_document_id;

-- document_id와 version의 복합 인덱스 추가 (조회 성능 향상)
CREATE INDEX IF NOT EXISTS idx_rag_document_document_id_version 
    ON infra.rag_document(document_id, version);

COMMENT ON INDEX infra.idx_rag_document_document_id_version IS 
    'document_id와 version의 복합 인덱스 (버전별 조회 성능 향상)';

