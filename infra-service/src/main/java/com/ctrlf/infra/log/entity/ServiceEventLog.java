package com.ctrlf.infra.entity;

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

@Entity
@Table(name = "service_event_log", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class ServiceEventLog {

    /** 비즈니스 이벤트 로그 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 서비스명 */
    @Column(name = "service_name", length = 50)
    private String serviceName;

    /** 엔티티 종류 */
    @Column(name = "entity_type", length = 50)
    private String entityType;

    /** 엔티티 PK */
    @Column(name = "entity_id", columnDefinition = "uuid")
    private UUID entityId;

    /** 이벤트 유형 */
    @Column(name = "event_type", length = 50)
    private String eventType;

    /** 변경 전 값 */
    @Column(name = "old_value", length = 255)
    private String oldValue;

    /** 변경 후 값 */
    @Column(name = "new_value", length = 255)
    private String newValue;

    /** 수행자 식별자(UUID 등) */
    @Column(name = "changed_by", length = 50)
    private String changedBy;

    /** 트레이스 ID */
    @Column(name = "trace_id", length = 100)
    private String traceId;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;
}

