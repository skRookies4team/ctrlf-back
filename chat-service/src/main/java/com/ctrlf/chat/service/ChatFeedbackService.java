package com.ctrlf.chat.service;

import java.util.UUID;

public interface ChatFeedbackService {

    // ✅ 메시지 피드백 (sectionId 제거)
    void submitMessageFeedback(
        UUID sessionId,
        UUID messageId,
        UUID userUuid,
        Integer score,
        String comment
    );

    // ✅ 세션 종료 총평
    void submitSessionFeedback(
        UUID sessionId,
        UUID userUuid,
        Integer score,
        String comment
    );
}
