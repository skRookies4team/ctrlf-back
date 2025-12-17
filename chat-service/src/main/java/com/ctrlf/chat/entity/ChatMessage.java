package com.ctrlf.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_message", schema = "chat")
@Getter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "uuid")
    private UUID sessionId;

    @Column(nullable = false, length = 20)
    private String role;

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
    void onCreate() {
        this.createdAt = Instant.now();
    }

    // user 메시지 생성
    public static ChatMessage userMessage(UUID sessionId, String content) {
        ChatMessage m = new ChatMessage();
        m.sessionId = sessionId;
        m.role = "user";
        m.content = content;
        return m;
    }

    // assistant 메시지 생성 (초기 content는 빈 문자열이어도 됨)
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

    // ✅ 스트리밍 완료 후 최종 답변 업데이트용
    public void updateContent(String content) {
        this.content = content;
    }
}
