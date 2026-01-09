package com.ctrlf.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * 세션 응답 DTO.
 */
public record ChatSessionResponse(
    UUID id,
    String title,
    String domain,
    UUID userUuid,
    Instant createdAt,
    Instant updatedAt
) {}
