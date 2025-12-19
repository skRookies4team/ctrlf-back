package com.ctrlf.education.video.repository;

import com.ctrlf.education.video.entity.VideoGenerationJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 영상 생성 작업(Job) 저장소.
 */
public interface VideoGenerationJobRepository extends JpaRepository<VideoGenerationJob, UUID> {
}


