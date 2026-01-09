package com.ctrlf.chat.faq.controller;

import com.ctrlf.chat.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * FAQ 후보 / Draft 관리용 관리자 API
 */
@RestController
@RequestMapping("/admin/faqs")
@RequiredArgsConstructor
public class AdminFaqController {

    private final FaqService faqService;

    /**
     * FAQ 후보 기반 AI Draft 생성
     * 
     * @param candidateId FAQ Candidate ID (FAQ 후보 ID)
     *                    - 엔티티: {@link com.ctrlf.chat.faq.entity.FaqCandidate}
     *                    - 테이블: {@code chat.faq_candidate}
     *                    - 컬럼: {@code id} (UUID 타입)
     *                    - 조회: {@code faqCandidateRepository.findById(candidateId)}
     * @return 생성된 Draft ID
     */
    @PostMapping("/candidates/{candidateId}/generate")
    public UUID generate(@PathVariable UUID candidateId) {
        return faqService.generateDraftFromCandidate(candidateId);
    }

    /**
     * FAQ Draft 승인
     * 
     * @param draftId FAQ Draft ID (FAQ 초안 ID)
     *                - 엔티티: {@link com.ctrlf.chat.faq.entity.FaqDraft}
     *                - 테이블: {@code chat.faq_drafts}
     *                - 컬럼: {@code id} (UUID 타입)
     *                - 조회: {@code faqDraftRepository.findById(draftId)}
     *                - 동작: Draft를 승인하여 새로운 FAQ를 생성하고 Draft 상태를 PUBLISHED로 변경
     * @param reviewerId 승인자 ID
     * @param question 승인할 질문 내용
     * @param answer 승인할 답변 내용
     */
    @PostMapping("/drafts/{draftId}/approve")
    public void approve(
        @PathVariable UUID draftId,
        @RequestParam UUID reviewerId,
        @RequestParam String question,
        @RequestParam String answer
    ) {
        faqService.approveDraft(draftId, reviewerId, question, answer);
    }

    /**
     * FAQ Draft 반려
     * 
     * @param draftId FAQ Draft ID (FAQ 초안 ID)
     *                - 엔티티: {@link com.ctrlf.chat.faq.entity.FaqDraft}
     *                - 테이블: {@code chat.faq_drafts}
     *                - 컬럼: {@code id} (UUID 타입)
     *                - 조회: {@code faqDraftRepository.findById(draftId)}
     *                - 동작: Draft 상태를 REJECTED로 변경
     * @param reviewerId 반려자 ID
     * @param reason 반려 사유
     */
    @PostMapping("/drafts/{draftId}/reject")
    public void reject(
        @PathVariable UUID draftId,
        @RequestParam UUID reviewerId,
        @RequestParam String reason
    ) {
        faqService.rejectDraft(draftId, reviewerId, reason);
    }
}
