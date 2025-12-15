package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageCursorResponse;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;
import java.util.UUID;

public interface ChatMessageService {

    // ✅ 메시지 전송
    ChatMessageSendResponse sendMessage(
        ChatMessageSendRequest request,
        UUID userId,
        String domain
    );

    // ✅ ✅ ✅ 세션 내 메시지 조회(커서 기반)
    ChatMessageCursorResponse getMessagesBySession(UUID sessionId, String cursor, int size);

    // ✅ Retry (messageId 기준)
    ChatMessage retryMessage(UUID sessionId, UUID messageId);

    // ✅ Regen (messageId 기준)
    ChatMessage regenMessage(UUID sessionId, UUID messageId);
}
