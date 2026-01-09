package com.ctrlf.infra.hr.repository;

import com.ctrlf.infra.hr.entity.Department;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 부서 정보 Repository (Q17)
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /** 부서 UUID로 부서 정보 조회 */
    Optional<Department> findById(UUID id);

    /** 부서 코드로 부서 정보 조회 */
    Optional<Department> findByDepartmentCode(String departmentCode);

    /** 부서명으로 부서 정보 조회 */
    Optional<Department> findByDepartmentName(String departmentName);
}
