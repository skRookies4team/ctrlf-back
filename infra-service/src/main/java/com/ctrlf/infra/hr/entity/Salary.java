package com.ctrlf.infra.hr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 급여 명세 엔티티 (Q13)
 */
@Entity
@Table(name = "salary", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    /** 급여 년도 */
    @Column(name = "pay_year", nullable = false)
    private Integer payYear;

    /** 급여 월 */
    @Column(name = "pay_month", nullable = false)
    private Integer payMonth;

    /** 기본급 */
    @Column(name = "base_salary")
    private Integer baseSalary;

    /** 연장근로수당 */
    @Column(name = "overtime_pay")
    private Integer overtimePay;

    /** 상여금 */
    @Column(name = "bonus")
    private Integer bonus;

    /** 식대 */
    @Column(name = "meal_allowance")
    private Integer mealAllowance;

    /** 교통비 */
    @Column(name = "transport_allowance")
    private Integer transportAllowance;

    /** 총 지급액 */
    @Column(name = "total_earnings")
    private Integer totalEarnings;

    /** 소득세 */
    @Column(name = "income_tax")
    private Integer incomeTax;

    /** 지방소득세 */
    @Column(name = "local_tax")
    private Integer localTax;

    /** 국민연금 */
    @Column(name = "national_pension")
    private Integer nationalPension;

    /** 건강보험 */
    @Column(name = "health_insurance")
    private Integer healthInsurance;

    /** 장기요양보험 */
    @Column(name = "long_term_care")
    private Integer longTermCare;

    /** 고용보험 */
    @Column(name = "employment_insurance")
    private Integer employmentInsurance;

    /** 총 공제액 */
    @Column(name = "total_deductions")
    private Integer totalDeductions;

    /** 실수령액 */
    @Column(name = "net_pay")
    private Integer netPay;

    /** 지급일 */
    @Column(name = "pay_date")
    private java.time.LocalDate payDate;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}
