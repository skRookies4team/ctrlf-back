package com.ctrlf.education.quiz.repository;

import com.ctrlf.education.quiz.entity.QuizLeaveTracking;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizLeaveTrackingRepository extends JpaRepository<QuizLeaveTracking, UUID> {
    Optional<QuizLeaveTracking> findByAttemptId(UUID attemptId);
}


