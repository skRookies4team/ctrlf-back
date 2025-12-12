package com.ctrlf.chat.dto.request;

public record ChatFeedbackRequest(
    Integer score,
    String comment
) {
}
