package com.ctrlf.education.quiz.repository;

import com.ctrlf.education.quiz.entity.QuizQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    List<QuizQuestion> findByAttemptId(UUID attemptId);
}


