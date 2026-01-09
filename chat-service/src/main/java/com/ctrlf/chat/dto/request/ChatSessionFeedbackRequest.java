package com.ctrlf.chat.dto.request;

/**
 * 세션 종료/총평 요청 DTO.
 */
public record ChatSessionFeedbackRequest(
    /** 만족도(예: 1~5) */
    Integer score,
    /** 총평 코멘트 */
    String comment
) {}
