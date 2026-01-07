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
 * 복지 포인트 잔액 엔티티 (Q14, Q15)
 */
@Entity
@Table(name = "welfare_point", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class WelfarePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", nullable = false, unique = true)
    private UUID userUuid;

    /** 연간 총 지급액 */
    @Column(name = "total_granted", nullable = false)
    private Integer totalGranted = 0;

    /** 연간 총 사용액 */
    @Column(name = "total_used", nullable = false)
    private Integer totalUsed = 0;

    /** 현재 잔액 */
    @Column(name = "remaining", nullable = false)
    private Integer remaining = 0;

    /** 기준 연도 */
    @Column(name = "year", nullable = false)
    private Integer year;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}
