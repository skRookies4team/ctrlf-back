package com.ctrlf.infra.hr.repository;

import com.ctrlf.infra.hr.entity.WelfarePointUsage;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 복지 포인트 사용 내역 Repository (Q15)
 */
@Repository
public interface WelfarePointUsageRepository extends JpaRepository<WelfarePointUsage, UUID> {

    /**
     * 사용자의 복지 포인트 사용 내역 조회 (최근순)
     */
    List<WelfarePointUsage> findByUserUuidOrderByUsageDateDesc(UUID userUuid);

    /**
     * 사용자의 특정 기간 복지 포인트 사용 내역 조회
     */
    @Query("SELECT w FROM WelfarePointUsage w WHERE w.userUuid = :userUuid " +
           "AND w.usageDate >= :startDate AND w.usageDate <= :endDate " +
           "ORDER BY w.usageDate DESC")
    List<WelfarePointUsage> findByUserUuidAndPeriod(
        @Param("userUuid") UUID userUuid,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 사용자의 특정 연도 복지 포인트 사용 내역 조회
     */
    @Query("SELECT w FROM WelfarePointUsage w WHERE w.userUuid = :userUuid " +
           "AND YEAR(w.usageDate) = :year " +
           "ORDER BY w.usageDate DESC")
    List<WelfarePointUsage> findByUserUuidAndYear(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year
    );

    /**
     * 사용자의 카테고리별 사용 금액 합계
     */
    @Query("SELECT w.category, SUM(w.amount) FROM WelfarePointUsage w " +
           "WHERE w.userUuid = :userUuid " +
           "AND YEAR(w.usageDate) = :year " +
           "GROUP BY w.category")
    List<Object[]> sumAmountByCategory(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year
    );

    /**
     * 사용자의 특정 연도 복지 포인트 사용 건수
     */
    @Query("SELECT COUNT(w) FROM WelfarePointUsage w " +
           "WHERE w.userUuid = :userUuid " +
           "AND YEAR(w.usageDate) = :year")
    Long countByUserUuidAndYear(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year
    );
}
