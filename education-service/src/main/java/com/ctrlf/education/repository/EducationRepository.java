package com.ctrlf.education.repository;

import com.ctrlf.education.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * 교육 엔티티 기본 저장소.
 * 목록 조회를 위한 네이티브 쿼리를 포함합니다.
 */
public interface EducationRepository extends JpaRepository<Education, UUID> {

    /**
     * 교육 목록(간략 정보) 조회.
     * - 사용자 UUID가 있을 경우 해당 사용자의 이수 여부를 함께 계산합니다.
     *
     * @param offset 오프셋
     * @param size 페이지 크기
     * @param completed 이수 여부 필터(null 허용)
     * @param year 연도 필터(null 허용)
     * @param category 카테고리 필터(null 허용)
     * @param userUuid 사용자 UUID(null 허용)
     * @return [id(UUID), title(String), required(Boolean), is_completed(Boolean)] 컬럼 순서의 Object[] 리스트
     */
    @Query(
        value = """
            SELECT 
              e.id,
              e.title,
              e.require AS required,
              CASE 
                WHEN :userUuid IS NOT NULL THEN COALESCE(
                  (SELECT ep.is_completed 
                     FROM education.education_progress ep 
                    WHERE ep.education_id = e.id 
                      AND ep.user_uuid = CAST(:userUuid AS uuid)
                  ), FALSE)
                ELSE FALSE
              END AS is_completed
            FROM education.education e
            WHERE e.deleted_at IS NULL
              AND (:year IS NULL OR EXTRACT(YEAR FROM e.created_at) = :year)
              AND (:category IS NULL OR e.category = :category)
              AND (
                    :completed IS NULL 
                 OR (:userUuid IS NOT NULL AND 
                     COALESCE(
                       (SELECT ep.is_completed 
                          FROM education.education_progress ep 
                         WHERE ep.education_id = e.id 
                           AND ep.user_uuid = CAST(:userUuid AS uuid)
                       ), FALSE) = :completed
                   )
              )
            ORDER BY e.created_at DESC NULLS LAST
            LIMIT :size OFFSET :offset
        """,
        nativeQuery = true
    )
    List<Object[]> findEducationsNative(
        @Param("offset") int offset,
        @Param("size") int size,
        @Param("completed") Boolean completed,
        @Param("year") Integer year,
        @Param("category") String category,
        @Param("userUuid") UUID userUuid
    );
}
