package com.ctrlf.chat.faq.dto.response;

import com.ctrlf.chat.faq.entity.FaqCandidate;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * FAQ 후보 응답 DTO.
 */
@Getter
@Builder
public class FaqCandidateResponse {

    /** 후보 ID */
    private UUID id;

    /** 후보 질문 */
    private String question;

    /** 도메인 */
    private String domain;

    /** 누적 빈도(집계) */
    private Integer frequency;

    /** 후보 점수(예: 유사도/랭킹 점수) */
    private Double score;

    /** 비활성/제외 여부 */
    private Boolean isDisabled;

    /** 생성 시각 */
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
