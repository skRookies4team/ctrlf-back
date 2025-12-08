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
@Table(name = "chat_session", schema = "chat")
@Getter
@Setter
@NoArgsConstructor
public class ChatSession {

    /** 채팅 세션(채팅방) PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 세션 생성 사용자 UUID */
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUuid;

    /** 세션 제목 */
    @Column(name = "title")
    private String title;

    /** 업무 도메인(FAQ/보안/직무/상담 등) */
    @Column(name = "domain")
    private String domain;

    /** 세션 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 마지막 메시지 업데이트 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** 삭제 플래그(마이그레이션: deleted 또는 deleted_at 대체) */
    @Column(name = "deleted")
    private Boolean deleted;
}

