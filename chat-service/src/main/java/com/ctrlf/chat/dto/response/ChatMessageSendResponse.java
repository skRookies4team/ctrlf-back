package com.ctrlf.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageSendResponse(
    UUID messageId,
    String role,
    String content,
    Instant createdAt
) {}
