-- 대시보드 필드 추가 (IF NOT EXISTS로 안전하게 추가)
DO $$
BEGIN
    -- 컬럼 추가 (이미 존재하면 무시)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'chat' 
                   AND table_name = 'chat_message' 
                   AND column_name = 'routing_type') THEN
        ALTER TABLE chat.chat_message ADD COLUMN routing_type VARCHAR(50);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'chat' 
                   AND table_name = 'chat_message' 
                   AND column_name = 'pii_detected') THEN
        ALTER TABLE chat.chat_message ADD COLUMN pii_detected BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'chat' 
                   AND table_name = 'chat_message' 
                   AND column_name = 'response_time_ms') THEN
        ALTER TABLE chat.chat_message ADD COLUMN response_time_ms BIGINT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'chat' 
                   AND table_name = 'chat_message' 
                   AND column_name = 'is_error') THEN
        ALTER TABLE chat.chat_message ADD COLUMN is_error BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'chat' 
                   AND table_name = 'chat_message' 
                   AND column_name = 'department') THEN
        ALTER TABLE chat.chat_message ADD COLUMN department VARCHAR(100);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'chat' 
                   AND table_name = 'chat_message' 
                   AND column_name = 'keyword') THEN
        ALTER TABLE chat.chat_message ADD COLUMN keyword VARCHAR(200);
    END IF;
END $$;

-- 인덱스 생성 (이미 존재하면 무시)
CREATE INDEX IF NOT EXISTS idx_chat_message_routing_type ON chat.chat_message (routing_type);
CREATE INDEX IF NOT EXISTS idx_chat_message_created_at ON chat.chat_message (created_at);
CREATE INDEX IF NOT EXISTS idx_chat_message_department ON chat.chat_message (department);
CREATE INDEX IF NOT EXISTS idx_chat_message_keyword ON chat.chat_message (keyword);

