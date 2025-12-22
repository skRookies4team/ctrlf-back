-- V19: education_source_doc 테이블 및 education_script.source_doc_id 컬럼 제거
-- 새로운 플로우에서는 source_set + source_set_document가 infra.rag_document를 참조하므로
-- education_source_doc는 더 이상 사용하지 않음

-- 1. education_script.source_doc_id의 FK 제약 제거
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON t.oid = c.conrelid
    JOIN pg_namespace n ON n.oid = t.relnamespace
    WHERE c.conname = 'education_script_source_doc_id_fkey'
      AND n.nspname = 'education'
      AND t.relname = 'education_script'
  ) THEN
    ALTER TABLE education.education_script
      DROP CONSTRAINT education_script_source_doc_id_fkey;
  END IF;
END $$;

-- 2. education_script.source_doc_id 컬럼 제거
ALTER TABLE education.education_script
  DROP COLUMN IF EXISTS source_doc_id;

-- 3. education_source_doc 테이블 삭제
DROP TABLE IF EXISTS education.education_source_doc CASCADE;
