package com.ctrlf.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_message", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {

    /** 메시지 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 세션 ID */
    @Column(name = "session_id", columnDefinition = "uuid")
    private UUID sessionId;

    /** 섹션 ID */
    @Column(name = "section_id", columnDefinition = "uuid")
    private UUID sectionId;

    /** 역할(user/assistant/system) */
    @Column(name = "role")
    private String role;

    /** 메시지 본문 */
    @Column(name = "content", columnDefinition = "text")
    private String content;

    /** 입력 토큰 수 */
    @Column(name = "tokens_in")
    private Integer tokensIn;

    /** 출력 토큰 수 */
    @Column(name = "tokens_out")
    private Integer tokensOut;

    /** 사용된 LLM 모델명 */
    @Column(name = "llm_model")
    private String llmModel;

    /** 메시지 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;
}

