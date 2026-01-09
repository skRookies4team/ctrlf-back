package com.ctrlf.chat.faq.dto.response;

import com.ctrlf.chat.faq.entity.Faq;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * FAQ 응답 DTO.
 */
@Getter
@Builder
public class FaqResponse {

    /** FAQ ID */
    private UUID id;

    /** 질문 */
    private String question;

    /** 답변 */
    private String answer;

    /** 도메인 */
    private String domain;

    /** 활성 여부 */
    private Boolean isActive;

    /** 우선순위 (작을수록 상위) */
    private Integer priority;

    /** 생성 시각 */
    private Instant createdAt;

    /** 수정 시각 */
    private Instant updatedAt;

    public static FaqResponse from(Faq faq) {
        return FaqResponse.builder()
            .id(faq.getId())
            .question(faq.getQuestion())
            .answer(faq.getAnswer())
            .domain(faq.getDomain())
            .isActive(faq.getIsActive())
            .priority(faq.getPriority())
            .createdAt(faq.getCreatedAt())
            .updatedAt(faq.getUpdatedAt())
            .build();
    }
}
