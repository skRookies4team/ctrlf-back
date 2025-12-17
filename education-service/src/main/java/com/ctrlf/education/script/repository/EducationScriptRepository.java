package com.ctrlf.education.script.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ctrlf.education.script.entity.EducationScript;

/**
 * 교육 스크립트 저장소.
 */
public interface EducationScriptRepository extends JpaRepository<EducationScript, UUID> {
}


