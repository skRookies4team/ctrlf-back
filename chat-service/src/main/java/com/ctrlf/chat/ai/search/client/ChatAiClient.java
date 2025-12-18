package com.ctrlf.chat.ai.search.client;

import com.ctrlf.chat.ai.search.dto.ChatAiMessage;
import com.ctrlf.chat.ai.search.dto.ChatAiRequest;
import com.ctrlf.chat.ai.search.dto.ChatAiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAiClient {

    private final WebClient aiWebClient;

    public ChatAiResponse ask(
        UUID sessionId,
        UUID userId,
        String userRole,
        String department,
        String domain,
        String channel,
        String message
    ) {
        ChatAiRequest request =
            new ChatAiRequest(
                sessionId,
                userId,
                userRole,
                department,
                domain,
                channel,
                List.of(
                    new ChatAiMessage("user", message)
                )
            );

        log.info("[AI] request -> {}", request);

        ChatAiResponse response =
            aiWebClient.post()
                .uri("/ai/chat/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatAiResponse.class)
                .block();

        log.info("[AI] response <- {}", response);
        return response;
    }
}
