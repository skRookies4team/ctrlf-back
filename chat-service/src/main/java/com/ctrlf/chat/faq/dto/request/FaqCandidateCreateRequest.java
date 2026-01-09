package com.ctrlf.chat.faq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * FAQ 후보(질문) 생성 요청 DTO.
 * - FAQ 후보는 사용자가 자주 묻는 질문을 수집/집계하기 위한 엔티티
 */
@Getter
public class FaqCandidateCreateRequest {

    /** 후보 질문 (최대 500자 권장) */
    @NotBlank
    @Size(max = 500)
    private String question;

    /** 도메인 (예: SECURITY / POLICY / EDUCATION 등) */
    @NotBlank
    @Size(max = 50)
    private String domain;
}
