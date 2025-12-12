package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;
import com.ctrlf.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // ✅ 메시지 전송
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageSendResponse> sendMessage(
        @RequestBody ChatMessageSendRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String domain = jwt.getClaimAsString("domain");

        return ResponseEntity.ok(
            chatMessageService.sendMessage(request, userId, domain)
        );
    }

    // ✅ ✅ 특정 섹션 메시지 조회
    @GetMapping("/sessions/{sessionId}/sections/{sectionId}/messages")
    public ResponseEntity<List<ChatMessage>> getMessagesBySection(
        @PathVariable UUID sessionId,
        @PathVariable UUID sectionId
    ) {
        return ResponseEntity.ok(
            chatMessageService.getMessages(sessionId, sectionId)
        );
    }

    // ✅ ✅ ✅ 세션 전체 메시지 조회 (신규 분리)
    @GetMapping("/sessions/{sessionId}/sections/messages")
    public ResponseEntity<List<ChatMessage>> getMessagesBySession(
        @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(
            chatMessageService.getMessagesBySession(sessionId)
        );
    }

    // ✅ Retry
    @PostMapping("/sessions/{sessionId}/sections/{sectionId}/retry")
    public ResponseEntity<ChatMessage> retryMessage(
        @PathVariable UUID sessionId,
        @PathVariable UUID sectionId
    ) {
        return ResponseEntity.ok(
            chatMessageService.retryMessage(sessionId, sectionId)
        );
    }

    // ✅ Regen (재생성)
    @PostMapping("/sessions/{sessionId}/sections/{sectionId}/regen")
    public ResponseEntity<ChatMessage> regenMessage(
        @PathVariable UUID sessionId,
        @PathVariable UUID sectionId
    ) {
        return ResponseEntity.ok(
            chatMessageService.regenMessage(sessionId, sectionId)
        );
    }
}

