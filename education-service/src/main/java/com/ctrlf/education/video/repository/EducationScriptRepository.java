package com.ctrlf.education.video.repository;

import com.ctrlf.education.video.entity.EducationScript;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 교육 스크립트 저장소.
 */
public interface EducationScriptRepository extends JpaRepository<EducationScript, UUID> {
}


