package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatSectionCreateRequest;
import com.ctrlf.chat.dto.response.ChatSectionResponse;
import com.ctrlf.chat.entity.ChatSection;
import com.ctrlf.chat.repository.ChatSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatSectionServiceImpl implements ChatSectionService {

    private final ChatSectionRepository chatSectionRepository;

    // ✅ 섹션 생성
    @Override
    public ChatSectionResponse createSection(UUID sessionId, ChatSectionCreateRequest request) {
        ChatSection section = new ChatSection();
        section.setSessionId(sessionId);
        section.setTitle(request.title());
        section.setSummary(null);           // ✅ 요약은 나중에 LLM으로 채움
        section.setRetryCount(0);           // ✅ 최초 0
        section.setCreatedAt(Instant.now());
        section.setClosedAt(null);

        ChatSection saved = chatSectionRepository.save(section);

        return new ChatSectionResponse(
            saved.getId(),
            saved.getTitle(),
            saved.getSummary(),
            saved.getRetryCount(),
            saved.getCreatedAt(),
            saved.getClosedAt()
        );
    }

    // ✅ 섹션 목록 조회
    @Override
    public List<ChatSectionResponse> getSectionList(UUID sessionId) {
        return chatSectionRepository.findBySessionId(sessionId)
            .stream()
            .map(section -> new ChatSectionResponse(
                section.getId(),
                section.getTitle(),
                section.getSummary(),
                section.getRetryCount(),
                section.getCreatedAt(),
                section.getClosedAt()
            ))
            .toList();
    }

    // ✅ 섹션 종료
    @Override
    public void closeSection(UUID sessionId, UUID sectionId) {
        ChatSection section = chatSectionRepository
            .findById(sectionId)
            .orElseThrow(() -> new IllegalArgumentException("섹션이 존재하지 않습니다."));

        section.setClosedAt(Instant.now());
    }
}
