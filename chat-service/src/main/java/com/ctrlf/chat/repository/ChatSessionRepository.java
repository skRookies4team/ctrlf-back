package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    @Query("""
        SELECT s
        FROM ChatSession s
        WHERE s.deleted = false
        ORDER BY s.createdAt DESC
    """)
    List<ChatSession> findAllActive();

    @Query("""
        SELECT s
        FROM ChatSession s
        WHERE s.id = :sessionId
          AND s.deleted = false
    """)
    ChatSession findActiveById(UUID sessionId);
}
