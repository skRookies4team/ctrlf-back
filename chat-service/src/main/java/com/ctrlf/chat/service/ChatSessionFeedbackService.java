package com.ctrlf.chat.service;

import java.util.UUID;

public interface ChatSessionFeedbackService {

    void submitSessionFeedback(
        UUID sessionId,
        UUID userUuid,
        Integer score,
        String comment
    );
}
