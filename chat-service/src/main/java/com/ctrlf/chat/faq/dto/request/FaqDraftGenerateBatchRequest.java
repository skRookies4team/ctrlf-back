package com.ctrlf.chat.faq.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;

/**
 * FAQ 초안 배치 생성 요청 DTO (Phase 20-AI-2)
 */
@Getter
public class FaqDraftGenerateBatchRequest {

    /** FAQ 초안 생성 요청 리스트 */
    @NotEmpty
    @Valid
    private List<FaqDraftGenerateRequest> items;

    /** 동시 처리 수 (선택, 기본값은 설정에서 가져옴) */
    private Integer concurrency;
}

