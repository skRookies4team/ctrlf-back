package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatFeedback;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, UUID> {}
