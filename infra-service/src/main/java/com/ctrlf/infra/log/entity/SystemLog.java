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
@Table(name = "system_log", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class SystemLog {

    /** 운영 요약 로그 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 서비스명(chat/quiz/rag 등) */
    @Column(name = "service_name", length = 50)
    private String serviceName;

    /** 이벤트 유형 */
    @Column(name = "event_type", length = 50)
    private String eventType;

    /** 로그 레벨(INFO/WARN/ERROR) */
    @Column(name = "level", length = 20)
    private String level;

    /** 요약 메시지 */
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    /** 원본 로그 위치(S3 등) */
    @Column(name = "raw_ref", length = 255)
    private String rawRef;

    /** 트레이스 ID */
    @Column(name = "trace_id", length = 100)
    private String traceId;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;
}

