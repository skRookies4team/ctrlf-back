package com.ctrlf.infra.telemetry.service;

import com.ctrlf.infra.telemetry.client.ControlPlaneClient;
import com.ctrlf.infra.telemetry.entity.AlertEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StrategyEngine {

    private final ControlPlaneClient controlPlaneClient;

    public void onCritical(AlertEventEntity alert) {
        // 1) RAG fallback
        controlPlaneClient.enableRagFallback();

        // 2) LLM provider switch
        controlPlaneClient.switchLlm("openai");

        // 3) rate limit emergency
        controlPlaneClient.enableRateLimit();
    }
}
