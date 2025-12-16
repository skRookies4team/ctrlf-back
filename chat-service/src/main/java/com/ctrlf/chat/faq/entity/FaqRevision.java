package com.ctrlf.chat.faq.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "faq_revisions", schema = "chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FaqRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id", nullable = false, columnDefinition = "uuid")
    private UUID targetId;

    @Column(nullable = false)
    private String action;

    @Column(name = "actor_id", columnDefinition = "uuid")
    private UUID actorId;

    @Column
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /* ======================
       Static Factory Method
       ====================== */

    public static FaqRevision create(
        String targetType,
        UUID targetId,
        String action,
        UUID actorId,
        String reason
    ) {
        FaqRevision revision = new FaqRevision();
        revision.targetType = targetType;
        revision.targetId = targetId;
        revision.action = action;
        revision.actorId = actorId;
        revision.reason = reason;
        revision.createdAt = Instant.now();
        return revision;
    }
}
