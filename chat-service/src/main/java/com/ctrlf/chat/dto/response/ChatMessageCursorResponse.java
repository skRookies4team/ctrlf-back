package com.ctrlf.chat.dto.response;

import com.ctrlf.chat.entity.ChatMessage;
import java.util.List;

/**
 * 세션 내 메시지 커서 기반 조회 응답 DTO.
 */
public record ChatMessageCursorResponse(
    /** 메시지 목록(클라이언트 표시용: 오래된 -> 최신 순) */
    List<ChatMessage> messages,
    /** 다음 페이지 요청을 위한 커서(없으면 null) */
    String nextCursor,
    /** 다음 페이지 존재 여부 */
    boolean hasNext
) {}
