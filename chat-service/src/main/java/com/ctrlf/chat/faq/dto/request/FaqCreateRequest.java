package com.ctrlf.chat.faq.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

/**
 * FAQ 생성 요청 DTO.
 */
@Getter
public class FaqCreateRequest {

    /** 질문 (최대 500자 권장) */
    @NotBlank
    @Size(max = 500)
    private String question;

    /** 답변 (최대 5000자 권장) */
    @NotBlank
    @Size(max = 5000)
    private String answer;

    /** 도메인 (예: SECURITY / POLICY / EDUCATION 등) */
    @NotBlank
    @Size(max = 50)
    private String domain;

    /**
     * 우선순위 (값이 작을수록 상위 노출)
     * - 예: 1~5
     */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer priority;
}
