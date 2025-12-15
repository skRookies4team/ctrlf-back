package com.ctrlf.education.repository;

import com.ctrlf.education.entity.EducationScriptScene;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationScriptSceneRepository extends JpaRepository<EducationScriptScene, UUID> {
    java.util.List<EducationScriptScene> findByChapterIdOrderBySceneIndexAsc(UUID chapterId);
    java.util.List<EducationScriptScene> findByScriptIdOrderByChapterIdAscSceneIndexAsc(UUID scriptId);
}

