package com.ctrlf.chat.service;

import com.ctrlf.chat.ai.search.dto.ChatCompletionResponse;
import com.ctrlf.chat.ai.search.facade.ChatAiFacade;
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
    private final ChatAiFacade chatAiFacade;

    @Override
    public ChatMessageSendResponse sendMessage(
        ChatMessageSendRequest request,
        UUID userId,
        String domain
    ) {
        // 1️⃣ 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.userMessage(
            request.sessionId(),
            request.content()
        );
        chatMessageRepository.save(userMessage);

        // 2️⃣ AI 서버 → FastAPI → (현재는 Dummy 응답)
        ChatCompletionResponse aiResponse =
            chatAiFacade.chat(
                request.sessionId(),
                userId,
                domain,
                request.content()
            );

        // 3️⃣ AI 메시지 저장
        ChatMessage assistantMessage = ChatMessage.assistantMessage(
            request.sessionId(),
            aiResponse.getAnswer(),
            null,   // promptTokens (아직 없음)
            null,   // completionTokens (아직 없음)
            aiResponse.getMeta() != null
                ? aiResponse.getMeta().getUsed_model()
                : "unknown"
        );
        chatMessageRepository.save(assistantMessage);

        return new ChatMessageSendResponse(
            assistantMessage.getId(),
            assistantMessage.getRole(),
            assistantMessage.getContent(),
            assistantMessage.getCreatedAt()
        );
    }

    // ===== 이하 기존 코드 그대로 =====

    @Override
    @Transactional(readOnly = true)
    public ChatMessageCursorResponse getMessagesBySession(
        UUID sessionId,
        String cursor,
        int size
    ) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int limit = safeSize + 1;

        Instant cursorCreatedAt = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            ParsedCursor parsed = ParsedCursor.parse(cursor);
            cursorCreatedAt = parsed.createdAt();
            cursorId = parsed.id();
        }

        List<ChatMessage> rows =
            chatMessageRepository.findNextPageBySessionId(
                sessionId,
                cursorCreatedAt,
                cursorId,
                limit
            );

        boolean hasNext = rows.size() > safeSize;
        List<ChatMessage> pageDesc =
            hasNext ? rows.subList(0, safeSize) : rows;

        String nextCursor = null;
        if (hasNext && !pageDesc.isEmpty()) {
            ChatMessage oldest =
                pageDesc.get(pageDesc.size() - 1);
            nextCursor =
                ParsedCursor.encode(
                    oldest.getCreatedAt(),
                    oldest.getId()
                );
        }

        List<ChatMessage> pageAsc =
            new ArrayList<>(pageDesc);
        Collections.reverse(pageAsc);

        return new ChatMessageCursorResponse(
            pageAsc,
            nextCursor,
            hasNext
        );
    }

    @Override
    public ChatMessage retryMessage(UUID sessionId, UUID messageId) {
        throw new UnsupportedOperationException("Retry는 다음 단계에서 구현");
    }

    @Override
    public ChatMessage regenMessage(UUID sessionId, UUID messageId) {
        throw new UnsupportedOperationException("Regen은 다음 단계에서 구현");
    }

    private record ParsedCursor(
        Instant createdAt,
        UUID id
    ) {
        static ParsedCursor parse(String cursor) {
            String[] parts = cursor.split("_", 2);
            long millis = Long.parseLong(parts[0]);
            UUID id = UUID.fromString(parts[1]);
            return new ParsedCursor(
                Instant.ofEpochMilli(millis),
                id
            );
        }

        static String encode(Instant createdAt, UUID id) {
            return createdAt.toEpochMilli() + "_" + id;
        }
    }
}
