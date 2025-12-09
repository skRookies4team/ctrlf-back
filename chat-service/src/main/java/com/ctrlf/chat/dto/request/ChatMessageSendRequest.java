package com.ctrlf.chat.dto.request;

import java.util.UUID;

public record ChatMessageSendRequest(
    UUID sessionId,
    UUID sectionId,
    String content
) {}
