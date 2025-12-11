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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    @Column(name = "score")
    private Integer score;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
