CREATE SCHEMA IF NOT EXISTS chat;
CREATE SCHEMA IF NOT EXISTS education;
CREATE SCHEMA IF NOT EXISTS infra;

-- ============================================================================
-- Q12: 연차 사용 이력 테이블
-- ============================================================================
CREATE TABLE IF NOT EXISTS infra.leave_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    leave_type VARCHAR(20) NOT NULL,  -- 연차, 반차, 병가, 경조사 등
    start_date DATE NOT NULL,
    end_date DATE,
    days DECIMAL(3,1) NOT NULL,  -- 0.5(반차), 1, 2, ...
    reason VARCHAR(200),
    status VARCHAR(20) DEFAULT 'APPROVED',  -- APPROVED, PENDING, REJECTED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_leave_history_user_uuid ON infra.leave_history(user_uuid);
CREATE INDEX IF NOT EXISTS idx_leave_history_start_date ON infra.leave_history(start_date);

COMMENT ON TABLE infra.leave_history IS '연차/휴가 사용 이력';
COMMENT ON COLUMN infra.leave_history.leave_type IS '휴가 유형 (연차, 반차, 병가, 경조사 등)';
COMMENT ON COLUMN infra.leave_history.days IS '사용 일수 (0.5=반차, 1=연차 등)';

-- ============================================================================
-- Q15: 복지 포인트 테이블
-- ============================================================================
CREATE TABLE IF NOT EXISTS infra.welfare_point (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL UNIQUE,
    total_granted INT NOT NULL DEFAULT 0,  -- 총 지급액
    total_used INT NOT NULL DEFAULT 0,     -- 총 사용액
    remaining INT NOT NULL DEFAULT 0,      -- 잔액
    year INT NOT NULL,                     -- 기준 연도
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_welfare_point_user_uuid ON infra.welfare_point(user_uuid);

COMMENT ON TABLE infra.welfare_point IS '복지 포인트 잔액';
COMMENT ON COLUMN infra.welfare_point.total_granted IS '연간 총 지급액';
COMMENT ON COLUMN infra.welfare_point.total_used IS '연간 총 사용액';
COMMENT ON COLUMN infra.welfare_point.remaining IS '현재 잔액';

-- ============================================================================
-- Q15: 복지 포인트 사용 내역 테이블
-- ============================================================================
CREATE TABLE IF NOT EXISTS infra.welfare_point_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    category VARCHAR(50) NOT NULL,      -- 건강/의료, 자기계발, 여가/문화 등
    merchant VARCHAR(100),              -- 사용처
    amount INT NOT NULL,                -- 사용 금액
    usage_date DATE NOT NULL,
    description VARCHAR(200),           -- 사용 내역 설명
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_welfare_point_usage_user_uuid ON infra.welfare_point_usage(user_uuid);
CREATE INDEX IF NOT EXISTS idx_welfare_point_usage_date ON infra.welfare_point_usage(usage_date);

COMMENT ON TABLE infra.welfare_point_usage IS '복지 포인트 사용 내역';
COMMENT ON COLUMN infra.welfare_point_usage.category IS '사용 카테고리 (건강/의료, 자기계발, 여가/문화 등)';

-- ============================================================================
-- 테스트 데이터 삽입 (샘플 사용자 UUID 사용)
-- ============================================================================
-- 테스트용 사용자 UUID (실제 환경에서는 infra.user 테이블의 실제 UUID 사용)
DO $$
DECLARE
    test_user_uuid UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    -- Q12: 연차 사용 이력 샘플 데이터
    INSERT INTO infra.leave_history (user_uuid, leave_type, start_date, end_date, days, reason) VALUES
        (test_user_uuid, '연차', '2025-01-02', '2025-01-03', 2, '개인 사유'),
        (test_user_uuid, '연차', '2024-12-24', '2024-12-25', 2, '연말 휴가'),
        (test_user_uuid, '반차', '2024-11-15', '2024-11-15', 0.5, '병원 방문'),
        (test_user_uuid, '연차', '2024-10-01', '2024-10-02', 2, '가족 행사'),
        (test_user_uuid, '반차', '2024-09-10', '2024-09-10', 0.5, '개인 사유')
    ON CONFLICT DO NOTHING;

    -- Q15: 복지 포인트 잔액 샘플 데이터
    INSERT INTO infra.welfare_point (user_uuid, total_granted, total_used, remaining, year) VALUES
        (test_user_uuid, 500000, 350000, 150000, 2025)
    ON CONFLICT (user_uuid) DO UPDATE SET
        total_granted = EXCLUDED.total_granted,
        total_used = EXCLUDED.total_used,
        remaining = EXCLUDED.remaining,
        updated_at = CURRENT_TIMESTAMP;

    -- Q15: 복지 포인트 사용 내역 샘플 데이터
    INSERT INTO infra.welfare_point_usage (user_uuid, category, merchant, amount, usage_date, description) VALUES
        (test_user_uuid, '건강/의료', '강남세브란스병원', 85000, '2025-01-03', '건강검진 비용'),
        (test_user_uuid, '자기계발', '교보문고', 45000, '2024-12-20', '도서 구입'),
        (test_user_uuid, '여가/문화', 'CGV', 28000, '2024-12-15', '영화 관람'),
        (test_user_uuid, '건강/의료', '올리브영', 52000, '2024-11-28', '건강용품 구입'),
        (test_user_uuid, '자기계발', '클래스101', 99000, '2024-10-15', '온라인 강의 수강'),
        (test_user_uuid, '여가/문화', '스타벅스', 41000, '2024-09-20', '카페 이용')
    ON CONFLICT DO NOTHING;
END $$;

-- chat.chat_session 테이블 생성 추가
CREATE TABLE IF NOT EXISTS chat.chat_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID,
    title VARCHAR(255),
    domain VARCHAR(255),
    summary TEXT,
    intent VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted BOOLEAN,
    embedding_model VARCHAR(20),
    llm_model VARCHAR(20)
);