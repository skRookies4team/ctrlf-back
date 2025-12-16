package com.ctrlf.chat.faq.controller;

import com.ctrlf.chat.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/faqs")
@RequiredArgsConstructor
public class AdminFaqController {

    private final FaqService faqService;

    @PostMapping("/candidates/{id}/generate")
    public UUID generate(@PathVariable UUID id) {
        return faqService.generateDraftFromCandidate(id);
    }

    @PostMapping("/drafts/{id}/approve")
    public void approve(
        @PathVariable UUID id,
        @RequestParam UUID reviewerId,
        @RequestParam String question,
        @RequestParam String answer
    ) {
        faqService.approveDraft(id, reviewerId, question, answer);
    }

    @PostMapping("/drafts/{id}/reject")
    public void reject(
        @PathVariable UUID id,
        @RequestParam UUID reviewerId,
        @RequestParam String reason
    ) {
        faqService.rejectDraft(id, reviewerId, reason);
    }
}
