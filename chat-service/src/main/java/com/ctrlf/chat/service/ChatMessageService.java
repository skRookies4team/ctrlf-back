package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;

import java.util.List;
import java.util.UUID;

public interface ChatMessageService {

    ChatMessageSendResponse sendMessage(ChatMessageSendRequest request, UUID userId, String domain);

    List<ChatMessage> getMessagesBySession(UUID sessionId);

    ChatMessage retryMessage(UUID sessionId);

    ChatMessage regenMessage(UUID sessionId);
}
