package com.ctrlf.chat.dto.response;

import com.ctrlf.chat.entity.ChatMessage;
import java.util.List;
import java.util.UUID;

/**
 * 세션 히스토리 응답 DTO.
 * (기존 전체 조회 유지)
 */
public record ChatSessionHistoryResponse(
    UUID sessionId,
    String title,
    List<ChatMessage> messages
) {}
