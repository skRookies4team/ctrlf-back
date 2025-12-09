package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;
import com.ctrlf.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    // ✅ 메시지 전송
    @Override
    public ChatMessageSendResponse sendMessage(
        ChatMessageSendRequest request,
        UUID userId,
        String domain
    ) {
        // USER 메시지 저장
        ChatMessage userMessage = ChatMessage.userMessage(
            request.sessionId(),
            request.sectionId(),
            request.content()
        );
        chatMessageRepository.save(userMessage);

        // 임시 AI 응답
        String aiAnswer = "임시 AI 응답입니다.";

        ChatMessage assistantMessage = ChatMessage.assistantMessage(
            request.sessionId(),
            request.sectionId(),
            aiAnswer,
            10,
            20,
            "gpt-4o-mini"
        );
        chatMessageRepository.save(assistantMessage);

        return new ChatMessageSendResponse(
            assistantMessage.getId(),
            assistantMessage.getRole(),
            assistantMessage.getContent(),
            assistantMessage.getCreatedAt()
        );
    }

    // ✅ 메시지 조회
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(UUID sessionId, UUID sectionId) {
        return chatMessageRepository
            .findBySessionIdAndSectionIdOrderByCreatedAtAsc(sessionId, sectionId);
    }

    // ✅ Retry
    @Override
    public ChatMessage retryMessage(UUID sessionId, UUID sectionId) {

        ChatMessage latest = chatMessageRepository
            .findTopBySessionIdAndSectionIdOrderByCreatedAtDesc(sessionId, sectionId)
            .orElseThrow(() -> new IllegalStateException("Retry할 메시지가 없습니다."));

        ChatMessage retryMessage = ChatMessage.assistantMessage(
            sessionId,
            sectionId,
            "Retry된 AI 응답입니다.",
            15,
            30,
            "gpt-4o-mini"
        );

        return chatMessageRepository.save(retryMessage);
    }
}
