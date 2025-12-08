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
@Table(name = "error_event_log", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class ErrorEventLog {

    /** 오류 로그 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 서비스명 */
    @Column(name = "service_name", length = 50)
    private String serviceName;

    /** 오류 타입(예: LLM_ERROR 등) */
    @Column(name = "error_type", length = 50)
    private String errorType;

    /** 요약된 에러 메시지 */
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    /** 스택트레이스 저장 위치 참조(S3 링크 등) */
    @Column(name = "stack_ref", length = 255)
    private String stackRef;

    /** 트레이스 ID */
    @Column(name = "trace_id", length = 100)
    private String traceId;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;
}

