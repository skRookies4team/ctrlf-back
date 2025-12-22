package com.ctrlf.education.quiz.entity;

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
import org.hibernate.annotations.CreationTimestamp;

/**
 * 교육 퀴즈 시도 엔티티.
 * - 사용자별 교육 퀴즈 시도와 점수/합격여부/시간 제한 등을 기록합니다.
 */
@Entity
@Table(name = "quiz_attempt", schema = "education")
@Getter
@Setter
@NoArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 응시자 사용자 UUID */
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUuid;

    /** 대상 교육 ID */
    @Column(name = "education_id", columnDefinition = "uuid")
    private UUID educationId;

    /** 최종 점수 */
    @Column(name = "score")
    private Integer score;

    /** 합격 여부 */
    @Column(name = "passed")
    private Boolean passed;

    /** 시도 회차(1부터 증가) */
    @Column(name = "attempt_no")
    private Integer attemptNo;

    /** 시작 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 시간 제한(초) */
    @Column(name = "time_limit")
    private Integer timeLimit;

    /** 제출 시각 */
    @Column(name = "submitted_at")
    private Instant submittedAt;

    /** 응시자 부서 (제출 시 JWT에서 추출하여 저장) */
    @Column(name = "department")
    private String department;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}

