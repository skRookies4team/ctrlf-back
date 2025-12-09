package com.ctrlf.education.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 교육 메타 정보 엔티티.
 * 카테고리/필수 여부/통과 기준 및 부서 범위 등 기본 속성을 보관합니다.
 */
@Entity
@Table(name = "education", schema = "education")
@Getter
@Setter
@NoArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "title")
    private String title;

    /** 카테고리(예: MANDATORY, JOB, ETC) */
    @Column(name = "category")
    private String category;

    /** 대상 부서 범위(JSON 문자열; 부서 코드 배열을 직렬화) */
    @Column(name = "department_scope")
    private String departmentScope;

    @Column(name = "description")
    private String description;

    /** 통과 기준 점수 */
    @Column(name = "pass_score")
    private Integer passScore;

    /** 통과 기준 비율 */
    @Column(name = "pass_ratio")
    private Integer passRatio;

    /** 필수 교육 여부 */
    @Column(name = "require")
    private Boolean require;

    /** 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 최근 수정 시각 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}


