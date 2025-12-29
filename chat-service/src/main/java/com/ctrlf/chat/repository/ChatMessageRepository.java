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

    // ==================== 관리자 대시보드 통계 쿼리 ====================

    /**
     * 기간 내 사용자 메시지 수 조회 (질문 수)
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            """,
        nativeQuery = true
    )
    Long countUserMessagesByPeriod(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );

    /**
     * 오늘 질문 수 조회
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'user'
              AND DATE(m.created_at) = CURRENT_DATE
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            """,
        nativeQuery = true
    )
    Long countTodayQuestions(@Param("department") String department);

    /**
     * 기간 내 평균 응답 시간 조회
     */
    @Query(
        value = """
            SELECT AVG(m.response_time_ms)
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'assistant'
              AND m.response_time_ms IS NOT NULL
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            """,
        nativeQuery = true
    )
    Double getAverageResponseTime(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );

    /**
     * 기간 내 PII 감지 비율 조회
     */
    @Query(
        value = """
            SELECT 
              CASE 
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN m.pii_detected = true THEN 1 END)::FLOAT / COUNT(*)::FLOAT * 100)
              END
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            """,
        nativeQuery = true
    )
    Double getPiiDetectionRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );

    /**
     * 기간 내 에러율 조회
     */
    @Query(
        value = """
            SELECT 
              CASE 
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN m.status = 'ERROR' THEN 1 END)::FLOAT / COUNT(*)::FLOAT * 100)
              END
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'assistant'
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            """,
        nativeQuery = true
    )
    Double getErrorRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );

    /**
     * 기간 내 활성 사용자 수 조회 (질문한 고유 사용자 수)
     */
    @Query(
        value = """
            SELECT COUNT(DISTINCT s.user_uuid)
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND s.user_uuid IS NOT NULL
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            """,
        nativeQuery = true
    )
    Long countActiveUsers(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );

    /**
     * 라우트별 질문 비율 조회
     */
    @Query(
        value = """
            SELECT 
              COALESCE(m.routing_type, 'OTHER') as route_type,
              COUNT(*) as question_count
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            GROUP BY COALESCE(m.routing_type, 'OTHER')
            """,
        nativeQuery = true
    )
    List<Object[]> getRouteRatio(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
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
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'user'
              AND m.keyword IS NOT NULL
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            GROUP BY m.keyword
            ORDER BY question_count DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<Object[]> getTopKeywords(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department,
        @Param("limit") int limit
    );

    /**
     * 주간 질문 수 및 에러율 조회
     */
    @Query(
        value = """
            SELECT 
              DATE_PART('week', m.created_at) - DATE_PART('week', :startDate) + 1 as week,
              COUNT(CASE WHEN m.role = 'user' THEN 1 END) as question_count,
              CASE 
                WHEN COUNT(CASE WHEN m.role = 'assistant' THEN 1 END) = 0 THEN 0.0
                ELSE (COUNT(CASE WHEN m.role = 'assistant' AND m.status = 'ERROR' THEN 1 END)::FLOAT / 
                      COUNT(CASE WHEN m.role = 'assistant' THEN 1 END)::FLOAT * 100)
              END as error_rate
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            GROUP BY DATE_PART('week', m.created_at)
            ORDER BY week
            """,
        nativeQuery = true
    )
    List<Object[]> getWeeklyTrend(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );

    /**
     * 도메인별 질문 비율 조회
     */
    @Query(
        value = """
            SELECT 
              COALESCE(s.domain, 'OTHER') as domain_type,
              COUNT(*) as question_count
            FROM chat.chat_message m
            INNER JOIN chat.chat_session s ON m.session_id = s.id
            WHERE m.role = 'user'
              AND m.created_at >= :startDate
              AND m.created_at < :endDate
              AND s.deleted = false
              AND (:department IS NULL OR EXISTS (
                SELECT 1 FROM infra.user_profile up
                WHERE up.user_uuid = s.user_uuid
                  AND up.department = :department
              ))
            GROUP BY COALESCE(s.domain, 'OTHER')
            """,
        nativeQuery = true
    )
    List<Object[]> getDomainRatio(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department
    );
}
