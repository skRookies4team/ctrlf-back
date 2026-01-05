package com.ctrlf.infra.ailog.entity;

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
 * AI 로그 엔티티
 * 
 * <p>AI에서 정제된 로그를 저장합니다.</p>
 * <p>관리자 대시보드 로그 조회에 사용됩니다.</p>
 */
@Entity
@Table(name = "ai_log", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class AiLog {

    /** AI 로그 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 생성 시각 (AI에서 발생한 시각) */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** 사용자 ID */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /** 사용자 역할 (EMPLOYEE, ADMIN 등) */
    @Column(name = "user_role", length = 50)
    private String userRole;

    /** 부서명 */
    @Column(name = "department", length = 100)
    private String department;

    /** 도메인 ID */
    @Column(name = "domain", length = 50)
    private String domain;

    /** 라우트 ID (RAG, LLM, INCIDENT, FAQ 등) */
    @Column(name = "route", length = 50)
    private String route;

    /** 모델명 */
    @Column(name = "model_name", length = 100)
    private String modelName;

    /** PII 입력 감지 여부 */
    @Column(name = "has_pii_input")
    private Boolean hasPiiInput;

    /** PII 출력 감지 여부 */
    @Column(name = "has_pii_output")
    private Boolean hasPiiOutput;

    /** RAG 사용 여부 */
    @Column(name = "rag_used")
    private Boolean ragUsed;

    /** RAG 소스 개수 */
    @Column(name = "rag_source_count")
    private Integer ragSourceCount;

    /** 총 지연시간 (밀리초) */
    @Column(name = "latency_ms_total")
    private Long latencyMsTotal;

    /** 에러 코드 (에러 발생 시) */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    /** 트레이스 ID */
    @Column(name = "trace_id", length = 200)
    private String traceId;

    /** 대화 ID */
    @Column(name = "conversation_id", length = 100)
    private String conversationId;

    /** 턴 ID */
    @Column(name = "turn_id")
    private Integer turnId;

    /** 백엔드 수신 시각 */
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}

