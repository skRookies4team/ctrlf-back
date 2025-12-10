package com.ctrlf.education.repository;

import com.ctrlf.education.entity.EducationProgress;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 교육 진행 현황 저장소.
 */
public interface EducationProgressRepository extends JpaRepository<EducationProgress, UUID> {
}


