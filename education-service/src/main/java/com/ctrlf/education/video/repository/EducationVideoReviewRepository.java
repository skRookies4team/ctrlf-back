package com.ctrlf.education.video.repository;

import com.ctrlf.education.video.entity.EducationVideoReview;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 교육 영상 검수(리뷰) 저장소.
 */
public interface EducationVideoReviewRepository extends JpaRepository<EducationVideoReview, UUID> {
}


