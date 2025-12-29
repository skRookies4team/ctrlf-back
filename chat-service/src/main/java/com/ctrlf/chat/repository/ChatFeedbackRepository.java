package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatFeedback;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, UUID> {

    /**
     * 기간 내 응답 만족도 조회 (평균 점수 기반, 5점 만점)
     */
    @Query(
        value = """
            SELECT 
              CASE 
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (AVG(f.score)::FLOAT / 5.0 * 100)
              END
            FROM chat.chat_feedback f
            INNER JOIN chat.chat_message m ON f.message_id = m.id
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE f.created_at >= :startDate
              AND f.created_at < :endDate
              AND f.score IS NOT NULL
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            """,
        nativeQuery = true
    )
    Double getSatisfactionRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );
}
