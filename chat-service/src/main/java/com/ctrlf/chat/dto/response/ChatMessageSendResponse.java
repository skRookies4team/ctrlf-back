package com.ctrlf.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * 메시지 전송 응답 DTO.
 */
public record ChatMessageSendResponse(
    /** 생성된(보통 assistant) 메시지 ID */
    UUID messageId,
    /** 메시지 역할(user/assistant) */
    String role,
    /** 메시지 내용 */
    String content,
    /** 생성 시각 */
    Instant createdAt
) {}
