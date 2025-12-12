package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.request.ChatSessionFeedbackRequest;
import com.ctrlf.chat.service.ChatSessionFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatSessionFeedbackController {

    private final ChatSessionFeedbackService chatSessionFeedbackService;

    // ✅ ✅ ✅ 세션 종료 및 총평
    // POST /chat/sessions/{sessionId}/feedback
    @PostMapping("/sessions/{sessionId}/feedback")
    public ResponseEntity<Void> sessionFeedback(
        @PathVariable UUID sessionId,
        @RequestBody ChatSessionFeedbackRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        chatSessionFeedbackService.submitSessionFeedback(
            sessionId,
            userUuid,
            request.score(),
            request.comment()
        );

        return ResponseEntity.ok().build();
    }
}
