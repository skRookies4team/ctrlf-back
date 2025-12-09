package com.ctrlf.chat.entity;

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
@Table(name = "faq", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class Faq {

    /** FAQ PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 질문 본문 */
    @Column(name = "question", columnDefinition = "text")
    private String question;

    /** 공식 답변 */
    @Column(name = "answer", columnDefinition = "text")
    private String answer;

    /** 도메인 분류(예: HR/보안/직무 등) */
    @Column(name = "domain")
    private String domain;

    /** 활성 여부 */
    @Column(name = "is_active")
    private Boolean isActive;

    /** 노출 우선순위 */
    @Column(name = "priority")
    private Integer priority;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}

