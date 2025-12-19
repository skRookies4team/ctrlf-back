package com.ctrlf.education.repository;

import com.ctrlf.education.entity.EducationProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 교육 진행 현황 저장소.
 */
public interface EducationProgressRepository extends JpaRepository<EducationProgress, UUID> {
    /** 사용자별 이수 완료한 교육 진행 현황 조회 */
    List<EducationProgress> findByUserUuidAndIsCompletedTrue(UUID userUuid);
    
    /** 사용자별 특정 교육의 진행 현황 조회 */
    Optional<EducationProgress> findByUserUuidAndEducationId(UUID userUuid, UUID educationId);
}


