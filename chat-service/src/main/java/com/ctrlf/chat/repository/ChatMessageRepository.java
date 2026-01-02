package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatMessage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    Optional<ChatMessage> findTopBySessionIdOrderByCreatedAtDesc(UUID sessionId);

    // ✅ 추가: 해당 세션에서 가장 최근 user 메시지 1개
    Optional<ChatMessage> findTopBySessionIdAndRoleOrderByCreatedAtDesc(UUID sessionId, String role);

    @Query(
        value = """
            SELECT *
            FROM chat.chat_message m
            WHERE m.session_id = :sessionId
            ORDER BY m.created_at DESC, m.id DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<ChatMessage> findFirstPageBySessionId(
        @Param("sessionId") UUID sessionId,
        @Param("limit") int limit
    );

    @Query(
        value = """
            SELECT *
            FROM chat.chat_message m
            WHERE m.session_id = :sessionId
              AND (
                m.created_at < :cursorCreatedAt
                OR (m.created_at = :cursorCreatedAt AND m.id < :cursorId)
              )
            ORDER BY m.created_at DESC, m.id DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<ChatMessage> findNextPageBySessionId(
        @Param("sessionId") UUID sessionId,
        @Param("cursorCreatedAt") Instant cursorCreatedAt,
        @Param("cursorId") UUID cursorId,
        @Param("limit") int limit
    );

    // ========================
    // 대시보드 통계 쿼리
    // ========================

    /**
     * 오늘 질문 수 조회 (user role 메시지)
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM chat.chat_message m
            WHERE m.role = 'user'
              AND DATE(m.created_at) = CURRENT_DATE
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Long countTodayQuestions(@Param("department") String department);

    /**
     * 평균 응답 시간 조회 (밀리초)
     */
    @Query(
        value = """
            SELECT COALESCE(AVG(m.response_time_ms), 0)
            FROM chat.chat_message m
            WHERE m.role = 'assistant'
              AND m.response_time_ms IS NOT NULL
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Long getAverageResponseTime(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * PII 감지 비율 조회 (%)
     */
    @Query(
        value = """
            SELECT CASE
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN m.pii_detected = true THEN 1 END) * 100.0 / COUNT(*))
            END
            FROM chat.chat_message m
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Double getPiiDetectionRate(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * 에러율 조회 (%)
     */
    @Query(
        value = """
            SELECT CASE
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN m.is_error = true THEN 1 END) * 100.0 / COUNT(*))
            END
            FROM chat.chat_message m
            WHERE m.role = 'assistant'
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Double getErrorRate(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * 기간 내 질문 수 조회
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM chat.chat_message m
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Long countQuestionsByPeriod(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * 활성 사용자 수 조회 (고유 user_uuid 기준)
     */
    @Query(
        value = """
            SELECT COUNT(DISTINCT s.user_uuid)
            FROM chat.chat_session s
            INNER JOIN chat.chat_message m ON m.session_id = s.id
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND s.deleted = false
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Long countActiveUsers(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * RAG 사용 비율 조회 (%)
     */
    @Query(
        value = """
            SELECT CASE
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN m.routing_type = 'RAG' THEN 1 END) * 100.0 / COUNT(*))
            END
            FROM chat.chat_message m
            WHERE m.role = 'assistant'
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            """,
        nativeQuery = true
    )
    Double getRagUsageRate(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * 라우트별 질문 비율 조회
     */
    @Query(
        value = """
            SELECT 
                COALESCE(m.routing_type, 'OTHER') as route_type,
                COUNT(*) as count,
                COUNT(*) * 100.0 / (SELECT COUNT(*) FROM chat.chat_message m2 
                    WHERE m2.role = 'assistant' 
                      AND m2.created_at >= :startDate
                      AND (:department IS NULL OR m2.department = :department)) as ratio
            FROM chat.chat_message m
            WHERE m.role = 'assistant'
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            GROUP BY COALESCE(m.routing_type, 'OTHER')
            ORDER BY count DESC
            """,
        nativeQuery = true
    )
    List<Object[]> getRouteRatio(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * 최근 많이 질문된 키워드 Top N 조회
     */
    @Query(
        value = """
            SELECT 
                m.keyword,
                COUNT(*) as question_count
            FROM chat.chat_message m
            WHERE m.role = 'user'
              AND m.keyword IS NOT NULL
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            GROUP BY m.keyword
            ORDER BY question_count DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<Object[]> getTopKeywords(
        @Param("startDate") Instant startDate,
        @Param("department") String department,
        @Param("limit") int limit
    );

    /**
     * 주간 질문 수 및 에러율 추이 조회
     */
    @Query(
        value = """
            SELECT 
                TO_CHAR(DATE_TRUNC('week', m.created_at), 'YYYY-MM-DD') as week_start,
                COUNT(*) as question_count,
                COUNT(CASE WHEN m.is_error = true THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0) as error_rate
            FROM chat.chat_message m
            WHERE m.role = 'assistant'
              AND m.created_at >= :startDate
              AND (:department IS NULL OR m.department = :department)
            GROUP BY DATE_TRUNC('week', m.created_at)
            ORDER BY week_start ASC
            """,
        nativeQuery = true
    )
    List<Object[]> getQuestionTrend(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );

    /**
     * 도메인별 질문 비율 조회
     */
    @Query(
        value = """
            SELECT 
                COALESCE(UPPER(TRIM(s.domain)), 'OTHER') as domain,
                COUNT(*) as count,
                COUNT(*) * 100.0 / NULLIF((SELECT COUNT(*) FROM chat.chat_message m2 
                    INNER JOIN chat.chat_session s2 ON s2.id = m2.session_id
                    WHERE m2.role = 'user' 
                      AND m2.created_at >= :startDate
                      AND s2.deleted = false
                      AND (:department IS NULL OR m2.department = :department)), 0) as ratio
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON s.id = m.session_id
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND s.deleted = false
              AND (:department IS NULL OR m.department = :department)
            GROUP BY COALESCE(UPPER(TRIM(s.domain)), 'OTHER')
            ORDER BY count DESC
            """,
        nativeQuery = true
    )
    List<Object[]> getDomainRatio(
        @Param("startDate") Instant startDate,
        @Param("department") String department
    );
}
