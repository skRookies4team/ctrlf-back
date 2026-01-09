-- V17: 소스셋(SourceSet) 테이블 추가
-- 여러 문서를 묶어 "스크립트 1개/영상 1개"를 만드는 제작 단위

-- 소스셋 메인 테이블
CREATE TABLE IF NOT EXISTS education.source_set (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title varchar(255) NOT NULL,
  domain varchar(50),
  status varchar(20) NOT NULL DEFAULT 'CREATED', -- CREATED, LOCKED, SCRIPT_READY, FAILED
  requested_by varchar(36), -- 요청자 UUID
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp,
  deleted_at timestamp
);

COMMENT ON TABLE education.source_set IS '소스셋: 여러 문서를 묶어 스크립트/영상 제작 단위';
COMMENT ON COLUMN education.source_set.status IS '상태: CREATED(생성됨), LOCKED(잠김), SCRIPT_READY(스크립트 준비), FAILED(실패)';

-- 소스셋과 문서의 관계 테이블 (다대다)
CREATE TABLE IF NOT EXISTS education.source_set_document (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  source_set_id uuid NOT NULL REFERENCES education.source_set(id) ON DELETE CASCADE,
  document_id uuid NOT NULL, -- infra.rag_document.id (스키마 간 참조이므로 FK 제약 없음)
  created_at timestamp NOT NULL DEFAULT now(),
  UNIQUE(source_set_id, document_id)
);

COMMENT ON TABLE education.source_set_document IS '소스셋과 문서의 관계 (다대다)';
COMMENT ON COLUMN education.source_set_document.document_id IS 'infra.rag_document.id 참조';

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_source_set_status ON education.source_set(status);
CREATE INDEX IF NOT EXISTS idx_source_set_created_at ON education.source_set(created_at);
CREATE INDEX IF NOT EXISTS idx_source_set_document_source_set_id ON education.source_set_document(source_set_id);
CREATE INDEX IF NOT EXISTS idx_source_set_document_document_id ON education.source_set_document(document_id);
