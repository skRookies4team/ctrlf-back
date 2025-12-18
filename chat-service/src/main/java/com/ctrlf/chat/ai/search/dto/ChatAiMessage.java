package com.ctrlf.chat.ai.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatAiMessage {
    private String role;     // "user" | "assistant"
    private String content;
}
