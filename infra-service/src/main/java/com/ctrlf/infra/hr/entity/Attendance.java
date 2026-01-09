package com.ctrlf.infra.hr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 근태 기록 엔티티 (Q10)
 */
@Entity
@Table(name = "attendance", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    /** 근무일 */
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    /** 출근 시간 */
    @Column(name = "check_in")
    private LocalTime checkIn;

    /** 퇴근 시간 */
    @Column(name = "check_out")
    private LocalTime checkOut;

    /** 실제 근무 시간 (시간 단위) */
    @Column(name = "work_hours", precision = 4, scale = 2)
    private BigDecimal workHours;

    /** 근무 상태 (NORMAL, LATE, EARLY_LEAVE, ABSENT) */
    @Column(name = "status", length = 20)
    private String status;

    /** 근무 유형 (OFFICE, REMOTE, HALF_DAY) */
    @Column(name = "work_type", length = 20)
    private String workType;

    /** 초과 근무 시간 */
    @Column(name = "overtime_hours", precision = 4, scale = 2)
    private BigDecimal overtimeHours;

    /** 비고 */
    @Column(name = "note", length = 200)
    private String note;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}
