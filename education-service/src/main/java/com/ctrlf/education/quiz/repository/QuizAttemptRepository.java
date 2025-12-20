package com.ctrlf.education.quiz.repository;

import com.ctrlf.education.quiz.entity.QuizAttempt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    Optional<QuizAttempt> findTopByUserUuidAndEducationIdAndSubmittedAtIsNullOrderByCreatedAtDesc(UUID userUuid, UUID educationId);
    long countByUserUuidAndEducationId(UUID userUuid, UUID educationId);
    
    /** 사용자별 제출 완료된 퀴즈 시도 목록 조회 */
    List<QuizAttempt> findByUserUuidAndSubmittedAtIsNotNullOrderByCreatedAtDesc(UUID userUuid);
    
    /** 교육별 사용자의 제출 완료된 퀴즈 시도 목록 조회 (재응시 시 이전 문항 제외용) */
    List<QuizAttempt> findByUserUuidAndEducationIdAndSubmittedAtIsNotNullOrderByCreatedAtDesc(UUID userUuid, UUID educationId);
    
    /** 교육별 사용자의 최고 점수 시도 조회 */
    @Query("SELECT a FROM QuizAttempt a WHERE a.userUuid = :userUuid AND a.educationId = :educationId AND a.submittedAt IS NOT NULL ORDER BY COALESCE(a.score, 0) DESC, a.createdAt DESC")
    List<QuizAttempt> findTopByUserUuidAndEducationIdOrderByScoreDesc(@Param("userUuid") UUID userUuid, @Param("educationId") UUID educationId);
}


