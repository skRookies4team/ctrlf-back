package com.ctrlf.education.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

public final class QuizResponse {
    private QuizResponse() {}

    // ---------- Start (GET /quiz/{eduId}/start) ----------
    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StartResponse {
        private UUID attemptId;
        private List<QuestionItem> questions;
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QuestionItem {
        private UUID questionId;
        private String question;
        private List<String> choices;
        private Integer answerIndex; // null until submitted
    }

    // ---------- Submit (POST /quiz/attempt/{attemptId}/submit) ----------
    @Getter
    @AllArgsConstructor
    public static class SubmitResponse {
        private int score;
        private boolean passed;
        private int correctCount;
        private int wrongCount;
        private int totalCount;
        private Instant submittedAt;
    }

    // ---------- Result (GET /quiz/attempt/{attemptId}/result) ----------
    @Getter
    @AllArgsConstructor
    public static class ResultResponse {
        private int score;
        private boolean passed;
        private int correctCount;
        private int wrongCount;
        private int totalCount;
        private Instant finishedAt;
    }

    // ---------- Wrongs (GET /quiz/{attemptId}/wrongs) ----------
    @Getter
    @AllArgsConstructor
    public static class WrongNoteItem {
        private String question;
        private Integer userAnswerIndex;
        private Integer correctAnswerIndex;
        private String explanation;
        private List<String> choices;
    }

    // ---------- Leave (POST /quiz/attempt/{attemptId}/leave) ----------
    @Getter
    @AllArgsConstructor
    public static class LeaveResponse {
        private boolean recorded;
        private int leaveCount;
        private Instant lastLeaveAt;
    }
}

