package com.ctrlf.chat.controller;

import com.ctrlf.chat.dto.request.ChatFeedbackRequest;
import com.ctrlf.chat.service.ChatFeedbackService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatFeedbackController {

    private final ChatFeedbackService chatFeedbackService;

    // ✅ 메시지별 피드백만 담당 (섹션 삭제 반영)
    // POST /chat/sessions/{sessionId}/messages/{messageId}/feedback
    @PostMapping("/sessions/{sessionId}/messages/{messageId}/feedback")
    public ResponseEntity<Void> messageFeedback(
        @PathVariable UUID sessionId,
        @PathVariable UUID messageId,
        @RequestBody ChatFeedbackRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userUuid = UUID.fromString(jwt.getSubject());

        chatFeedbackService.submitMessageFeedback(
            sessionId,
            messageId,
            userUuid,
            request.score(),
            request.comment()
        );

        return ResponseEntity.ok().build();
    }
}
