-- V16: rag_document status 체크 제약 조건에 REJECTED 추가
-- REJECTED 상태가 enum에는 정의되어 있지만 제약 조건에 누락되어 있어 추가

-- 기존 체크 제약 조건 삭제
ALTER TABLE infra.rag_document 
    DROP CONSTRAINT IF EXISTS ck_rag_document_status;

-- REJECTED를 포함한 새로운 체크 제약 조건 추가
ALTER TABLE infra.rag_document
    ADD CONSTRAINT ck_rag_document_status 
    CHECK (status IN (
        -- RAG 문서 처리 상태
        'QUEUED', 
        'PROCESSING', 
        'COMPLETED', 
        'FAILED',
        'REPROCESSING',
        -- 사규 관리 상태
        'ACTIVE',
        'DRAFT',
        'PENDING',
        'REJECTED',
        'ARCHIVED'
    ));

COMMENT ON CONSTRAINT ck_rag_document_status ON infra.rag_document IS 
    '문서 상태 체크 제약 조건: RAG 문서 처리 상태(QUEUED, PROCESSING, COMPLETED, FAILED, REPROCESSING) 또는 사규 관리 상태(ACTIVE, DRAFT, PENDING, REJECTED, ARCHIVED)';

