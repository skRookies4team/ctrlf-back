package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.request.ChatSectionCreateRequest;
import com.ctrlf.chat.dto.response.ChatSectionResponse;
import com.ctrlf.chat.dto.response.ChatSectionSummaryResponse;
import com.ctrlf.chat.service.ChatSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/sessions/{sessionId}/sections")
public class ChatSectionController {

    private final ChatSectionService chatSectionService;

    // ✅ 섹션 생성
    @PostMapping
    public ChatSectionResponse createSection(
        @PathVariable UUID sessionId,
        @RequestBody ChatSectionCreateRequest request
    ) {
        return chatSectionService.createSection(sessionId, request);
    }

    // ✅ 섹션 목록 조회
    @GetMapping
    public List<ChatSectionResponse> getSectionList(
        @PathVariable UUID sessionId
    ) {
        return chatSectionService.getSectionList(sessionId);
    }

    // ✅ 섹션 종료
    @PostMapping("/{sectionId}/close")
    public void closeSection(
        @PathVariable UUID sessionId,
        @PathVariable UUID sectionId
    ) {
        chatSectionService.closeSection(sessionId, sectionId);
    }

    // ✅ ✅ ✅ 섹션 요약 조회 (신규)
    @GetMapping("/{sectionId}/summary")
    public ChatSectionSummaryResponse getSectionSummary(
        @PathVariable UUID sessionId,
        @PathVariable UUID sectionId
    ) {
        return chatSectionService.getSectionSummary(sessionId, sectionId);
    }
}
