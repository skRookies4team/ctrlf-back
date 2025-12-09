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

    // ✅ Retry용 최신 메시지
    Optional<ChatMessage> findTopBySessionIdAndSectionIdOrderByCreatedAtDesc(
        UUID sessionId,
        UUID sectionId
    );

    // ✅ ✅ ✅ 세션 히스토리용 (🔥 이게 없어서 터졌던 거)
    List<ChatMessage> findAllBySessionId(UUID sessionId);
}
