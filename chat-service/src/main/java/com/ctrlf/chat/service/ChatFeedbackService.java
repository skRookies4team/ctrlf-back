package com.ctrlf.chat.service;

import java.util.UUID;

public interface ChatFeedbackService {

    // ✅ 메시지 피드백 (섹션 삭제 반영)
    void submitMessageFeedback(
        UUID sessionId,
        UUID messageId,
        UUID userUuid,
        Integer score,
        String comment
    );
}
