package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageCursorResponse;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;
import com.ctrlf.chat.service.ChatMessageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // ✅ 메시지 전송 (섹션 삭제 반영)
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

    // ✅ ✅ ✅ 세션 내 메시지 조회: 커서 기반(키셋) 페이징
    // GET /chat/sessions/{sessionId}/messages?cursor=...&size=20
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessageCursorResponse> getMessagesBySession(
        @PathVariable UUID sessionId,
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            chatMessageService.getMessagesBySession(sessionId, cursor, size)
        );
    }

    // ✅ Retry (섹션 삭제 반영: messageId 기준)
    @PostMapping("/sessions/{sessionId}/messages/{messageId}/retry")
    public ResponseEntity<ChatMessage> retryMessage(
        @PathVariable UUID sessionId,
        @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(
            chatMessageService.retryMessage(sessionId, messageId)
        );
    }

    // ✅ Regen (재생성) (섹션 삭제 반영: messageId 기준)
    @PostMapping("/sessions/{sessionId}/messages/{messageId}/regen")
    public ResponseEntity<ChatMessage> regenMessage(
        @PathVariable UUID sessionId,
        @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(
            chatMessageService.regenMessage(sessionId, messageId)
        );
    }
}
