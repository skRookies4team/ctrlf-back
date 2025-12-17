package com.ctrlf.chat.ai.search.facade;

import com.ctrlf.chat.ai.search.dto.ChatCompletionRequest;
import com.ctrlf.chat.ai.search.dto.ChatCompletionRequest.Message;
import com.ctrlf.chat.ai.search.dto.ChatCompletionResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatAiFacade {

    private final WebClient aiWebClient;

    public ChatCompletionResponse chat(
        UUID sessionId,
        UUID userId,
        String domain,
        String userMessage
    ) {
        ChatCompletionRequest request =
            new ChatCompletionRequest(
                sessionId,
                userId,
                "EMPLOYEE",   // TODO: JWT role 연동
                domain,       // department
                domain,
                "WEB",
                List.of(
                    new Message("user", userMessage)
                )
            );

        log.info("[CHAT → AI] session={}, user={}, domain={}",
            sessionId, userId, domain);

        ChatCompletionResponse response = aiWebClient.post()
            .uri("/ai/chat/messages")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatCompletionResponse.class)
            .block();

        log.info("[CHAT ← AI] {}", response.getAnswer());

        return response;
    }
}
