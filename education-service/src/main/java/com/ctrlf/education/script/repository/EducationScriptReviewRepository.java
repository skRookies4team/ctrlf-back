package com.ctrlf.education.script.repository;

import com.ctrlf.education.script.entity.EducationScriptReview;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 교육 스크립트 검수(리뷰) 리포지토리.
 */
@Repository
public interface EducationScriptReviewRepository extends JpaRepository<EducationScriptReview, UUID> {
    
    /**
     * 스크립트 ID로 리뷰 목록 조회 (삭제되지 않은 것만).
     */
    List<EducationScriptReview> findByScriptIdAndDeletedAtIsNull(UUID scriptId);
    
    /**
     * 스크립트 ID와 상태로 리뷰 조회 (삭제되지 않은 것만).
     */
    List<EducationScriptReview> findByScriptIdAndStatusAndDeletedAtIsNull(UUID scriptId, String status);
}
