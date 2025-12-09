package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatMessageService {

    // ✅ 메시지 전송
    ChatMessageSendResponse sendMessage(
        ChatMessageSendRequest request,
        UUID userId,
        String domain
    );

    // ✅ 메시지 조회
    List<ChatMessage> getMessages(UUID sessionId, UUID sectionId);

    // ✅ Retry
    ChatMessage retryMessage(UUID sessionId, UUID sectionId);
}
