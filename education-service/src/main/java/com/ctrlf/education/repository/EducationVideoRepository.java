package com.ctrlf.education.repository;

import com.ctrlf.education.entity.EducationVideo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 교육 영상 저장소.
 */
public interface EducationVideoRepository extends JpaRepository<EducationVideo, UUID> {
    /**
     * 특정 교육에 속한 영상 목록 조회.
     */
    List<EducationVideo> findByEducationId(UUID educationId);
    /**
     * 특정 교육의 모든 영상을 삭제.
     */
    void deleteByEducationId(UUID educationId);
}


