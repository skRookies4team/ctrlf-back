package com.ctrlf.infra.hr.repository;

import com.ctrlf.infra.hr.entity.Salary;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 급여 명세 Repository (Q13)
 */
@Repository
public interface SalaryRepository extends JpaRepository<Salary, UUID> {

    /** 특정 사용자의 특정 년월 급여 명세 조회 */
    @Query("SELECT s FROM Salary s WHERE s.userUuid = :userUuid " +
           "AND s.payYear = :year AND s.payMonth = :month")
    Optional<Salary> findByUserUuidAndYearMonth(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year,
        @Param("month") int month
    );

    /** 특정 사용자의 특정 년도 급여 명세 목록 조회 */
    @Query("SELECT s FROM Salary s WHERE s.userUuid = :userUuid " +
           "AND s.payYear = :year ORDER BY s.payMonth DESC")
    List<Salary> findByUserUuidAndYear(
        @Param("userUuid") UUID userUuid,
        @Param("year") int year
    );

    /** 특정 사용자의 최근 급여 명세 조회 */
    @Query("SELECT s FROM Salary s WHERE s.userUuid = :userUuid " +
           "ORDER BY s.payYear DESC, s.payMonth DESC")
    List<Salary> findByUserUuidOrderByPayYearDescPayMonthDesc(
        @Param("userUuid") UUID userUuid
    );
}
