package com.ctrlf.education.repository;

import com.ctrlf.education.entity.EducationVideoProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 교육 영상 진행률 저장소.
 */
public interface EducationVideoProgressRepository extends JpaRepository<EducationVideoProgress, UUID> {
    /**
     * 사용자/교육/영상 기준의 단일 진행률을 조회합니다.
     */
    Optional<EducationVideoProgress> findByUserUuidAndEducationIdAndVideoId(UUID userUuid, UUID educationId, UUID videoId);
    /**
     * 사용자/교육 기준의 전체 영상 진행률 목록을 조회합니다.
     */
    List<EducationVideoProgress> findByUserUuidAndEducationId(UUID userUuid, UUID educationId);
    /**
     * 특정 교육의 모든 진행 이력을 삭제합니다.
     */
    void deleteByEducationId(UUID educationId);
}


