package com.ctrlf.chat.faq.controller;

import com.ctrlf.chat.faq.dto.request.FaqUiCategoryCreateRequest;
import com.ctrlf.chat.faq.dto.request.FaqUiCategoryUpdateRequest;
import com.ctrlf.chat.faq.dto.response.FaqUiCategoryResponse;
import com.ctrlf.chat.faq.service.FaqUiCategoryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/faq/ui-categories")
public class AdminFaqUiCategoryController {

    private final FaqUiCategoryService faqUiCategoryService;

    @PostMapping
    public ResponseEntity<UUID> create(
        @RequestBody FaqUiCategoryCreateRequest req,
        @RequestParam UUID operatorId
    ) {
        return ResponseEntity.ok(faqUiCategoryService.create(req, operatorId));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<Void> update(
        @PathVariable UUID categoryId,
        @RequestBody FaqUiCategoryUpdateRequest req,
        @RequestParam UUID operatorId
    ) {
        faqUiCategoryService.update(categoryId, req, operatorId);
        return ResponseEntity.ok().build();
    }

    // ✅ 비활성화 전용 API
    @PostMapping("/{categoryId}/deactivate")
    public ResponseEntity<Void> deactivate(
        @PathVariable UUID categoryId,
        @RequestParam UUID operatorId,
        @RequestParam(required = false) String reason
    ) {
        faqUiCategoryService.deactivate(categoryId, operatorId, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<FaqUiCategoryResponse>> getAll() {
        return ResponseEntity.ok(faqUiCategoryService.getAllCategories());
    }
}
