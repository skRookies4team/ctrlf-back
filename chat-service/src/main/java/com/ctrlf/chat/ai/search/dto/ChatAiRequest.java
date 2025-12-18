package com.ctrlf.chat.ai.search.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatAiRequest {

    private UUID sessionId;
    private UUID userId;
    private String userRole;
    private String department;
    private String domain;
    private String channel;
    private List<ChatAiMessage> messages;
}

