package com.ctrlf.education.script.entity;

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
 * 교육 스크립트 검수(리뷰) 엔티티.
 * 리뷰어와 상태/코멘트를 기록합니다.
 */
@Entity
@Table(name = "education_script_review", schema = "education")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationScriptReview {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    /** 대상 스크립트 ID */
    @Column(name = "script_id", columnDefinition = "uuid")
    private UUID scriptId;

    /** 리뷰어(검수자) UUID */
    @Column(name = "reviewer_uuid", columnDefinition = "uuid")
    private UUID reviewerUuid;

    /** 상태(예: PENDING/APPROVED/REJECTED 등) */
    @Column(name = "status")
    private String status;

    /** 검수 코멘트 */
    @Column(name = "comment", columnDefinition = "text")
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
    public static EducationScriptReview createRejection(UUID scriptId, String comment, UUID reviewerUuid) {
        EducationScriptReview review = new EducationScriptReview();
        review.setScriptId(scriptId);
        review.setStatus("REJECTED");
        review.setComment(comment);
        review.setReviewerUuid(reviewerUuid);
        return review;
    }

    /**
     * 승인 리뷰 생성 팩토리 메서드.
     */
    public static EducationScriptReview createApproval(UUID scriptId, UUID reviewerUuid) {
        EducationScriptReview review = new EducationScriptReview();
        review.setScriptId(scriptId);
        review.setStatus("APPROVED");
        review.setReviewerUuid(reviewerUuid);
        return review;
    }
}
