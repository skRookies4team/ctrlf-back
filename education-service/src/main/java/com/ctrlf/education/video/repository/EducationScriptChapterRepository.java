package com.ctrlf.education.video.repository;

import com.ctrlf.education.video.entity.EducationScriptChapter;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationScriptChapterRepository extends JpaRepository<EducationScriptChapter, UUID> {
    java.util.List<EducationScriptChapter> findByScriptIdOrderByChapterIndexAsc(UUID scriptId);
}

