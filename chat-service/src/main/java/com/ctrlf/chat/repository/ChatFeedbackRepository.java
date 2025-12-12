package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ChatFeedbackRepository
    extends JpaRepository<ChatFeedback, UUID> {
}
