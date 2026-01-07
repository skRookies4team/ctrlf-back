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
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 연차/휴가 사용 이력 엔티티 (Q12)
 */
@Entity
@Table(name = "leave_history", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class LeaveHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    /** 휴가 유형 (연차, 반차, 병가, 경조사 등) */
    @Column(name = "leave_type", length = 20, nullable = false)
    private String leaveType;

    /** 시작일 */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** 종료일 */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** 사용 일수 (0.5=반차, 1=연차 등) */
    @Column(name = "days", precision = 3, scale = 1, nullable = false)
    private BigDecimal days;

    /** 사유 */
    @Column(name = "reason", length = 200)
    private String reason;

    /** 상태 (APPROVED, PENDING, REJECTED) */
    @Column(name = "status", length = 20)
    private String status = "APPROVED";

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}
