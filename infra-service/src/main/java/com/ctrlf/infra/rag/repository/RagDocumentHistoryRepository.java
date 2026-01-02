package com.ctrlf.infra.rag.repository;

import com.ctrlf.infra.rag.entity.RagDocumentHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RagDocumentHistoryRepository extends JpaRepository<RagDocumentHistory, UUID> {

    /**
     * 특정 문서 버전의 히스토리 조회 (최신순)
     */
    @Query("SELECT h FROM RagDocumentHistory h WHERE h.documentId = :documentId AND h.version = :version ORDER BY h.createdAt DESC")
    List<RagDocumentHistory> findByDocumentIdAndVersionOrderByCreatedAtDesc(
        @Param("documentId") String documentId,
        @Param("version") Integer version
    );
}

