-- LLM 모델 선택 필드 추가
-- 관리자 대시보드에서 설정하여 exaone 또는 openai 선택 가능

ALTER TABLE chat.chat_session
ADD COLUMN llm_model VARCHAR(20);

-- 기존 세션은 기본값(exaone)으로 설정
UPDATE chat.chat_session
SET llm_model = 'exaone'
WHERE llm_model IS NULL;

-- 인덱스 추가 (분석 쿼리 성능 향상)
CREATE INDEX IF NOT EXISTS idx_chat_session_llm_model
ON chat.chat_session(llm_model);
