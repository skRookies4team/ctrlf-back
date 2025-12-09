package com.ctrlf.chat.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_feedback", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class ChatFeedback {

    /** 메시지 피드백 PK */
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

    /** 피드백 대상 메시지 ID */
    @Column(name = "message_id", columnDefinition = "uuid")
    private UUID messageId;

    /** 피드백 남긴 사용자 UUID */
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUuid;

    /** 평점(1~5) */
    @Column(name = "score")
    private Integer score;

    /** 선택 코멘트(최대 500자) */
    @Column(name = "comment", length = 500)
    private String comment;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
