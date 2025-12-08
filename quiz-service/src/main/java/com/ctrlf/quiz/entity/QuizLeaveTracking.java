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
@Table(name = "quiz_leave_tracking", schema = "quiz")
@Getter
@Setter
@NoArgsConstructor
public class QuizLeaveTracking {

    /** 퀴즈 이탈 로그 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 소속 시도 ID */
    @Column(name = "attempt_id", columnDefinition = "uuid")
    private UUID attemptId;

    /** 이탈 횟수 */
    @Column(name = "leave_count")
    private Integer leaveCount;

    /** 이탈 누적 시간(초) */
    @Column(name = "total_leave_seconds")
    private Integer totalLeaveSeconds;

    /** 마지막 이탈 시각 */
    @Column(name = "last_leave_at")
    private Instant lastLeaveAt;
}

