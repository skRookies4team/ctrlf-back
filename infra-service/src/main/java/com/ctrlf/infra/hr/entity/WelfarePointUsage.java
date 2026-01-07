package com.ctrlf.infra.hr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 복지 포인트 사용 내역 엔티티 (Q15)
 */
@Entity
@Table(name = "welfare_point_usage", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class WelfarePointUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    /** 사용 카테고리 (건강/의료, 자기계발, 여가/문화 등) */
    @Column(name = "category", length = 50, nullable = false)
    private String category;

    /** 사용처 */
    @Column(name = "merchant", length = 100)
    private String merchant;

    /** 사용 금액 */
    @Column(name = "amount", nullable = false)
    private Integer amount;

    /** 사용일 */
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    /** 사용 내역 설명 */
    @Column(name = "description", length = 200)
    private String description;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;
}
