package com.ctrlf.chat.dto.request;

/**
 * 메시지 피드백 요청 DTO.
 */
public record ChatFeedbackRequest(
    /** 평점(예: 1~5) */
    Integer score,
    /** 선택 코멘트 */
    String comment
) {}
