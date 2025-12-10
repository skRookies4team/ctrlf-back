package com.ctrlf.chat.faq.dto.response;

import com.ctrlf.chat.faq.entity.Faq;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class FaqResponse {

    private UUID id;
    private String question;
    private String answer;
    private String domain;
    private Boolean isActive;
    private Integer priority;
    private Instant createdAt;
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
