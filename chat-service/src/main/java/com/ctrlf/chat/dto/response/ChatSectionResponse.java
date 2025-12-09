package com.ctrlf.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ChatSectionResponse(
    UUID id,
    String title,
    String summary,
    Integer retryCount,
    Instant createdAt,
    Instant closedAt
) {
}
