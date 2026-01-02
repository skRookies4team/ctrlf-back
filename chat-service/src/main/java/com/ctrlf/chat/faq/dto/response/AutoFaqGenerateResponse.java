package com.ctrlf.chat.faq.dto.response;

import com.ctrlf.chat.faq.dto.response.FaqDraftGenerateResponse.FaqDraftPayload;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 자동 FAQ 생성 응답 DTO
 */
@Getter
@AllArgsConstructor
public class AutoFaqGenerateResponse {

    /** 처리 상태 (SUCCESS, PARTIAL, FAILED) */
    private String status;

    /** 발견된 후보 수 */
    private Integer candidatesFound;

    /** 생성된 초안 수 */
    private Integer draftsGenerated;

    /** 실패한 초안 수 */
    private Integer draftsFailed;

    /** 생성된 FAQ 초안 목록 */
    private List<FaqDraftPayload> drafts;

    /** 에러 메시지 (실패 시) */
    private String errorMessage;
}

