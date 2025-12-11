package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // ✅ 세션 전체 메시지 조회
    List<ChatMessage> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    // ✅ Retry / Regen 최신 메시지 조회 (섹션 제거 버전)
    Optional<ChatMessage> findTopBySessionIdOrderByCreatedAtDesc(UUID sessionId);
}
