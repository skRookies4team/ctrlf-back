package com.ctrlf.education.script.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ctrlf.education.script.entity.EducationScriptScene;

public interface EducationScriptSceneRepository extends JpaRepository<EducationScriptScene, UUID> {
    java.util.List<EducationScriptScene> findByChapterIdOrderBySceneIndexAsc(UUID chapterId);
    java.util.List<EducationScriptScene> findByScriptIdOrderByChapterIdAscSceneIndexAsc(UUID scriptId);
}

