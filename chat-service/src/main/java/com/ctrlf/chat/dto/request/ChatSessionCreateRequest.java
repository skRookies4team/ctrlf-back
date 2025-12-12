package com.ctrlf.chat.dto.request;

import java.util.UUID;

public record ChatSessionCreateRequest(
    UUID userUuid,
    String title,
    String domain
) {
}
