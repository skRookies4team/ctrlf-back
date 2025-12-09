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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    // ✅ 세션 생성
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

    // ✅ 세션 단건 조회
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

    // ✅ 세션 목록 조회
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

    // ✅ 세션 수정
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

    // ✅ 세션 삭제
    @Override
    public void deleteSession(UUID sessionId) {
        ChatSession session = chatSessionRepository.findActiveById(sessionId);

        if (session == null) {
            throw new ChatSessionNotFoundException();
        }

        session.softDelete();
    }

    // ✅ ✅ ✅ 세션 히스토리 조회 (최종 정상 버전)
    @Override
    public ChatSessionHistoryResponse getSessionHistory(UUID sessionId) {
        ChatSession session = chatSessionRepository.findActiveById(sessionId);

        if (session == null) {
            throw new ChatSessionNotFoundException();
        }

        // ✅ 현재 존재하는 Repository 메서드 사용
        List<ChatMessage> messages =
            chatMessageRepository.findAllBySessionId(sessionId);

        return new ChatSessionHistoryResponse(
            session.getId(),
            session.getTitle(),
            messages
        );
    }
}
