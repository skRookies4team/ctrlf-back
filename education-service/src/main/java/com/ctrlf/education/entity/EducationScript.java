package com.ctrlf.education.entity;

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
import org.hibernate.annotations.CreationTimestamp;

/**
 * 교육 스크립트 엔티티.
 * 소스 문서로부터 생성된 스크립트 버전과 본문을 보관합니다.
 */
@Entity
@Table(name = "education_script", schema = "education")
@Getter
@Setter
@NoArgsConstructor
public class EducationScript {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 교육 ID */
    @Column(name = "education_id", columnDefinition = "uuid")
    private UUID educationId;

    /** 연계된 원본 자료 ID */
    @Column(name = "source_doc_id", columnDefinition = "uuid")
    private UUID sourceDocId;

    /** 스크립트 버전 */
    @Column(name = "version")
    private Integer version;

    /** 스크립트 본문 */
    @Column(name = "content")
    private String content;

    /** 작성자 사용자 UUID */
    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    /** 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}


