-- V23: education_script_scene에 source_refs jsonb 컬럼 추가 (D-3)
-- 멀티문서 지원: Scene의 출처를 여러 문서의 chunk로 표현
-- 형식: [{"documentId": "uuid", "chunkIndex": 0}, ...]

ALTER TABLE education.education_script_scene
  ADD COLUMN IF NOT EXISTS source_refs jsonb;

COMMENT ON COLUMN education.education_script_scene.source_refs IS '출처 참조 (멀티문서): [{"documentId": "uuid", "chunkIndex": 0}, ...]';
COMMENT ON COLUMN education.education_script_scene.source_chunk_indexes IS '레거시: 단일 문서일 때만 사용 (deprecated)';
