package com.ctrlf.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quiz_question", schema = "quiz")
@Getter
@Setter
@NoArgsConstructor
public class QuizQuestion {

    /** 생성된 퀴즈 문항 PK */
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

    /** 보기(JSON 문자열) */
    @Column(name = "options", columnDefinition = "text")
    private String options;

    /** 정답 보기 인덱스 */
    @Column(name = "correct_option_idx")
    private Integer correctOptionIdx;

    /** 해설 */
    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;

    /** 사용자가 선택한 보기 인덱스 */
    @Column(name = "user_selected_option_idx")
    private Integer userSelectedOptionIdx;
}

