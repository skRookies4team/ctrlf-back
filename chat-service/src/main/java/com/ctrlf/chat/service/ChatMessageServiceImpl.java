package com.ctrlf.chat.service;

import com.ctrlf.chat.dto.request.ChatMessageSendRequest;
import com.ctrlf.chat.dto.response.ChatMessageCursorResponse;
import com.ctrlf.chat.dto.response.ChatMessageSendResponse;
import com.ctrlf.chat.entity.ChatMessage;
import com.ctrlf.chat.repository.ChatMessageRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        ChatMessage userMessage = ChatMessage.userMessage(
            request.sessionId(),
            request.content()
        );
        chatMessageRepository.save(userMessage);

        ChatMessage assistantMessage = ChatMessage.assistantMessage(
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
    public ChatMessageCursorResponse getMessagesBySession(UUID sessionId, String cursor, int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int limit = safeSize + 1;

        Instant cursorCreatedAt = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            ParsedCursor parsed = ParsedCursor.parse(cursor);
            cursorCreatedAt = parsed.createdAt();
            cursorId = parsed.id();
        }

        // 최신 -> 과거 (DESC)
        List<ChatMessage> rows = chatMessageRepository.findNextPageBySessionId(
            sessionId,
            cursorCreatedAt,
            cursorId,
            limit
        );

        boolean hasNext = rows.size() > safeSize;
        List<ChatMessage> pageDesc = hasNext ? rows.subList(0, safeSize) : rows;

        String nextCursor = null;
        if (hasNext && !pageDesc.isEmpty()) {
            ChatMessage oldest = pageDesc.get(pageDesc.size() - 1); // DESC에서 마지막이 가장 과거
            nextCursor = ParsedCursor.encode(oldest.getCreatedAt(), oldest.getId());
        }

        // 클라이언트 표시용: 과거 -> 최신 (ASC)
        List<ChatMessage> pageAsc = new ArrayList<>(pageDesc);
        Collections.reverse(pageAsc);

        return new ChatMessageCursorResponse(pageAsc, nextCursor, hasNext);
    }

    @Override
    public ChatMessage retryMessage(UUID sessionId, UUID messageId) {
        ChatMessage base = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalStateException("Retry할 기준 메시지가 없습니다."));

        if (!sessionId.equals(base.getSessionId())) {
            throw new IllegalArgumentException("세션에 속하지 않은 메시지입니다.");
        }

        ChatMessage retry = ChatMessage.assistantMessage(
            sessionId,
            "Retry된 AI 응답입니다.",
            15,
            30,
            "gpt-4o-mini"
        );

        return chatMessageRepository.save(retry);
    }

    @Override
    public ChatMessage regenMessage(UUID sessionId, UUID messageId) {
        ChatMessage base = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalStateException("재생성할 기준 메시지가 없습니다."));

        if (!sessionId.equals(base.getSessionId())) {
            throw new IllegalArgumentException("세션에 속하지 않은 메시지입니다.");
        }

        ChatMessage regen = ChatMessage.assistantMessage(
            sessionId,
            "재생성된 AI 응답입니다.",
            20,
            40,
            "gpt-4o-mini"
        );

        return chatMessageRepository.save(regen);
    }

    /**
     * 커서 인코딩/파싱 유틸 (createdAtMillis_uuid)
     */
    private record ParsedCursor(Instant createdAt, UUID id) {
        static ParsedCursor parse(String cursor) {
            String[] parts = cursor.split("_", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("cursor 형식이 올바르지 않습니다.");
            }
            long millis = Long.parseLong(parts[0]);
            UUID id = UUID.fromString(parts[1]);
            return new ParsedCursor(Instant.ofEpochMilli(millis), id);
        }

        static String encode(Instant createdAt, UUID id) {
            return createdAt.toEpochMilli() + "_" + id;
        }
    }
}
