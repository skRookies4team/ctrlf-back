package com.ctrlf.chat.faq.controller;

import com.ctrlf.chat.faq.dto.request.FaqDraftApproveRequest;
import com.ctrlf.chat.faq.dto.request.FaqDraftRejectRequest;
import com.ctrlf.chat.faq.dto.response.FaqDraftResponse;
import com.ctrlf.chat.faq.service.FaqDraftService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/admin/faq/drafts")
public class AdminFaqDraftController {

    private final FaqDraftService faqDraftService;

    /**
     * 관리자 FAQ 초안 목록 조회
     */
    @GetMapping
    public List<FaqDraftResponse> getDrafts(
        @RequestParam(required = false) String domain,
        @RequestParam(required = false) String status
    ) {
        return faqDraftService.getDrafts(domain, status);
    }

    /**
     * 초안 승인 (Request Body 방식 - 권장)
     *
     * <p>프론트엔드에서 사용하기 편리한 Request Body 방식입니다.
     * 기존 Query Parameter 방식도 하위 호환성을 위해 유지됩니다.</p>
     */
    @PostMapping("/{draftId}/approve")
    public void approve(
        @PathVariable UUID draftId,
        @Valid @RequestBody FaqDraftApproveRequest request
    ) {
        faqDraftService.approve(draftId, request.getReviewerId(), request.getQuestion(), request.getAnswer());
    }

    /**
     * 초안 반려 (Request Body 방식 - 권장)
     *
     * <p>프론트엔드에서 사용하기 편리한 Request Body 방식입니다.
     * 기존 Query Parameter 방식도 하위 호환성을 위해 유지됩니다.</p>
     */
    @PostMapping("/{draftId}/reject")
    public void reject(
        @PathVariable UUID draftId,
        @Valid @RequestBody FaqDraftRejectRequest request
    ) {
        faqDraftService.reject(draftId, request.getReviewerId(), request.getReason());
    }
}
