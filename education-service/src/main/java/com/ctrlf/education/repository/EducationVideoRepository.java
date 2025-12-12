package com.ctrlf.education.repository;

import com.ctrlf.education.entity.EducationVideo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 특정 교육의 모든 영상 소프트 삭제.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE education.education_video SET deleted_at = now() WHERE education_id = :educationId AND deleted_at IS NULL", nativeQuery = true)
    int softDeleteByEducationId(@Param("educationId") UUID educationId);
}


