package com.ctrlf.infra.personalization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Personalization 관련 요청/응답 DTO 모음.
 * AI Gateway에서 개인화 데이터를 요청하는 API용 DTO입니다.
 */
public final class PersonalizationDtos {
    private PersonalizationDtos() {}

    // ---------- Resolve Request ----------
    /**
     * 개인화 facts 조회 요청.
     */
    @Getter
    @NoArgsConstructor
    public static class ResolveRequest {
        @NotBlank
        @Schema(example = "Q11", description = "인텐트 ID (Q1-Q20)")
        private String sub_intent_id;

        @Schema(example = "this-year", description = "기간 유형 (this-week, this-month, 3m, this-year)")
        private String period;

        @Schema(example = "D001", description = "부서 비교 대상 ID (Q5에서만 사용)", nullable = true)
        private String target_dept_id;

        @Schema(example = "PERSONAL_INFO_PROTECTION",
            description = "교육 토픽 (Q2, Q7, Q8, Q18, Q19에서 사용). " +
                "WORKPLACE_BULLYING(직장내괴롭힘), SEXUAL_HARASSMENT_PREVENTION(성희롱예방), " +
                "PERSONAL_INFO_PROTECTION(개인정보보호), DISABILITY_AWARENESS(장애인인식개선), JOB_DUTY(직무교육)",
            nullable = true)
        private String topic;
    }

    // ---------- Resolve Response ----------
    /**
     * 개인화 facts 조회 응답 (공통 구조).
     */
    @Getter
    @AllArgsConstructor
    public static class ResolveResponse {
        private String sub_intent_id;
        private String period_start;
        private String period_end;
        private String updated_at;
        private Map<String, Object> metrics;
        private List<Object> items;
        private Map<String, Object> extra;
        private ErrorInfo error;
    }

    /**
     * 에러 정보.
     */
    @Getter
    @AllArgsConstructor
    public static class ErrorInfo {
        private String type;
        private String message;
    }

    // ---------- Q1: 미이수 필수 교육 조회 ----------
    @Getter
    @AllArgsConstructor
    public static class Q1EducationItem {
        private String education_id;
        private String title;
        private String deadline;
        private String status;
    }

    @Getter
    @AllArgsConstructor
    public static class Q1Metrics {
        private int total_required;
        private int completed;
        private int remaining;
    }

    // ---------- Q3: 이번 달 데드라인 필수 교육 ----------
    @Getter
    @AllArgsConstructor
    public static class Q3EducationItem {
        private String education_id;
        private String title;
        private String deadline;
        private int days_left;
    }

    @Getter
    @AllArgsConstructor
    public static class Q3Metrics {
        private int deadline_count;
    }

    // ---------- Q5: 내 평균 vs 부서/전사 평균 ----------
    @Getter
    @AllArgsConstructor
    public static class Q5Metrics {
        private double my_average;
        private double dept_average;
        private double company_average;
    }

    @Getter
    @AllArgsConstructor
    public static class Q5Extra {
        private String target_dept_id;
        private String target_dept_name;
    }

    @Getter
    @AllArgsConstructor
    public static class Q5Item {
        private int rank;
        private String topic;
        private double wrong_rate;
    }

    // ---------- Q6: 가장 많이 틀린 보안 토픽 TOP3 ----------
    @Getter
    @AllArgsConstructor
    public static class Q6TopicItem {
        private int rank;
        private String topic;
        private double wrong_rate;
    }

    // ---------- Q9: 이번 주 교육/퀴즈 할 일 ----------
    @Getter
    @AllArgsConstructor
    public static class Q9TodoItem {
        private String type; // "education" | "quiz"
        private String title;
        private String deadline;
    }

    @Getter
    @AllArgsConstructor
    public static class Q9Metrics {
        private int todo_count;
    }

    // ---------- Q11: 남은 연차 일수 ----------
    @Getter
    @AllArgsConstructor
    public static class Q11Metrics {
        private int total_days;
        private int used_days;
        private int remaining_days;
    }

    // ---------- Q14: 복지/식대 포인트 잔액 ----------
    @Getter
    @AllArgsConstructor
    public static class Q14Metrics {
        private int welfare_points;
        private int meal_allowance;
    }

    // ---------- Q12: 연차 사용 이력 ----------
    @Getter
    @AllArgsConstructor
    public static class Q12LeaveItem {
        private String leave_type;   // 연차, 반차, 병가 등
        private String start_date;   // YYYY-MM-DD
        private String end_date;     // YYYY-MM-DD
        private double days;         // 사용 일수
        private String reason;       // 사유
    }

    @Getter
    @AllArgsConstructor
    public static class Q12Metrics {
        private int total_days;      // 총 연차
        private double used_days;    // 사용 연차
        private double remaining_days; // 남은 연차
        private int usage_count;     // 사용 건수
    }

    // ---------- Q15: 복지 포인트 사용 내역 ----------
    @Getter
    @AllArgsConstructor
    public static class Q15UsageItem {
        private String category;     // 카테고리
        private String merchant;     // 사용처
        private int amount;          // 금액
        private String date;         // YYYY-MM-DD
        private String description;  // 설명
    }

    @Getter
    @AllArgsConstructor
    public static class Q15Metrics {
        private int total_granted;   // 총 지급액
        private int total_used;      // 총 사용액
        private int remaining;       // 잔액
        private int usage_count;     // 사용 건수
    }

    // ---------- Q20: 올해 HR 할 일 (미완료) ----------
    @Getter
    @AllArgsConstructor
    public static class Q20TodoItem {
        private String type; // "education" | "document" | "survey" | "review"
        private String title;
        private String status; // 선택
        private String deadline; // 선택
    }

    @Getter
    @AllArgsConstructor
    public static class Q20Metrics {
        private int todo_count;
    }

    // ---------- Q4: 교육 이어보기 (마지막 시청 영상) ----------
    @Getter
    @AllArgsConstructor
    public static class Q4VideoItem {
        private String education_id;
        private String video_id;
        private String education_title;
        private String video_title;
        private Integer resume_position_seconds;
        private Integer progress_percent;
        private Integer duration;
    }

    @Getter
    @AllArgsConstructor
    public static class Q4Metrics {
        private int progress_percent;
        private int total_watch_seconds;
    }

    // ---------- Q10: 근태 현황 조회 ----------
    @Getter
    @AllArgsConstructor
    public static class Q10AttendanceItem {
        private String date;           // YYYY-MM-DD
        private String day_of_week;    // 월, 화, 수...
        private String check_in;       // HH:mm
        private String check_out;      // HH:mm
        private double work_hours;     // 근무 시간
        private String status;         // NORMAL, LATE, EARLY_LEAVE, ABSENT
        private String work_type;      // OFFICE, REMOTE, HALF_DAY
    }

    @Getter
    @AllArgsConstructor
    public static class Q10Metrics {
        private int work_days;         // 이번 달 근무일수
        private int actual_work_days;  // 실제 출근일
        private int late_count;        // 지각 횟수
        private int early_leave_count; // 조퇴 횟수
        private int absent_count;      // 결근 횟수
        private int remote_days;       // 재택근무 일수
        private double overtime_hours; // 초과근무 시간
    }

    // ---------- Q13: 급여 명세서 요약 ----------
    @Getter
    @AllArgsConstructor
    public static class Q13SalaryItem {
        private String category;       // 지급 또는 공제
        private String item;           // 항목명
        private int amount;            // 금액
    }

    @Getter
    @AllArgsConstructor
    public static class Q13Metrics {
        private String pay_month;      // YYYY-MM
        private int base_salary;       // 기본급
        private int overtime_pay;      // 연장근로수당
        private int bonus;             // 상여금
        private int meal_allowance;    // 식대
        private int transport_allowance; // 교통비
        private int total_earnings;    // 총 지급액
        private int income_tax;        // 소득세
        private int local_tax;         // 지방소득세
        private int national_pension;  // 국민연금
        private int health_insurance;  // 건강보험
        private int long_term_care;    // 장기요양보험
        private int employment_insurance; // 고용보험
        private int total_deductions;  // 총 공제액
        private int net_pay;           // 실수령액
    }

    // ---------- Q16: 내 인사 정보 조회 ----------
    @Getter
    @AllArgsConstructor
    public static class Q16EmployeeItem {
        private String label;          // 항목명
        private String value;          // 값
    }

    @Getter
    @AllArgsConstructor
    public static class Q16Metrics {
        private String employee_id;    // 사원번호
        private String name;           // 이름
        private String department;     // 부서명
        private String position;       // 직급
        private String job_title;      // 직책
        private String hire_date;      // 입사일
        private int years_of_service;  // 근속 년수
        private int months_of_service; // 근속 개월수
        private String email;          // 이메일
        private String phone;          // 연락처
        private String office_phone;   // 사내전화
    }

    // ---------- Q17: 팀/부서 정보 조회 ----------
    @Getter
    @AllArgsConstructor
    public static class Q17TeamMemberItem {
        private String employee_id;    // 사원번호
        private String name;           // 이름
        private String position;       // 직급
        private String job_title;      // 직책
        private boolean is_leader;     // 팀장 여부
    }

    @Getter
    @AllArgsConstructor
    public static class Q17Metrics {
        private String department_name;    // 부서명
        private String department_code;    // 부서 코드
        private String team_lead;          // 팀장명
        private String team_lead_position; // 팀장 직급
        private int total_members;         // 총 인원
        private int full_time;             // 정규직
        private int contract;              // 계약직 (임시)
        private String parent_department;  // 상위 부서명
    }
}

