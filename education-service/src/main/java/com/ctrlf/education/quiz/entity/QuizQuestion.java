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
 * 교육 퀴즈 문항 엔티티.
 * - 시도(QuizAttempt)에 종속된 문항과 보기/정답/선택값을 보관합니다.
 */
@Entity
@Table(name = "quiz_question", schema = "education")
@Getter
@Setter
@NoArgsConstructor
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 소속 시도 ID */
    @Column(name = "attempt_id", columnDefinition = "uuid")
    private UUID attemptId;

    /** 문제 텍스트 */
    @Column(name = "question", columnDefinition = "text")
    private String question;

    /** 보기(JSON 직렬화 문자열) */
    @Column(name = "options", columnDefinition = "text")
    private String options;

    /** 정답 보기 인덱스(0-based) */
    @Column(name = "correct_option_idx")
    private Integer correctOptionIdx;

    /** 해설 텍스트 */
    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;

    /** 사용자 선택 보기 인덱스(0-based) */
    @Column(name = "user_selected_option_idx")
    private Integer userSelectedOptionIdx;

    /** 문항 순서(0-based) */
    @Column(name = "question_order")
    private Integer questionOrder;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}

