package com.ctrlf.chat.controller;

import com.ctrlf.chat.service.ChatFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatFeedbackController {

    private final ChatFeedbackService chatFeedbackService;

    // ✅ 메시지별 피드백
    @PostMapping("/sessions/{sessionId}/sections/{sectionId}/messages/{messageId}/feedback")
    public ResponseEntity<Void> messageFeedback(
        @PathVariable UUID sessionId,
        @PathVariable UUID sectionId,
        @PathVariable UUID messageId,
        @RequestParam Integer score,
        @RequestParam(required = false) String comment,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        chatFeedbackService.submitMessageFeedback(
            sessionId, sectionId, messageId, userUuid, score, comment
        );

        return ResponseEntity.ok().build();
    }

    // ✅ 세션 종료 및 총평
    @PostMapping("/sessions/{sessionId}/feedback")
    public ResponseEntity<Void> sessionFeedback(
        @PathVariable UUID sessionId,
        @RequestParam Integer score,
        @RequestParam(required = false) String comment,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        chatFeedbackService.submitSessionFeedback(
            sessionId, userUuid, score, comment
        );

        return ResponseEntity.ok().build();
    }
}
