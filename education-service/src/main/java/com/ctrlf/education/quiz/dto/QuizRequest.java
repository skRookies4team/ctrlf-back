package com.ctrlf.education.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

public final class QuizRequest {
    private QuizRequest() {}

    // ---------- Submit (POST /quiz/attempt/{attemptId}/submit) ----------
    @Getter
    @NoArgsConstructor
    public static class SubmitRequest {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private List<AnswerItem> answers;
    }

    @Getter
    @NoArgsConstructor
    public static class AnswerItem {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private UUID questionId;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer userSelectedIndex;
    }

    // ---------- Leave (POST /quiz/attempt/{attemptId}/leave) ----------
    @Getter
    @NoArgsConstructor
    public static class LeaveRequest {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-01-01T12:00:00Z")
        private Instant timestamp;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "window_blur")
        private String reason;
    }
}

