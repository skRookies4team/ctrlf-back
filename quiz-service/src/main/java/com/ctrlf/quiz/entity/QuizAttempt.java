package com.ctrlf.quiz.entity;

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
@Table(name = "quiz_attempt", schema = "quiz")
@Getter
@Setter
@NoArgsConstructor
public class QuizAttempt {

    /** 퀴즈 시도 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUuid;

    /** 교육 ID */
    @Column(name = "education_id", columnDefinition = "uuid")
    private UUID educationId;

    /** 점수 */
    @Column(name = "score")
    private Integer score;

    /** 합격 여부 */
    @Column(name = "passed")
    private Boolean passed;

    /** 시도 회차 */
    @Column(name = "attempt_no")
    private Integer attemptNo;

    /** 시도 시작 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 시간 제한(초) */
    @Column(name = "time_limit")
    private Integer timeLimit;

    /** 제출 시각 */
    @Column(name = "submitted_at")
    private Instant submittedAt;
}

