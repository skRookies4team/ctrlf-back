-- V19: HR 테이블 샘플 데이터 삽입

-- 테스트용 사용자 UUID
-- 00000000-0000-0000-0000-000000000001 (테스트 사용자 1)
-- 00000000-0000-0000-0000-000000000002 (테스트 사용자 2)

-- 부서 데이터 추가
INSERT INTO infra.department (id, department_code, department_name, parent_department_name, leader_name, leader_position, level, sort_order, is_active)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, 'D001', '개발팀', 'IT본부', '김팀장', '부장', 2, 1, true),
    ('00000000-0000-0000-0000-000000000002'::uuid, 'D002', '기획팀', 'IT본부', '이팀장', '차장', 2, 2, true),
    ('00000000-0000-0000-0000-000000000003'::uuid, 'D003', '인사팀', '경영지원본부', '박팀장', '부장', 2, 3, true)
ON CONFLICT (department_code) DO UPDATE SET
    department_name = EXCLUDED.department_name,
    parent_department_name = EXCLUDED.parent_department_name,
    leader_name = EXCLUDED.leader_name,
    leader_position = EXCLUDED.leader_position;

-- 직원 정보 (Q16)
INSERT INTO infra.employee (id, user_uuid, employee_id, name, department_uuid, department_name, position, job_title, hire_date, email, phone, office_phone, status)
VALUES
    ('10000000-0000-0000-0000-000000000001'::uuid, '00000000-0000-0000-0000-000000000001'::uuid, 'EMP001', '홍길동', '00000000-0000-0000-0000-000000000001'::uuid, '개발팀', '대리', '파트장', '2022-03-15', 'hong@ctrlf.com', '010-1234-5678', '02-1234-1001', 'ACTIVE'),
    ('10000000-0000-0000-0000-000000000002'::uuid, '00000000-0000-0000-0000-000000000002'::uuid, 'EMP002', '김철수', '00000000-0000-0000-0000-000000000001'::uuid, '개발팀', '사원', NULL, '2024-01-02', 'kim@ctrlf.com', '010-2345-6789', '02-1234-1002', 'ACTIVE')
ON CONFLICT (user_uuid) DO UPDATE SET
    name = EXCLUDED.name,
    department_name = EXCLUDED.department_name,
    position = EXCLUDED.position;

-- 근태 기록 (Q10) - 2026년 1월 데이터
INSERT INTO infra.attendance (user_uuid, work_date, check_in, check_out, work_hours, status, work_type, overtime_hours, note)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, '2026-01-02', '09:00', '18:30', 8.5, 'NORMAL', 'OFFICE', 0.5, NULL),
    ('00000000-0000-0000-0000-000000000001'::uuid, '2026-01-03', '09:15', '18:00', 7.75, 'LATE', 'OFFICE', 0, '교통 체증'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '2026-01-06', '09:00', '20:00', 10, 'NORMAL', 'OFFICE', 2, '프로젝트 마감'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '2026-01-07', '09:00', '18:00', 8, 'NORMAL', 'REMOTE', 0, '재택근무');

-- 급여 명세 (Q13) - 2025년 12월, 2026년 1월
INSERT INTO infra.salary (user_uuid, pay_year, pay_month, base_salary, overtime_pay, bonus, meal_allowance, transport_allowance, total_earnings, income_tax, local_tax, national_pension, health_insurance, long_term_care, employment_insurance, total_deductions, net_pay, pay_date)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, 2025, 12, 3500000, 250000, 500000, 100000, 100000, 4450000, 180000, 18000, 157500, 140000, 17000, 35000, 547500, 3902500, '2025-12-25'),
    ('00000000-0000-0000-0000-000000000001'::uuid, 2026, 1, 3500000, 180000, 0, 100000, 100000, 3880000, 150000, 15000, 157500, 140000, 17000, 35000, 514500, 3365500, '2026-01-25')
ON CONFLICT (user_uuid, pay_year, pay_month) DO UPDATE SET
    base_salary = EXCLUDED.base_salary,
    net_pay = EXCLUDED.net_pay;

-- 연차 사용 이력 (Q11, Q12)
INSERT INTO infra.leave_history (user_uuid, leave_type, start_date, end_date, days, reason, status)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, '연차', '2026-01-10', '2026-01-10', 1, '개인 사유', 'APPROVED'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '반차', '2026-01-15', '2026-01-15', 0.5, '병원 방문', 'APPROVED'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '연차', '2026-01-20', '2026-01-21', 2, '가족 행사', 'APPROVED');

-- 복지 포인트 잔액 (Q14)
INSERT INTO infra.welfare_point (user_uuid, total_granted, total_used, remaining, year)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, 1200000, 450000, 750000, 2026)
ON CONFLICT (user_uuid, year) DO UPDATE SET
    total_granted = EXCLUDED.total_granted,
    total_used = EXCLUDED.total_used,
    remaining = EXCLUDED.remaining;

-- 복지 포인트 사용 내역 (Q15)
INSERT INTO infra.welfare_point_usage (user_uuid, category, merchant, amount, usage_date, description)
VALUES
    ('00000000-0000-0000-0000-000000000001'::uuid, '건강/의료', '강남세브란스병원', 150000, '2026-01-05', '건강검진'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '자기계발', '클래스101', 100000, '2026-01-03', '온라인 강의 수강'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '여가/문화', 'CGV', 50000, '2026-01-06', '영화 관람'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '도서/교육', '교보문고', 80000, '2026-01-02', '도서 구매'),
    ('00000000-0000-0000-0000-000000000001'::uuid, '스포츠/레저', '스포애니', 70000, '2026-01-04', '헬스장 이용권');
