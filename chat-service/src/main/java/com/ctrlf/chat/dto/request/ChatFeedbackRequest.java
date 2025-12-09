package com.ctrlf.chat.dto.request;

public record ChatFeedbackRequest(
    Boolean isHelpful,
    String comment
) {}
