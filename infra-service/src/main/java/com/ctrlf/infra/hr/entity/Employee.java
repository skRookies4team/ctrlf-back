package com.ctrlf.infra.hr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 직원(인사) 정보 엔티티 (Q16)
 */
@Entity
@Table(name = "employee", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID (keycloak user id) */
    @Column(name = "user_uuid", nullable = false, unique = true)
    private UUID userUuid;

    /** 사원번호 */
    @Column(name = "employee_id", length = 20, nullable = false)
    private String employeeId;

    /** 이름 */
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /** 부서 UUID */
    @Column(name = "department_uuid")
    private UUID departmentUuid;

    /** 부서명 (조회 편의용) */
    @Column(name = "department_name", length = 50)
    private String departmentName;

    /** 직급 (사원, 주임, 대리, 과장, 차장, 부장 등) */
    @Column(name = "position", length = 30)
    private String position;

    /** 직책 (팀장, 파트장 등) */
    @Column(name = "job_title", length = 50)
    private String jobTitle;

    /** 입사일 */
    @Column(name = "hire_date")
    private LocalDate hireDate;

    /** 이메일 */
    @Column(name = "email", length = 100)
    private String email;

    /** 휴대폰 번호 */
    @Column(name = "phone", length = 20)
    private String phone;

    /** 사내 전화번호 */
    @Column(name = "office_phone", length = 20)
    private String officePhone;

    /** 재직 상태 (ACTIVE, RESIGNED, ON_LEAVE) */
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}
