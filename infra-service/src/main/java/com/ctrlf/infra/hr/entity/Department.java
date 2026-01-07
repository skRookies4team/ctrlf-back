package com.ctrlf.infra.hr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 부서 정보 엔티티 (Q17)
 */
@Entity
@Table(name = "department", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 부서 코드 */
    @Column(name = "department_code", length = 20, nullable = false, unique = true)
    private String departmentCode;

    /** 부서명 */
    @Column(name = "department_name", length = 50, nullable = false)
    private String departmentName;

    /** 상위 부서 UUID */
    @Column(name = "parent_department_uuid")
    private UUID parentDepartmentUuid;

    /** 상위 부서명 (조회 편의용) */
    @Column(name = "parent_department_name", length = 50)
    private String parentDepartmentName;

    /** 부서장 UUID */
    @Column(name = "leader_uuid")
    private UUID leaderUuid;

    /** 부서장명 (조회 편의용) */
    @Column(name = "leader_name", length = 50)
    private String leaderName;

    /** 부서장 직급 */
    @Column(name = "leader_position", length = 30)
    private String leaderPosition;

    /** 부서 레벨 (1: 본부, 2: 팀, 3: 파트 등) */
    @Column(name = "level")
    private Integer level;

    /** 정렬 순서 */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** 활성화 상태 */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}
