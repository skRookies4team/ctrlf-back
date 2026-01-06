-- V5: rag_document status 체크 제약 조건 업데이트
-- 기존: QUEUED, PROCESSING, COMPLETED, FAILED (RAG 문서 처리 상태)
-- 추가: ACTIVE, DRAFT, PENDING, ARCHIVED (사규 관리 상태)

-- 기존 체크 제약 조건 삭제 (있는 경우)
ALTER TABLE infra.rag_document 
    DROP CONSTRAINT IF EXISTS ck_rag_document_status;

-- 새로운 체크 제약 조건 추가 (RAG 문서 처리 상태 + 사규 관리 상태)
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
        'ARCHIVED'
    ));

COMMENT ON CONSTRAINT ck_rag_document_status ON infra.rag_document IS 
    '문서 상태 체크 제약 조건: RAG 문서 처리 상태(QUEUED, PROCESSING, COMPLETED, FAILED, REPROCESSING) 또는 사규 관리 상태(ACTIVE, DRAFT, PENDING, ARCHIVED)';

