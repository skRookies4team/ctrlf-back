package com.ctrlf.infra.hr.repository;

import com.ctrlf.infra.hr.entity.LeaveHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 연차/휴가 사용 이력 Repository (Q12)
 */
@Repository
public interface LeaveHistoryRepository extends JpaRepository<LeaveHistory, UUID> {

    /**
     * 사용자의 연차 사용 이력 조회 (최근순)
     */
    List<LeaveHistory> findByUserUuidOrderByStartDateDesc(UUID userUuid);

    /**
     * 사용자의 특정 기간 연차 사용 이력 조회
     */
    @Query("SELECT l FROM LeaveHistory l WHERE l.userUuid = :userUuid " +
           "AND l.startDate >= :startDate AND l.startDate <= :endDate " +
           "ORDER BY l.startDate DESC")
    List<LeaveHistory> findByUserUuidAndPeriod(
        @Param("userUuid") UUID userUuid,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 사용자의 특정 연도 연차 사용 합계
     */
    @Query("SELECT COALESCE(SUM(l.days), 0) FROM LeaveHistory l " +
           "WHERE l.userUuid = :userUuid " +
           "AND YEAR(l.startDate) = :year " +
           "AND l.status = 'APPROVED'")
    Double sumDaysByUserUuidAndYear(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year
    );

    /**
     * 사용자의 특정 연도 연차 사용 건수
     */
    @Query("SELECT COUNT(l) FROM LeaveHistory l " +
           "WHERE l.userUuid = :userUuid " +
           "AND YEAR(l.startDate) = :year " +
           "AND l.status = 'APPROVED'")
    Long countByUserUuidAndYear(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year
    );
}
