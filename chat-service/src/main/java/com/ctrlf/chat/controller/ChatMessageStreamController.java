package com.ctrlf.chat.controller;

import com.ctrlf.chat.service.ChatStreamService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat/messages")
@RequiredArgsConstructor
public class ChatMessageStreamController {

    private final ChatStreamService chatStreamService;

    @GetMapping("/{messageId}/stream")
    public SseEmitter stream(@PathVariable UUID messageId) {
        SseEmitter emitter = new SseEmitter(0L);
        chatStreamService.stream(messageId, emitter);
        return emitter;
    }
}
