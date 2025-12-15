package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // ✅ 세션 전체 메시지 전체 조회(기존 history 용)
    List<ChatMessage> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    // ✅ Retry/Regen용: 세션 최신 메시지
    Optional<ChatMessage> findTopBySessionIdOrderByCreatedAtDesc(UUID sessionId);

    // ✅ ✅ ✅ 커서 기반(키셋) 페이지 조회 (Native Query)
    // 정렬: 최신 -> 과거 (DESC)
    @Query(
        value = """
            SELECT *
            FROM chat.chat_message m
            WHERE m.session_id = :sessionId
              AND (
                :cursorCreatedAt IS NULL
                OR m.created_at < :cursorCreatedAt
                OR (m.created_at = :cursorCreatedAt AND m.id < :cursorId)
              )
            ORDER BY m.created_at DESC, m.id DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<ChatMessage> findNextPageBySessionId(
        @Param("sessionId") UUID sessionId,
        @Param("cursorCreatedAt") Instant cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        @Param("limit") int limit
    );
}
