package com.ctrlf.chat.dto.request;

public record ChatSessionFeedbackRequest(
    Integer score,   // 1~5
    String comment
) {
}
