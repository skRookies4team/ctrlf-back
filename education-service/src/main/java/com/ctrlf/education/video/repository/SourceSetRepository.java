package com.ctrlf.education.video.repository;

import com.ctrlf.education.video.entity.SourceSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceSetRepository extends JpaRepository<SourceSet, UUID> {

    /**
     * ID로 소스셋 조회 (삭제되지 않은 것만).
     */
    @Query("SELECT ss FROM SourceSet ss WHERE ss.id = :id AND ss.deletedAt IS NULL")
    Optional<SourceSet> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * 교육 ID로 소스셋 조회 (삭제되지 않은 것만).
     */
    @Query("SELECT ss FROM SourceSet ss WHERE ss.educationId = :educationId AND ss.deletedAt IS NULL")
    List<SourceSet> findByEducationIdAndNotDeleted(@Param("educationId") UUID educationId);

    /**
     * 비디오 ID로 소스셋 조회 (삭제되지 않은 것만).
     */
    @Query("SELECT ss FROM SourceSet ss WHERE ss.videoId = :videoId AND ss.deletedAt IS NULL")
    List<SourceSet> findByVideoIdAndNotDeleted(@Param("videoId") UUID videoId);
}
