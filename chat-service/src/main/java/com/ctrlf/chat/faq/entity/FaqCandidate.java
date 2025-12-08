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
@Table(name = "faq_candidate", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class FaqCandidate {

    /** FAQ 후보 질문 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 원본 질문(사용자 입력) */
    @Column(name = "question", columnDefinition = "text")
    private String question;

    /** 도메인 분류(LLM 분류 결과 등) */
    @Column(name = "domain")
    private String domain;

    /** 등장 빈도 */
    @Column(name = "frequency")
    private Integer frequency;

    /** 추천 점수(double) */
    @Column(name = "score")
    private Double score;

    /** 비활성 여부 */
    @Column(name = "is_disabled")
    private Boolean isDisabled;

    /** 수집 시각 */
    @Column(name = "created_at")
    private Instant createdAt;
}

