package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.request.ChatSessionCreateRequest;
import com.ctrlf.chat.dto.request.ChatSessionUpdateRequest;
import com.ctrlf.chat.dto.response.ChatSessionHistoryResponse;
import com.ctrlf.chat.dto.response.ChatSessionResponse;
import com.ctrlf.chat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    // ✅ 세션 생성
    @PostMapping
    public ChatSessionResponse create(@RequestBody ChatSessionCreateRequest request) {
        return chatSessionService.createSession(request);
    }

    // ❌ 세션 단건 조회 제거됨(API 명세서에 없는내용임)

    // ✅ 세션 목록 조회
    @GetMapping
    public List<ChatSessionResponse> getList() {
        return chatSessionService.getSessionList();
    }

    // ✅ 세션 수정
    @PatchMapping("/{sessionId}")
    public ChatSessionResponse update(
        @PathVariable UUID sessionId,
        @RequestBody ChatSessionUpdateRequest request
    ) {
        return chatSessionService.updateSession(sessionId, request);
    }

    // ✅ 세션 삭제
    @DeleteMapping("/{sessionId}")
    public void delete(@PathVariable UUID sessionId) {
        chatSessionService.deleteSession(sessionId);
    }

    // ✅ 세션 히스토리 조회
    @GetMapping("/{sessionId}/history")
    public ChatSessionHistoryResponse history(@PathVariable UUID sessionId) {
        return chatSessionService.getSessionHistory(sessionId);
    }
}
