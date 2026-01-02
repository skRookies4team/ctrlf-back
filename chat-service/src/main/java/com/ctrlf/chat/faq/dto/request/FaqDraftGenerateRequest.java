package com.ctrlf.chat.faq.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;

/**
 * FAQ 초안 생성 요청 DTO
 */
@Getter
public class FaqDraftGenerateRequest {

    /** 도메인 (예: SEC_POLICY, PII_PRIVACY) */
    @NotBlank
    private String domain;

    /** FAQ 후보 클러스터 ID */
    @NotBlank
    private String clusterId;

    /** 대표 질문 */
    @NotBlank
    private String canonicalQuestion;

    /** 실제 직원 질문 예시들 (선택) */
    private List<String> sampleQuestions;

    /** RAG에서 뽑아온 후보 문서들 (선택) */
    private List<TopDoc> topDocs;

    /** 평균 의도 신뢰도 (선택, 최소 0.7 필요) */
    private Double avgIntentConfidence;

    /**
     * RAG 검색 결과 문서 정보
     */
    @Getter
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TopDoc {
        private String docId;
        private String docVersion;
        private String title;
        private String snippet;
        private String articleLabel;
        private String articlePath;
        private Double score;
        private Integer page;
        private String dataset;
        private String source;
    }
}

