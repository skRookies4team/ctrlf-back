package com.ctrlf.chat.faq.dto.response;

import com.ctrlf.chat.faq.entity.FaqCandidate;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class FaqCandidateResponse {

    private UUID id;
    private String question;
    private String domain;
    private Integer frequency;
    private Double score;
    private Boolean isDisabled;
    private Instant createdAt;

    public static FaqCandidateResponse from(FaqCandidate c) {
        return FaqCandidateResponse.builder()
            .id(c.getId())
            .question(c.getQuestion())
            .domain(c.getDomain())
            .frequency(c.getFrequency())
            .score(c.getScore())
            .isDisabled(c.getIsDisabled())
            .createdAt(c.getCreatedAt())
            .build();
    }
}
