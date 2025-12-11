package com.ctrlf.chat.service;

import java.util.UUID;

public interface ChatSessionFeedbackService {

    // ✅ 세션 종료 총평 등록
    void submitSessionFeedback(
        UUID sessionId,
        UUID userUuid,
        Integer score,
        String comment
    );
}
