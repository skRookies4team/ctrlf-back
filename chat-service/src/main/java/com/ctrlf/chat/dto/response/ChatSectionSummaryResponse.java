package com.ctrlf.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ChatSectionSummaryResponse(
    UUID sessionId,
    UUID sectionId,
    String summary,
    Instant createdAt
) {}
