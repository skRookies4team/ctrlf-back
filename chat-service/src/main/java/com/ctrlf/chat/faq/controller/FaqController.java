package com.ctrlf.chat.faq.controller;

import com.ctrlf.chat.faq.dto.request.FaqCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqUpdateRequest;
import com.ctrlf.chat.faq.dto.response.FaqResponse;
import com.ctrlf.chat.faq.service.FaqService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // ✅ FAQ 생성 (관리자)
    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody FaqCreateRequest request) {
        return ResponseEntity.ok(faqService.create(request));
    }

    // ✅ FAQ 수정 (관리자)
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
        @PathVariable UUID id,
        @RequestBody FaqUpdateRequest request
    ) {
        faqService.update(id, request);
        return ResponseEntity.ok().build();
    }

    // ✅ FAQ 삭제 (관리자 - Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        faqService.delete(id);
        return ResponseEntity.ok().build();
    }

    // ✅ FAQ 조회 (유저)
    @GetMapping
    public ResponseEntity<List<FaqResponse>> getAll() {
        return ResponseEntity.ok(faqService.getAll());
    }
}
