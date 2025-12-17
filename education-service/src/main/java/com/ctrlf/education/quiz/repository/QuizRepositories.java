package com.ctrlf.education.quiz.repository;

import com.ctrlf.education.quiz.entity.QuizAttempt;
import com.ctrlf.education.quiz.entity.QuizLeaveTracking;
import com.ctrlf.education.quiz.entity.QuizQuestion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    Optional<QuizAttempt> findTopByUserUuidAndEducationIdAndSubmittedAtIsNullOrderByCreatedAtDesc(UUID userUuid, UUID educationId);
    long countByUserUuidAndEducationId(UUID userUuid, UUID educationId);
}

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    List<QuizQuestion> findByAttemptId(UUID attemptId);
}

public interface QuizLeaveTrackingRepository extends JpaRepository<QuizLeaveTracking, UUID> {
    Optional<QuizLeaveTracking> findByAttemptId(UUID attemptId);
}

