package com.ctrlf.chat.dto.response;

import com.ctrlf.chat.entity.ChatMessage;

import java.util.List;
import java.util.UUID;

public record ChatSessionHistoryResponse(
    UUID sessionId,
    String title,
    List<ChatMessage> messages
) {
}
