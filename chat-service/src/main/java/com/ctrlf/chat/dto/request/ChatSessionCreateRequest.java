package com.ctrlf.chat.dto.request;

import java.util.UUID;

/**
 * 세션 생성 요청 DTO.
 */
public record ChatSessionCreateRequest(
    /** 세션 생성 사용자 UUID */
    UUID userUuid,
    /** 세션 제목 */
    String title,
    /** 업무 도메인 */
    String domain
) {}
