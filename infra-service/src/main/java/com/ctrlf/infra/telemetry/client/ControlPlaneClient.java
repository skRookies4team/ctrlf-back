package com.ctrlf.infra.telemetry.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ControlPlaneClient {

    // ⚠️ 로컬이면 http://localhost:8000
    // 도커 네트워크면 http://ctrlf-ai-gateway:8000
    private final WebClient webClient = WebClient.create("http://localhost:8000");

    public void enableRagFallback() {
        send("RAG_FALLBACK", "true");
    }

    public void switchLlm(String provider) {
        send("LLM_SWITCH", provider);
    }

    public void enableRateLimit() {
        send("RATE_LIMIT", "emergency");
    }

    private void send(String command, String value) {
        webClient.post()
            .uri("/internal/control")
            .bodyValue(new ControlCommand(command, value))
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    public record ControlCommand(String command, String value) {}
}
