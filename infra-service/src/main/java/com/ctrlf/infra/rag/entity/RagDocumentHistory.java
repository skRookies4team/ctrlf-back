package com.ctrlf.infra.rag.entity;

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
@Table(name = "rag_document_history", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class RagDocumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사규 문서 ID */
    @Column(name = "document_id", length = 50, nullable = false)
    private String documentId;

    /** 문서 버전 */
    @Column(name = "version", nullable = false)
    private Integer version;

    /** 액션 (예: "CREATED", "UPDATED", "STATUS_CHANGED", "REVIEW_REQUESTED") */
    @Column(name = "action", length = 50, nullable = false)
    private String action;

    /** 실행자 (사용자 이름 또는 UUID) */
    @Column(name = "actor", length = 100)
    private String actor;

    /** 메시지 (선택) */
    @Column(name = "message", columnDefinition = "text")
    private String message;

    /** 생성 시각 */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

