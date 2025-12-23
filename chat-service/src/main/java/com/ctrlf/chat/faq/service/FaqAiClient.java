package com.ctrlf.chat.faq.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FaqAiClient {

    private final RestClient restClient;

    public FaqAiClient(RestClient.Builder builder) {
        this.restClient = builder
            // AI Gateway base-url (필요 시 application.yml로 빼도 됨)
            .baseUrl("http://localhost:8000")
            .build();
    }

    public AiFaqResponse generate(
        String domain,
        String clusterId,
        String canonicalQuestion,
        List<TopDoc> topDocs
    ) {
        return restClient.post()
            .uri("/ai/faq/generate")
            .body(new AiFaqRequest(domain, clusterId, canonicalQuestion, topDocs))
            .retrieve()
            .body(AiFaqResponse.class);
    }

    /* ======================
       Request / Response DTO
       ====================== */

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AiFaqRequest(
        String domain,
        String cluster_id,
        String canonical_question,
        List<TopDoc> top_docs  // RAG 검색 결과 (선택)
    ) {}

    /**
     * RAG 검색 결과 문서 정보
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TopDoc(
        String doc_id,      // 문서 ID
        String title,       // 문서 제목
        String snippet,     // 검색된 텍스트 스니펫
        Double score,       // 유사도 점수
        Integer page,       // 페이지 번호 (선택)
        String dataset,    // 데이터셋 (선택)
        String source       // 출처 (선택)
    ) {}

    public record AiFaqResponse(
        String status,
        FaqDraftPayload faq_draft,
        String error_message
    ) {}

    public record FaqDraftPayload(
        String faq_draft_id,
        String question,
        String answer_markdown,
        String summary,
        Double ai_confidence
    ) {}
}
