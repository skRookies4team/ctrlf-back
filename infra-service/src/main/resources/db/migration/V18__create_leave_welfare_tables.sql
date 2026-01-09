-- V18: 연차/복지포인트 관련 테이블 생성 (Q11, Q12, Q14, Q15용)

-- 연차/휴가 사용 이력 테이블 (Q12)
CREATE TABLE IF NOT EXISTS infra.leave_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    days NUMERIC(3, 1) NOT NULL,
    reason VARCHAR(200),
    status VARCHAR(20) DEFAULT 'APPROVED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 복지 포인트 잔액 테이블 (Q14)
CREATE TABLE IF NOT EXISTS infra.welfare_point (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    total_granted INTEGER NOT NULL DEFAULT 0,
    total_used INTEGER NOT NULL DEFAULT 0,
    remaining INTEGER NOT NULL DEFAULT 0,
    year INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_welfare_point_user_year UNIQUE (user_uuid, year)
);

-- 복지 포인트 사용 내역 테이블 (Q15)
CREATE TABLE IF NOT EXISTS infra.welfare_point_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    category VARCHAR(50) NOT NULL,
    merchant VARCHAR(100),
    amount INTEGER NOT NULL,
    usage_date DATE NOT NULL,
    description VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_leave_history_user_date ON infra.leave_history(user_uuid, start_date);
CREATE INDEX IF NOT EXISTS idx_welfare_point_user_year ON infra.welfare_point(user_uuid, year);
CREATE INDEX IF NOT EXISTS idx_welfare_point_usage_user_date ON infra.welfare_point_usage(user_uuid, usage_date);

-- 코멘트
COMMENT ON TABLE infra.leave_history IS '연차/휴가 사용 이력 테이블 (Q11, Q12)';
COMMENT ON TABLE infra.welfare_point IS '복지 포인트 잔액 테이블 (Q14)';
COMMENT ON TABLE infra.welfare_point_usage IS '복지 포인트 사용 내역 테이블 (Q15)';
