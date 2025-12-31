package com.ctrlf.chat.ai.search.client;

import com.ctrlf.chat.ai.search.dto.ChatAiMessage;
import com.ctrlf.chat.ai.search.dto.ChatAiRequest;
import com.ctrlf.chat.ai.search.dto.ChatAiResponse;
import com.ctrlf.chat.util.TraceIdUtil;
// ⚠️ session-summary 기능 주석 처리로 인해 사용 안 함
// import com.ctrlf.chat.dto.summary.ChatSessionSummaryMessage;
// import com.ctrlf.chat.dto.summary.ChatSessionSummaryRequest;
// import com.ctrlf.chat.dto.summary.ChatSessionSummaryResponse;
// import com.ctrlf.chat.entity.ChatMessage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAiClient {

    private final WebClient aiWebClient;

    @Value("${app.internal.token:}")
    private String internalToken;

    // ✅ 기존 채팅 응답용 (헤더 전파 추가)
    public ChatAiResponse ask(
        UUID sessionId,
        UUID userId,
        String userRole,
        String department,
        String domain,
        String channel,
        String message,
        UUID conversationId,
        Integer turnId
    ) {
        ChatAiRequest request =
            new ChatAiRequest(
                sessionId,
                userId,
                userRole,
                department,
                domain,
                channel,
                List.of(new ChatAiMessage("user", message))
            );

        // Trace ID 생성 (요청마다 새로 생성)
        UUID traceId = TraceIdUtil.generateTraceId();

        // 헤더 전파: X-Trace-Id, X-User-Id, X-Dept-Id, X-Conversation-Id, X-Turn-Id
        WebClient.RequestBodySpec requestSpec = aiWebClient.post()
            .uri("/ai/chat/messages")
            .header("X-Trace-Id", traceId.toString())
            .header("X-User-Id", userId != null ? userId.toString() : "")
            .header("X-Dept-Id", department != null ? department : "")
            .header("Content-Type", "application/json");

        // X-Conversation-Id (권장)
        if (conversationId != null) {
            requestSpec = requestSpec.header("X-Conversation-Id", conversationId.toString());
        }

        // X-Turn-Id (권장)
        if (turnId != null) {
            requestSpec = requestSpec.header("X-Turn-Id", turnId.toString());
        }

        // X-Internal-Token (내부 서비스 간 통신용)
        if (internalToken != null && !internalToken.isBlank()) {
            requestSpec = requestSpec.header("X-Internal-Token", internalToken);
        }

        return requestSpec
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatAiResponse.class)
            .block();
    }

    // 하위 호환성을 위한 오버로드 메서드
    public ChatAiResponse ask(
        UUID sessionId,
        UUID userId,
        String userRole,
        String department,
        String domain,
        String channel,
        String message
    ) {
        // conversationId는 sessionId를 사용, turnId는 null
        return ask(sessionId, userId, userRole, department, domain, channel, message, sessionId, null);
    }

    // ⚠️ 세션 요약 전용 (현재 AI 서비스에 해당 엔드포인트가 없어 주석 처리)
    // AI 서비스에는 FAQ 관련 API만 제공되며, session-summary 엔드포인트는 구현되지 않음
    /*
    public ChatSessionSummaryResponse summarizeSession(
        UUID sessionId,
        List<ChatMessage> messages
    ) {
        ChatSessionSummaryRequest request =
            new ChatSessionSummaryRequest(
                sessionId,
                messages.stream()
                    .map(ChatSessionSummaryMessage::from)
                    .toList(),
                150
            );

        log.info("[AI][SUMMARY] request -> {}", request);

        return aiWebClient.post()
            .uri("/ai/chat/session-summary")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatSessionSummaryResponse.class)
            .block();
    }
    */
}
