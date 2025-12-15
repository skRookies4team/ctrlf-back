package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatSessionCreateRequest;
import com.ctrlf.chat.dto.request.ChatSessionUpdateRequest;
import com.ctrlf.chat.dto.response.ChatSessionHistoryResponse;
import com.ctrlf.chat.dto.response.ChatSessionResponse;
import com.ctrlf.chat.entity.ChatMessage;
import com.ctrlf.chat.entity.ChatSession;
import com.ctrlf.chat.exception.chat.ChatSessionNotFoundException;
import com.ctrlf.chat.repository.ChatMessageRepository;
import com.ctrlf.chat.repository.ChatSessionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public ChatSessionResponse createSession(ChatSessionCreateRequest request) {
        ChatSession session = new ChatSession();
        session.setTitle(request.title());
        session.setDomain(request.domain());
        session.setUserUuid(request.userUuid());

        ChatSession saved = chatSessionRepository.save(session);

        return new ChatSessionResponse(
            saved.getId(),
            saved.getTitle(),
            saved.getDomain(),
            saved.getUserUuid(),
            saved.getCreatedAt(),
            saved.getUpdatedAt()
        );
    }

    @Override
    public ChatSessionResponse getSession(UUID sessionId) {
        ChatSession session = chatSessionRepository.findActiveById(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException();
        }

        return new ChatSessionResponse(
            session.getId(),
            session.getTitle(),
            session.getDomain(),
            session.getUserUuid(),
            session.getCreatedAt(),
            session.getUpdatedAt()
        );
    }

    @Override
    public List<ChatSessionResponse> getSessionList() {
        return chatSessionRepository.findAllActive()
            .stream()
            .map(session -> new ChatSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDomain(),
                session.getUserUuid(),
                session.getCreatedAt(),
                session.getUpdatedAt()
            ))
            .toList();
    }

    @Override
    public ChatSessionResponse updateSession(UUID sessionId, ChatSessionUpdateRequest request) {
        ChatSession session = chatSessionRepository.findActiveById(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException();
        }

        session.updateTitle(request.title());

        return new ChatSessionResponse(
            session.getId(),
            session.getTitle(),
            session.getDomain(),
            session.getUserUuid(),
            session.getCreatedAt(),
            session.getUpdatedAt()
        );
    }

    @Override
    public void deleteSession(UUID sessionId) {
        ChatSession session = chatSessionRepository.findActiveById(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException();
        }
        session.softDelete();
    }

    // ✅ 세션 히스토리(전체) 조회 유지
    @Override
    public ChatSessionHistoryResponse getSessionHistory(UUID sessionId) {
        ChatSession session = chatSessionRepository.findActiveById(sessionId);
        if (session == null) {
            throw new ChatSessionNotFoundException();
        }

        List<ChatMessage> messages =
            chatMessageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);

        return new ChatSessionHistoryResponse(
            session.getId(),
            session.getTitle(),
            messages
        );
    }
}
