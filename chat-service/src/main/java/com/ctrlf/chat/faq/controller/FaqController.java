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

    /**
     * FAQ 생성 (관리자)
     * 
     * @param request FAQ 생성 요청 (질문, 답변, 도메인, 우선순위)
     * @return 생성된 FAQ ID
     */
    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody FaqCreateRequest request) {
        return ResponseEntity.ok(faqService.create(request));
    }

    /**
     * FAQ 수정 (관리자)
     * 
     * @param faqId FAQ ID (수정할 FAQ의 UUID)
     *              - 엔티티: {@link com.ctrlf.chat.faq.entity.Faq}
     *              - 테이블: {@code chat.faq}
     *              - 컬럼: {@code id} (UUID 타입, FAQ의 Primary Key)
     *              - 조회: {@code faqRepository.findById(faqId)} - FAQ 엔티티를 조회
     *              - 주의: 유저 ID가 아닌 FAQ ID를 사용합니다
     * @param request FAQ 수정 요청 (질문, 답변, 도메인, 활성 여부, 우선순위)
     */
    @PatchMapping("/{faqId}")
    public ResponseEntity<Void> update(
        @PathVariable UUID faqId,
        @RequestBody FaqUpdateRequest request
    ) {
        faqService.update(faqId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * FAQ 삭제 (관리자 - Soft Delete)
     * 
     * @param faqId FAQ ID (삭제할 FAQ의 UUID)
     *              - 엔티티: {@link com.ctrlf.chat.faq.entity.Faq}
     *              - 테이블: {@code chat.faq}
     *              - 컬럼: {@code id} (UUID 타입, FAQ의 Primary Key)
     *              - 조회: {@code faqRepository.findById(faqId)} - FAQ 엔티티를 조회
     *              - 실제 동작: {@code isActive = false}로 설정 (소프트 삭제)
     *              - 주의: 유저 ID가 아닌 FAQ ID를 사용합니다
     */
    @DeleteMapping("/{faqId}")
    public ResponseEntity<Void> delete(@PathVariable UUID faqId) {
        faqService.delete(faqId);
        return ResponseEntity.ok().build();
    }

    // ✅ FAQ 조회 (유저)
    @GetMapping
    public ResponseEntity<List<FaqResponse>> getAll() {
        return ResponseEntity.ok(faqService.getAll());
    }
}
