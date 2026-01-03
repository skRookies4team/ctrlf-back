-- A/B 테스트를 위한 임베딩 모델 필드 추가
-- 세션 생성 시 할당된 모델을 저장하여 일관성 보장

ALTER TABLE chat.chat_session 
ADD COLUMN embedding_model VARCHAR(20);

-- 기존 세션은 기본값(openai)으로 설정
UPDATE chat.chat_session 
SET embedding_model = 'openai' 
WHERE embedding_model IS NULL;

-- 인덱스 추가 (A/B 테스트 분석 쿼리 성능 향상)
CREATE INDEX IF NOT EXISTS idx_chat_session_embedding_model 
ON chat.chat_session(embedding_model);

