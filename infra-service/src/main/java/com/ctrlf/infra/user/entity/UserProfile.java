package com.ctrlf.infra.entity;

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

@Entity
@Table(name = "user_profile", schema = "infra")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    /** 프로필 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** Keycloak 사용자 UUID(sub), 고유 제약 */
    @Column(name = "user_uuid", columnDefinition = "uuid", unique = true)
    private UUID userUuid;

    /** 사번 또는 회사 로그인 ID */
    @Column(name = "employee_id", length = 50)
    private String employeeId;

    /** 부서명 */
    @Column(name = "department", length = 100)
    private String department;

    /** 직급(사원/대리/과장 등) */
    @Column(name = "position", length = 50)
    private String position;

    /** 성별(선택) */
    @Column(name = "gender", length = 10)
    private String gender;

    /** 나이(선택) */
    @Column(name = "age")
    private Integer age;

    /** 생성 시각 */
    @Column(name = "created_at")
    private Instant createdAt;

    /** 수정 시각 */
    @Column(name = "updated_at")
    private Instant updatedAt;
}

