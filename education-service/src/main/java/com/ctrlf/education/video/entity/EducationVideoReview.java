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
 * 교육 영상 검수(리뷰) 엔티티.
 * 리뷰어와 상태/코멘트를 기록합니다.
 */
@Entity
@Table(name = "education_video_review", schema = "education")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationVideoReview {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 영상 ID */
    @Column(name = "video_id", columnDefinition = "uuid")
    private UUID videoId;

    /** 리뷰어(검수자) UUID */
    @Column(name = "reviewer_uuid", columnDefinition = "uuid")
    private UUID reviewerUuid;

    /** 상태(예: PENDING/APPROVED/REJECTED 등) */
    @Column(name = "status")
    private String status;

    /** 검수 코멘트 */
    @Column(name = "comment")
    private String comment;

    /** 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    /** 삭제(소프트딜리트) 시각 */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * 반려 리뷰 생성 팩토리 메서드.
     */
    public static EducationVideoReview createRejection(UUID videoId, String comment, UUID reviewerUuid) {
        EducationVideoReview review = new EducationVideoReview();
        review.setVideoId(videoId);
        review.setStatus("REJECTED");
        review.setComment(comment);
        review.setReviewerUuid(reviewerUuid);
        return review;
    }
}


