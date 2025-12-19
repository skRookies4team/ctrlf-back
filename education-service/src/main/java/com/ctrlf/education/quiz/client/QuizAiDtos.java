package com.ctrlf.education.quiz.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class QuizAiDtos {
    private QuizAiDtos() {}

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenerateRequest {
        private String educationId;   // string for flexibility (UUID or code)
        private Integer attemptNo;
        private String language;      // "ko"
        private Integer numQuestions; // 5~10
        private String questionType;  // "MCQ_SINGLE"
        // Optional: candidate blocks, difficulty distribution 등은 필요 시 추가
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GenerateResponse {
        private String educationId;
        private Integer attemptNo;
        private Integer generatedCount;
        private List<AiQuestion> questions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AiQuestion {
        private String questionType; // MCQ_SINGLE
        private String stem;         // 문제 본문
        private List<AiOption> options;
        private String explanation;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AiOption {
        private String optionId;
        private String text;
        private Boolean isCorrect;
    }
}

