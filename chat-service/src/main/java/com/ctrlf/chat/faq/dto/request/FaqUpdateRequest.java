package com.ctrlf.chat.faq.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

/**
 * FAQ 수정 요청 DTO.
 * - null인 필드는 "수정하지 않음"으로 처리하는 방식(PATCH 유사) 권장
 */
@Getter
public class FaqUpdateRequest {

    /** 질문 (수정 시, 최대 500자 권장) */
    @Size(max = 500)
    private String question;

    /** 답변 (수정 시, 최대 5000자 권장) */
    @Size(max = 5000)
    private String answer;

    /** 도메인 (수정 시) */
    @Size(max = 50)
    private String domain;

    /** 활성 여부 (true: 노출, false: 비노출) */
    private Boolean isActive;

    /** 우선순위 (예: 1~5) */
    @Min(1)
    @Max(5)
    private Integer priority;
}
