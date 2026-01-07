package com.ctrlf.infra.hr.repository;

import com.ctrlf.infra.hr.entity.Attendance;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 근태 기록 Repository (Q10)
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    /** 특정 사용자의 특정 기간 근태 기록 조회 */
    @Query("SELECT a FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND a.workDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.workDate DESC")
    List<Attendance> findByUserUuidAndPeriod(
        @Param("userUuid") UUID userUuid,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /** 특정 사용자의 특정 년월 근태 기록 조회 */
    @Query("SELECT a FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "ORDER BY a.workDate DESC")
    List<Attendance> findByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );

    /** 지각 횟수 조회 */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "AND a.status = 'LATE'")
    Long countLateByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );

    /** 조퇴 횟수 조회 */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "AND a.status = 'EARLY_LEAVE'")
    Long countEarlyLeaveByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );

    /** 결근 횟수 조회 */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "AND a.status = 'ABSENT'")
    Long countAbsentByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );

    /** 재택근무 일수 조회 */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "AND a.workType = 'REMOTE'")
    Long countRemoteByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );

    /** 초과근무 시간 합계 조회 */
    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month")
    BigDecimal sumOvertimeByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );

    /** 실제 출근 일수 조회 */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userUuid = :userUuid " +
           "AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month " +
           "AND a.status != 'ABSENT'")
    Long countActualWorkDaysByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );
}
