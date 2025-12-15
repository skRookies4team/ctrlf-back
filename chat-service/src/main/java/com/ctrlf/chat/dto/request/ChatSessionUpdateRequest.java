package com.ctrlf.chat.dto.request;

/**
 * 세션 수정 요청 DTO.
 */
public record ChatSessionUpdateRequest(
    /** 변경할 세션 제목 */
    String title
) {}
