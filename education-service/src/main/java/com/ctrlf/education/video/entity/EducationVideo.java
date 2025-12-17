package com.ctrlf.education.video.entity;

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
 * 교육 영상 엔티티.
 * 생성 작업 결과로 만들어진 영상의 메타데이터를 보관합니다.
 */
@Entity
@Table(name = "education_video", schema = "education")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 교육 ID */
    @Column(name = "education_id", columnDefinition = "uuid")
    private UUID educationId;

    /** 생성 작업(Job) ID */
    @Column(name = "generation_job_id", columnDefinition = "uuid")
    private UUID generationJobId;

    /** 영상 파일 URL */
    @Column(name = "file_url")
    private String fileUrl;

    /** 영상 버전 */
    @Column(name = "version")
    private Integer version;

    /** 영상 길이(초) */
    @Column(name = "duration")
    private Integer duration;

    /** 상태(예: READY/PROCESSING/FAILED/ACTIVE 등) */
    @Column(name = "status")
    private String status;

    /** 대상 부서 코드 */
    @Column(name = "target_dept_code")
    private String targetDeptCode;

    /** 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 재생/표시 순서를 위한 인덱스 (교육 내 0-base) */
    @Column(name = "order_index")
    private Integer orderIndex;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * 시드/유틸용 생성 팩토리.
     */
    public static EducationVideo create(
        UUID educationId,
        String fileUrl,
        Integer duration,
        String targetDeptCode,
        Integer version,
        String status
    ) {
        EducationVideo v = new EducationVideo();
        v.setEducationId(educationId);
        v.setFileUrl(fileUrl);
        v.setDuration(duration);
        v.setTargetDeptCode(targetDeptCode);
        v.setVersion(version);
        v.setStatus(status);
        v.setOrderIndex(0);
        return v;
    }
}


