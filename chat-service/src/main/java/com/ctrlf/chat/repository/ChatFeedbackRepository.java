package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatFeedback;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, UUID> {

    /**
     * 응답 만족도 조회 (%)
     * 평점 4점 이상을 만족으로 간주
     */
    @Query(
        value = """
            SELECT CASE
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN f.score >= 4 THEN 1 END) * 100.0 / COUNT(*))
            END
            FROM chat.chat_feedback f
            INNER JOIN chat.chat_message m ON m.id = f.message_id
            WHERE f.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Double getSatisfactionRate(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );
}
