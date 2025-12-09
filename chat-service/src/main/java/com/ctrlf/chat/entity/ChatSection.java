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
@Table(name = "chat_section", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class ChatSection {

    /** 채팅 섹션 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 소속 세션 ID */
    @Column(name = "session_id", columnDefinition = "uuid")
    private UUID sessionId;

    /** 섹션 제목(주요 질문/주제) */
    @Column(name = "title")
    private String title;

    /** 섹션 요약(LLM 생성) */
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    /** 재질문/재시도 횟수 */
    @Column(name = "retry_count")
    private Integer retryCount;

    /** 섹션 시작 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 섹션 종료 시각 */
    @Column(name = "closed_at")
    private Instant closedAt;
}

