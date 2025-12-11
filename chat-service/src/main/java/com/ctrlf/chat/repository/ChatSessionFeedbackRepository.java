package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatSessionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionFeedbackRepository
    extends JpaRepository<ChatSessionFeedback, UUID> {

    // ✅ 세션 총평 조회
    Optional<ChatSessionFeedback> findBySessionId(UUID sessionId);
}
