package com.ctrlf.chat.dto.request;

import java.util.UUID;

public record ChatMessageSendRequest(
    UUID sessionId,
    String content
) {}
