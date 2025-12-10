package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // ✅ 섹션별 메시지 조회
    List<ChatMessage> findBySessionIdAndSectionIdOrderByCreatedAtAsc(
        UUID sessionId,
        UUID sectionId
    );

    // ✅ Retry / Regen 최신 메시지 조회
    Optional<ChatMessage> findTopBySessionIdAndSectionIdOrderByCreatedAtDesc(
        UUID sessionId,
        UUID sectionId
    );

    // ✅ ✅ ✅ 세션 전체 메시지 조회
    List<ChatMessage> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    // ✅ ✅ ✅ 섹션 요약용 (신규 추가)
    List<ChatMessage> findAllBySessionIdAndSectionId(
        UUID sessionId,
        UUID sectionId
    );
}
