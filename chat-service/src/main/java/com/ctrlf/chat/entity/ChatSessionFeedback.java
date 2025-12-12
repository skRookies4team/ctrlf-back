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
@Table(name = "chat_session_feedback", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class ChatSessionFeedback {

    /** 세션 총평 피드백 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 세션 ID */
    @Column(name = "session_id", columnDefinition = "uuid")
    private UUID sessionId;

    /** 피드백 남긴 사용자 UUID */
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUuid;

    /** 세션 만족도(1~5) */
    @Column(name = "score")
    private Integer score;

    /** 총평 코멘트 */
    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    /** 평가 시각 */
    @Column(name = "created_at")
    private Instant createdAt;
}

