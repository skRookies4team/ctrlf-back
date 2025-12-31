package com.ctrlf.chat.faq.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * FAQ 초안 배치 생성 응답 DTO (Phase 20-AI-2)
 */
@Getter
@AllArgsConstructor
public class FaqDraftGenerateBatchResponse {

    /** 요청 순서대로 응답 리스트 */
    private List<FaqDraftGenerateResponse> items;

    /** 전체 요청 수 */
    private Integer totalCount;

    /** 성공한 요청 수 */
    private Integer successCount;

    /** 실패한 요청 수 */
    private Integer failedCount;
}

