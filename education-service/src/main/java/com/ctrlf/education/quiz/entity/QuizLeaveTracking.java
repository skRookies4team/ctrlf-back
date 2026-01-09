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

/**
 * 퀴즈 이탈(탭/창 전환) 추적 엔티티.
 * - 시도 도중의 이탈 횟수/누적 시간/마지막 시각을 기록합니다.
 */
@Entity
@Table(name = "quiz_leave_tracking", schema = "education")
@Getter
@Setter
@NoArgsConstructor
public class QuizLeaveTracking {

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

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}

