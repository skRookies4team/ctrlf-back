-- V17: HR 관련 테이블 생성 (Q10, Q13, Q16, Q17용)

-- 부서 정보 테이블 (Q17)
CREATE TABLE IF NOT EXISTS infra.department (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    department_code VARCHAR(20) NOT NULL UNIQUE,
    department_name VARCHAR(50) NOT NULL,
    parent_department_uuid UUID,
    parent_department_name VARCHAR(50),
    leader_uuid UUID,
    leader_name VARCHAR(50),
    leader_position VARCHAR(30),
    level INTEGER,
    sort_order INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 직원(인사) 정보 테이블 (Q16)
CREATE TABLE IF NOT EXISTS infra.employee (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL UNIQUE,
    employee_id VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    department_uuid UUID,
    department_name VARCHAR(50),
    position VARCHAR(30),
    job_title VARCHAR(50),
    hire_date DATE,
    email VARCHAR(100),
    phone VARCHAR(20),
    office_phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_uuid) REFERENCES infra.department(id)
);

-- 근태 기록 테이블 (Q10)
CREATE TABLE IF NOT EXISTS infra.attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    work_date DATE NOT NULL,
    check_in TIME,
    check_out TIME,
    work_hours NUMERIC(4, 2),
    status VARCHAR(20),
    work_type VARCHAR(20),
    overtime_hours NUMERIC(4, 2),
    note VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 급여 명세 테이블 (Q13)
CREATE TABLE IF NOT EXISTS infra.salary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    pay_year INTEGER NOT NULL,
    pay_month INTEGER NOT NULL,
    base_salary INTEGER,
    overtime_pay INTEGER,
    bonus INTEGER,
    meal_allowance INTEGER,
    transport_allowance INTEGER,
    total_earnings INTEGER,
    income_tax INTEGER,
    local_tax INTEGER,
    national_pension INTEGER,
    health_insurance INTEGER,
    long_term_care INTEGER,
    employment_insurance INTEGER,
    total_deductions INTEGER,
    net_pay INTEGER,
    pay_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_salary_user_month UNIQUE (user_uuid, pay_year, pay_month)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_attendance_user_date ON infra.attendance(user_uuid, work_date);
CREATE INDEX IF NOT EXISTS idx_salary_user_year_month ON infra.salary(user_uuid, pay_year, pay_month);
CREATE INDEX IF NOT EXISTS idx_employee_department ON infra.employee(department_uuid);

-- 샘플 데이터: 부서
INSERT INTO infra.department (id, department_code, department_name, level, sort_order, is_active)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, 'D001', '개발팀', 2, 1, true),
    ('00000000-0000-0000-0000-000000000002'::uuid, 'D002', '기획팀', 2, 2, true),
    ('00000000-0000-0000-0000-000000000003'::uuid, 'D003', '인사팀', 2, 3, true)
ON CONFLICT (department_code) DO NOTHING;

-- 코멘트
COMMENT ON TABLE infra.department IS '부서 정보 테이블 (Q17)';
COMMENT ON TABLE infra.employee IS '직원 인사 정보 테이블 (Q16)';
COMMENT ON TABLE infra.attendance IS '근태 기록 테이블 (Q10)';
COMMENT ON TABLE infra.salary IS '급여 명세 테이블 (Q13)';
