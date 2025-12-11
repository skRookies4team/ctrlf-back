package com.ctrlf.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_message", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(nullable = false, length = 20)
    private String role; // user / assistant

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "tokens_in")
    private Integer tokensIn;

    @Column(name = "tokens_out")
    private Integer tokensOut;

    @Column(name = "llm_model")
    private String llmModel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public static ChatMessage userMessage(UUID sessionId, String content) {
        ChatMessage m = new ChatMessage();
        m.sessionId = sessionId;
        m.role = "user";
        m.content = content;
        return m;
    }

    public static ChatMessage assistantMessage(
        UUID sessionId,
        String content,
        Integer tokensIn,
        Integer tokensOut,
        String llmModel
    ) {
        ChatMessage m = new ChatMessage();
        m.sessionId = sessionId;
        m.role = "assistant";
        m.content = content;
        m.tokensIn = tokensIn;
        m.tokensOut = tokensOut;
        m.llmModel = llmModel;
        return m;
    }
}
