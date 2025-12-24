package com.ctrlf.chat.dto.request;

import java.util.UUID;

/**
 * 메시지 전송 요청 DTO. (섹션 삭제 반영)
 */
public record ChatMessageSendRequest(
    /** 대상 세션 ID */
    UUID sessionId,
    /** 메시지 내용 */
    String content
) {}
