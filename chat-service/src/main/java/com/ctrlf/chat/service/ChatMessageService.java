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

    // ✅ 특정 섹션 메시지 조회
    List<ChatMessage> getMessages(UUID sessionId, UUID sectionId);

    // ✅ ✅ ✅ 세션 전체 메시지 조회
    List<ChatMessage> getMessagesBySession(UUID sessionId);

    // ✅ Retry
    ChatMessage retryMessage(UUID sessionId, UUID sectionId);

    // ✅ Regen
    ChatMessage regenMessage(UUID sessionId, UUID sectionId);
}
