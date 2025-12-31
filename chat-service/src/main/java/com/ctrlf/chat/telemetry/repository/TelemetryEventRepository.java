package com.ctrlf.chat.telemetry.repository;

import com.ctrlf.chat.telemetry.entity.TelemetryEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Telemetry 이벤트 Repository
 * 
 * @author CtrlF Team
 * @since 1.0.0
 */
public interface TelemetryEventRepository extends JpaRepository<TelemetryEvent, UUID> {

    /**
     * eventId로 이벤트 조회 (중복 체크용)
     */
    Optional<TelemetryEvent> findByEventId(UUID eventId);

    /**
     * 기간 내 CHAT_TURN 이벤트 수 조회
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Long countChatTurns(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 오늘 CHAT_TURN 이벤트 수 조회
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND DATE(e.occurred_at) = CURRENT_DATE
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Long countTodayChatTurns(@Param("deptId") String deptId);

    /**
     * 기간 내 고유 사용자 수 조회
     */
    @Query(
        value = """
            SELECT COUNT(DISTINCT e.user_id)
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Long countActiveUsers(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 평균 지연 시간 조회 (밀리초)
     */
    @Query(
        value = """
            SELECT COALESCE(
                AVG((e.payload->>'latencyMsTotal')::numeric),
                0
            )
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
              AND e.payload->>'latencyMsTotal' IS NOT NULL
            """,
        nativeQuery = true
    )
    Double getAverageLatencyMs(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 PII 감지 비율 조회
     */
    @Query(
        value = """
            SELECT CASE
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (
                    COUNT(CASE 
                        WHEN (e.payload->>'piiDetectedInput')::boolean = true 
                             OR (e.payload->>'piiDetectedOutput')::boolean = true 
                        THEN 1 
                    END) * 100.0 / COUNT(*)
                )
            END
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Double getPiiDetectRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 에러율 조회
     */
    @Query(
        value = """
            SELECT CASE
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (
                    COUNT(CASE 
                        WHEN e.payload->>'errorCode' IS NOT NULL 
                             AND e.payload->>'errorCode' != 'null'
                        THEN 1 
                    END) * 100.0 / COUNT(*)
                )
            END
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Double getErrorRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 만족도 비율 조회 (like / (like + dislike))
     */
    @Query(
        value = """
            SELECT 
                COUNT(CASE WHEN e.payload->>'feedback' = 'like' THEN 1 END)::numeric as like_count,
                COUNT(CASE WHEN e.payload->>'feedback' = 'dislike' THEN 1 END)::numeric as dislike_count
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'FEEDBACK'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Object[] getFeedbackCounts(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 RAG 사용 비율 조회
     */
    @Query(
        value = """
            SELECT CASE
                WHEN COUNT(*) = 0 THEN 0.0
                ELSE (
                    COUNT(CASE 
                        WHEN (e.payload->>'ragUsed')::boolean = true 
                        THEN 1 
                    END) * 100.0 / COUNT(*)
                )
            END
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Double getRagUsageRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 도메인별 질문 수 및 비율 조회
     */
    @Query(
        value = """
            WITH domain_counts AS (
                SELECT 
                    e.payload->>'domain' as domain,
                    COUNT(*) as question_count
                FROM telemetry.telemetry_event e
                WHERE e.event_type = 'CHAT_TURN'
                  AND e.occurred_at >= :startDate
                  AND e.occurred_at < :endDate
                  AND (:deptId = 'all' OR e.dept_id = :deptId)
                GROUP BY e.payload->>'domain'
            ),
            total_count AS (
                SELECT SUM(question_count) as total
                FROM domain_counts
            )
            SELECT 
                COALESCE(dc.domain, 'ETC') as domain,
                dc.question_count,
                CASE 
                    WHEN tc.total = 0 THEN 0.0
                    ELSE (dc.question_count * 100.0 / tc.total)
                END as share
            FROM domain_counts dc
            CROSS JOIN total_count tc
            ORDER BY dc.question_count DESC
            """,
        nativeQuery = true
    )
    List<Object[]> getDomainShare(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 추이 데이터 조회 (버킷별)
     */
    @Query(
        value = """
            SELECT 
                TO_CHAR(
                    CASE 
                        WHEN :bucket = 'day' THEN DATE_TRUNC('day', e.occurred_at)
                        ELSE DATE_TRUNC('week', e.occurred_at)
                    END,
                    'YYYY-MM-DD'
                ) as bucket_start,
                COUNT(*) as question_count,
                CASE
                    WHEN COUNT(*) = 0 THEN 0.0
                    ELSE (
                        COUNT(CASE 
                            WHEN e.payload->>'errorCode' IS NOT NULL 
                                 AND e.payload->>'errorCode' != 'null'
                            THEN 1 
                        END) * 100.0 / COUNT(*)
                    )
                END as error_rate
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            GROUP BY 
                CASE 
                    WHEN :bucket = 'day' THEN DATE_TRUNC('day', e.occurred_at)
                    ELSE DATE_TRUNC('week', e.occurred_at)
                END
            ORDER BY bucket_start ASC
            """,
        nativeQuery = true
    )
    List<Object[]> getTrends(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId,
        @Param("bucket") String bucket
    );

    /**
     * 기간 내 보안 이벤트 수 조회
     */
    @Query(
        value = """
            SELECT 
                COUNT(CASE WHEN e.payload->>'blockType' = 'PII_BLOCK' AND (e.payload->>'blocked')::boolean = true THEN 1 END) as pii_block_count,
                COUNT(CASE WHEN e.payload->>'blockType' = 'EXTERNAL_DOMAIN_BLOCK' AND (e.payload->>'blocked')::boolean = true THEN 1 END) as external_domain_block_count
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'SECURITY'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Object[] getSecurityCounts(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 PII 추이 조회 (버킷별)
     */
    @Query(
        value = """
            SELECT 
                TO_CHAR(DATE_TRUNC('week', e.occurred_at), 'YYYY-MM-DD') as bucket_start,
                CASE
                    WHEN COUNT(*) = 0 THEN 0.0
                    ELSE (
                        COUNT(CASE 
                            WHEN (e.payload->>'piiDetectedInput')::boolean = true 
                            THEN 1 
                        END) * 100.0 / COUNT(*)
                    )
                END as input_detect_rate,
                CASE
                    WHEN COUNT(*) = 0 THEN 0.0
                    ELSE (
                        COUNT(CASE 
                            WHEN (e.payload->>'piiDetectedOutput')::boolean = true 
                            THEN 1 
                        END) * 100.0 / COUNT(*)
                    )
                END as output_detect_rate
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            GROUP BY DATE_TRUNC('week', e.occurred_at)
            ORDER BY bucket_start ASC
            """,
        nativeQuery = true
    )
    List<Object[]> getPiiTrend(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 불만족 비율 조회
     */
    @Query(
        value = """
            SELECT 
                COUNT(CASE WHEN e.payload->>'feedback' = 'dislike' THEN 1 END)::numeric as dislike_count,
                COUNT(CASE WHEN e.payload->>'feedback' = 'like' THEN 1 END)::numeric as like_count
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'FEEDBACK'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Object[] getDislikeRate(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 OOS 수 조회
     */
    @Query(
        value = """
            SELECT COUNT(*)
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND (e.payload->>'oos')::boolean = true
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
            """,
        nativeQuery = true
    )
    Long getOosCount(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 지연 시간 히스토그램 조회
     */
    @Query(
        value = """
            SELECT 
                CASE 
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 500 THEN '0-500ms'
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 1000 THEN '0.5-1s'
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 2000 THEN '1-2s'
                    ELSE '2s+'
                END as range,
                COUNT(*) as count
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
              AND e.payload->>'latencyMsTotal' IS NOT NULL
            GROUP BY 
                CASE 
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 500 THEN '0-500ms'
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 1000 THEN '0.5-1s'
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 2000 THEN '1-2s'
                    ELSE '2s+'
                END
            ORDER BY 
                CASE 
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 500 THEN 1
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 1000 THEN 2
                    WHEN (e.payload->>'latencyMsTotal')::numeric < 2000 THEN 3
                    ELSE 4
                END
            """,
        nativeQuery = true
    )
    List<Object[]> getLatencyHistogram(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );

    /**
     * 기간 내 모델별 평균 지연 시간 조회
     */
    @Query(
        value = """
            SELECT 
                e.payload->>'model' as model,
                AVG((e.payload->>'latencyMsLlm')::numeric) as avg_latency_ms
            FROM telemetry.telemetry_event e
            WHERE e.event_type = 'CHAT_TURN'
              AND e.occurred_at >= :startDate
              AND e.occurred_at < :endDate
              AND (:deptId = 'all' OR e.dept_id = :deptId)
              AND e.payload->>'model' IS NOT NULL
              AND e.payload->>'latencyMsLlm' IS NOT NULL
            GROUP BY e.payload->>'model'
            ORDER BY avg_latency_ms DESC
            """,
        nativeQuery = true
    )
    List<Object[]> getModelLatency(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("deptId") String deptId
    );
}

