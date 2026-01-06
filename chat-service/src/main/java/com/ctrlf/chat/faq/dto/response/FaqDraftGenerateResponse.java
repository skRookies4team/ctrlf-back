package com.ctrlf.chat.faq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FAQ 초안 생성 응답 DTO
 */
@Getter
@AllArgsConstructor
public class FaqDraftGenerateResponse {

    /** 상태 (SUCCESS 또는 FAILED) */
    private String status;

    /** 생성된 FAQ 초안 (성공 시) */
    private FaqDraftPayload faqDraft;

    /** 에러 메시지 (실패 시) */
    private String errorMessage;

    /**
     * FAQ 초안 페이로드
     */
    @Getter
    @AllArgsConstructor
    public static class FaqDraftPayload {
        private String faqDraftId;
        private String domain;
        private String clusterId;
        private String question;
        private String answerMarkdown;
        private String summary;
        private String sourceDocId;
        private String sourceDocVersion;
        private String sourceArticleLabel;
        private String sourceArticlePath;
        private String answerSource;
        private Double aiConfidence;
        private String createdAt;
    }
}

