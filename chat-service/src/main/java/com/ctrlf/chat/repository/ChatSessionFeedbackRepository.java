package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatSessionFeedback;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionFeedbackRepository extends JpaRepository<ChatSessionFeedback, UUID> {
    Optional<ChatSessionFeedback> findBySessionId(UUID sessionId);
}
