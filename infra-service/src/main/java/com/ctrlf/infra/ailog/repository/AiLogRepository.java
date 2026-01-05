package com.ctrlf.infra.ailog.repository;

import com.ctrlf.infra.ailog.entity.AiLog;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * AI 로그 Repository
 */
@Repository
public interface AiLogRepository extends JpaRepository<AiLog, UUID> {

    /**
     * 관리자 대시보드용 로그 조회 (필터링 및 페이징)
     */
    @Query("SELECT a FROM AiLog a WHERE " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt < :endDate) AND " +
           "(:department IS NULL OR :department = '' OR a.department = :department) AND " +
           "(:domain IS NULL OR :domain = '' OR a.domain = :domain) AND " +
           "(:route IS NULL OR :route = '' OR a.route = :route) AND " +
           "(:model IS NULL OR :model = '' OR a.modelName = :model) AND " +
           "(:onlyError IS NULL OR :onlyError = false OR a.errorCode IS NOT NULL) AND " +
           "(:hasPiiOnly IS NULL OR :hasPiiOnly = false OR a.hasPiiInput = true OR a.hasPiiOutput = true)")
    Page<AiLog> findLogsWithFilters(
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("department") String department,
        @Param("domain") String domain,
        @Param("route") String route,
        @Param("model") String model,
        @Param("onlyError") Boolean onlyError,
        @Param("hasPiiOnly") Boolean hasPiiOnly,
        Pageable pageable
    );
}

