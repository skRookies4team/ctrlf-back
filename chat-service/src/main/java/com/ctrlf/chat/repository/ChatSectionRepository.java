package com.ctrlf.chat.repository;

import com.ctrlf.chat.entity.ChatSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSectionRepository extends JpaRepository<ChatSection, UUID> {

    // ✅ 세션에 속한 섹션 목록 조회
    List<ChatSection> findBySessionId(UUID sessionId);

    // ✅ 섹션 종료용 단건 조회
    Optional<ChatSection> findBySessionIdAndId(UUID sessionId, UUID id);
}
