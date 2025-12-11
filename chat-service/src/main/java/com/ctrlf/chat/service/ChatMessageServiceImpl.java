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

    @Override
    public ChatMessageSendResponse sendMessage(
        ChatMessageSendRequest request,
        UUID userId,
        String domain
    ) {
        // 1) User 메시지 저장
        ChatMessage userMessage =
            ChatMessage.userMessage(request.sessionId(), request.content());
        chatMessageRepository.save(userMessage);

        // 2) 임시 AI 응답 저장
        ChatMessage assistantMessage =
            ChatMessage.assistantMessage(
                request.sessionId(),
                "임시 AI 응답입니다.",
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

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesBySession(UUID sessionId) {
        return chatMessageRepository
            .findAllBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Override
    public ChatMessage retryMessage(UUID sessionId) {
        chatMessageRepository.findTopBySessionIdOrderByCreatedAtDesc(sessionId)
            .orElseThrow(() -> new IllegalStateException("Retry할 메시지가 없습니다."));

        ChatMessage retryMessage = ChatMessage.assistantMessage(
            sessionId,
            "Retry된 AI 응답입니다.",
            15,
            30,
            "gpt-4o-mini"
        );

        return chatMessageRepository.save(retryMessage);
    }

    @Override
    public ChatMessage regenMessage(UUID sessionId) {
        chatMessageRepository.findTopBySessionIdOrderByCreatedAtDesc(sessionId)
            .orElseThrow(() -> new IllegalStateException("재생성할 메시지가 없습니다."));

        ChatMessage regenMessage = ChatMessage.assistantMessage(
            sessionId,
            "재생성된 AI 응답입니다.",
            20,
            40,
            "gpt-4o-mini"
        );

        return chatMessageRepository.save(regenMessage);
    }
}
