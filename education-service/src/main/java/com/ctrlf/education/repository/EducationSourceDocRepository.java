package com.ctrlf.education.repository;

import com.ctrlf.education.entity.EducationSourceDoc;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 교육 자료(문서) 저장소.
 */
public interface EducationSourceDocRepository extends JpaRepository<EducationSourceDoc, UUID> {
    /**
     * 특정 교육의 자료 목록 조회.
     */
    List<EducationSourceDoc> findByEducationId(UUID educationId);
    /**
     * 특정 교육의 자료 전체 삭제.
     */
    void deleteByEducationId(UUID educationId);
}


