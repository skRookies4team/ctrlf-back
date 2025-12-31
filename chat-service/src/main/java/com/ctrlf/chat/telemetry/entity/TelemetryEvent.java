package com.ctrlf.chat.telemetry.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Telemetry 이벤트 엔티티
 * 
 * <p>AI에서 전송된 구조화된 이벤트 로그를 저장하는 엔티티입니다.</p>
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
@Entity
@Table(name = "telemetry_event", schema = "telemetry", indexes = {
    @Index(name = "idx_telemetry_event_occurred_at", columnList = "occurred_at"),
    @Index(name = "idx_telemetry_event_dept_occurred", columnList = "dept_id,occurred_at"),
    @Index(name = "idx_telemetry_event_type_occurred", columnList = "event_type,occurred_at"),
    @Index(name = "idx_telemetry_event_conversation_turn", columnList = "conversation_id,turn_id")
})
@Getter
@Setter
@NoArgsConstructor
public class TelemetryEvent {

    /** 이벤트 ID (PK, Idempotency key) */
    @Id
    @Column(name = "event_id", columnDefinition = "uuid", nullable = false)
    private UUID eventId;

    /** 이벤트 소스 (예: "ai-gateway") */
    @Column(name = "source", nullable = false, length = 50)
    private String source;

    /** AI가 전송한 시각 */
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    /** 이벤트 타입 (CHAT_TURN, FEEDBACK, SECURITY) */
    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    /** Trace ID (X-Trace-Id) */
    @Column(name = "trace_id", nullable = false, columnDefinition = "uuid")
    private UUID traceId;

    /** Conversation ID (X-Conversation-Id) */
    @Column(name = "conversation_id", length = 100)
    private String conversationId;

    /** Turn ID (X-Turn-Id) */
    @Column(name = "turn_id")
    private Integer turnId;

    /** User ID (X-User-Id) */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /** Dept ID (X-Dept-Id) */
    @Column(name = "dept_id", nullable = false, length = 64)
    private String deptId;

    /** 이벤트 발생 시각 (occurredAt) */
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** 이벤트 타입별 payload (JSONB) */
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Object payload;

    /** 백엔드 수신 시각 */
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    /**
     * 엔티티 저장 전 실행되는 콜백
     * 수신 시각을 현재 시간으로 설정합니다.
     */
    @PrePersist
    void onCreate() {
        if (this.receivedAt == null) {
            this.receivedAt = Instant.now();
        }
    }
}

