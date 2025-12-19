package com.ctrlf.education.quiz.repository;

import com.ctrlf.education.quiz.entity.QuizAttempt;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    Optional<QuizAttempt> findTopByUserUuidAndEducationIdAndSubmittedAtIsNullOrderByCreatedAtDesc(UUID userUuid, UUID educationId);
    long countByUserUuidAndEducationId(UUID userUuid, UUID educationId);
}


