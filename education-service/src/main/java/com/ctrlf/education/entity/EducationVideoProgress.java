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
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 교육 영상 시청 진행 엔티티.
 * 사용자별 특정 영상의 진행률/시청 위치/누적 시청 시간을 기록합니다.
 */
@Entity
@Table(name = "education_video_progress", schema = "education")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationVideoProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 사용자 UUID */
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUuid;

    /** 교육 ID */
    @Column(name = "education_id", columnDefinition = "uuid")
    private UUID educationId;

    /** 영상 ID */
    @Column(name = "video_id", columnDefinition = "uuid")
    private UUID videoId;

    /** 진행률(%) */
    @Column(name = "progress")
    private Integer progress;

    /** 마지막 시청 위치(초) */
    @Column(name = "last_position_seconds")
    private Integer lastPositionSeconds;

    /** 누적 시청 시간(초) */
    @Column(name = "total_watch_seconds")
    private Integer totalWatchSeconds;

    /** 완료 여부 */
    @Column(name = "is_completed")
    private Boolean isCompleted;

    /** 최근 업데이트 시각 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** 최초 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static EducationVideoProgress create(UUID userUuid, UUID educationId, UUID videoId) {
        EducationVideoProgress p = new EducationVideoProgress();
        p.setUserUuid(userUuid);
        p.setEducationId(educationId);
        p.setVideoId(videoId);
        return p;
    }
}


