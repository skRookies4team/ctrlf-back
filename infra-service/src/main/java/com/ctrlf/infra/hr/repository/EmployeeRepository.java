package com.ctrlf.infra.hr.repository;

import com.ctrlf.infra.hr.entity.Employee;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 직원 정보 Repository (Q16, Q17)
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    /** 사용자 UUID로 직원 정보 조회 */
    Optional<Employee> findByUserUuid(UUID userUuid);

    /** 사원번호로 직원 정보 조회 */
    Optional<Employee> findByEmployeeId(String employeeId);

    /** 특정 부서의 직원 목록 조회 */
    @Query("SELECT e FROM Employee e WHERE e.departmentUuid = :departmentUuid " +
           "AND e.status = 'ACTIVE' ORDER BY e.position, e.name")
    List<Employee> findByDepartmentUuid(@Param("departmentUuid") UUID departmentUuid);

    /** 특정 부서의 활성 직원 수 조회 */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.departmentUuid = :departmentUuid " +
           "AND e.status = 'ACTIVE'")
    Long countByDepartmentUuid(@Param("departmentUuid") UUID departmentUuid);

    /** 특정 부서의 직급별 직원 수 조회 */
    @Query("SELECT e.position, COUNT(e) FROM Employee e WHERE e.departmentUuid = :departmentUuid " +
           "AND e.status = 'ACTIVE' GROUP BY e.position")
    List<Object[]> countByDepartmentUuidGroupByPosition(@Param("departmentUuid") UUID departmentUuid);
}
