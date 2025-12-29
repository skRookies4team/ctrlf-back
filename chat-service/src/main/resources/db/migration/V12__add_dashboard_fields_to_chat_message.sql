-- 관리자 대시보드를 위한 필드 추가
ALTER TABLE chat.chat_message
  ADD COLUMN routing_type VARCHAR(50),  -- RAG, LLM, FAQ, INCIDENT, OTHER
  ADD COLUMN response_time_ms INTEGER,  -- 응답 시간 (밀리초)
  ADD COLUMN pii_detected BOOLEAN DEFAULT FALSE,  -- PII 감지 여부
  ADD COLUMN keyword VARCHAR(200);  -- 질문 키워드 (추출된 주요 키워드)

-- 기존 데이터에 대한 기본값 설정
UPDATE chat.chat_message
SET routing_type = CASE
  WHEN role = 'assistant' THEN 'LLM'  -- 기본값으로 LLM 설정 (나중에 실제 라우팅 정보로 업데이트 필요)
  ELSE NULL
END;

