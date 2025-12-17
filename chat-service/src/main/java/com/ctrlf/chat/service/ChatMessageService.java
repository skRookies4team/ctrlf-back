package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageCursorResponse;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;
import java.util.UUID;

public interface ChatMessageService {

    ChatMessageSendResponse sendMessage(
        ChatMessageSendRequest request,
        UUID userId,
        String domain
    );

    ChatMessageCursorResponse getMessagesBySession(
        UUID sessionId,
        String cursor,
        int size
    );

    ChatMessage retryMessage(UUID sessionId, UUID messageId);

    ChatMessage regenMessage(UUID sessionId, UUID messageId);
}
