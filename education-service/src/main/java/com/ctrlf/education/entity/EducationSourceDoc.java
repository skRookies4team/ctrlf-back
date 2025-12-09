package com.ctrlf.education.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 교육 자료(문서) 메타 엔티티.
 * 업로더, 파일 경로/유형, 페이지 수 등을 기록합니다.
 */
@Entity
@Table(name = "education_source_doc", schema = "education")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationSourceDoc {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 교육 ID */
    @Column(name = "education_id", columnDefinition = "uuid")
    private UUID educationId;

    /** 업로더 사용자 UUID */
    @Column(name = "uploader_uuid", columnDefinition = "uuid")
    private UUID uploaderUuid;

    /** 파일 저장 URL */
    @Column(name = "file_url")
    private String fileUrl;

    /** 파일 유형(예: pdf, docx 등) */
    @Column(name = "file_type")
    private String fileType;

    /** 페이지 수(옵션) */
    @Column(name = "page_count")
    private Integer pageCount;

    /** 업로드 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;
}


