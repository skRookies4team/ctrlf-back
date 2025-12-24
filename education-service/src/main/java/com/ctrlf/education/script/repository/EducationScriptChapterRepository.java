package com.ctrlf.education.script.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ctrlf.education.script.entity.EducationScriptChapter;

public interface EducationScriptChapterRepository extends JpaRepository<EducationScriptChapter, UUID> {
    java.util.List<EducationScriptChapter> findByScriptIdOrderByChapterIndexAsc(UUID scriptId);
}

