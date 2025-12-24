package com.ctrlf.infra.rag.repository;

import com.ctrlf.infra.rag.entity.RagFailChunk;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RagFailChunkRepository extends JpaRepository<RagFailChunk, UUID> {

    @Transactional
    void deleteByDocumentId(UUID documentId);

    /**
     * 문서 ID와 청크 인덱스로 실패 청크 조회 (멱등 처리용).
     */
    Optional<RagFailChunk> findByDocumentIdAndChunkIndex(UUID documentId, Integer chunkIndex);
}

