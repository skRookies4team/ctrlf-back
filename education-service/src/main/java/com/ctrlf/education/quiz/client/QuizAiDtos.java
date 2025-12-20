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
        private String language;      // "ko"
        private Integer numQuestions; // 5~10
        private Integer maxOptions;    // 최대 보기 개수 (기본 4)
        private List<QuizCandidateBlock> quizCandidateBlocks; // 퀴즈 후보 블록들
        private List<ExcludePreviousQuestion> excludePreviousQuestions; // 이전 문항 제외 (재응시 시)
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuizCandidateBlock {
        private String blockId;
        private String docId;         // 출처 문서 ID
        private String docVersion;    // 출처 문서 버전
        private String chapterId;
        private String learningObjectiveId;
        private String text;
        private List<String> tags;
        private String articlePath;   // 조항 경로 (예: "제3장 > 제2조 > 제1항")
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExcludePreviousQuestion {
        private String questionId;    // 1차 응시 때의 문항 ID
        private String stem;          // 1차 응시 때의 문제 텍스트
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GenerateResponse {
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

