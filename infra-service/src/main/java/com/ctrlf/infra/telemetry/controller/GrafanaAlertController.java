package com.ctrlf.infra.telemetry.controller;

import com.ctrlf.infra.telemetry.domain.AlertEvent;
import com.ctrlf.infra.telemetry.service.AlertHandlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class GrafanaAlertController {

    private final AlertHandlingService alertHandlingService;

    @PostMapping("/grafana")
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        log.warn("🚨 Grafana alert received: {}", payload);

        Map<String, String> labels = (Map<String, String>) payload.getOrDefault("commonLabels", Map.of());
        Map<String, String> annotations = (Map<String, String>) payload.getOrDefault("commonAnnotations", Map.of());

        AlertEvent event = new AlertEvent(
            "GRAFANA",
            (String) payload.getOrDefault("status", "UNKNOWN"),
            (String) payload.getOrDefault("title", "Grafana Alert"),
            labels.getOrDefault("severity", "UNKNOWN"),
            labels.getOrDefault("source", "prometheus"),
            labels,
            annotations,
            OffsetDateTime.now(),
            payload                       // 🔥 raw Grafana JSON 전체
        );

        alertHandlingService.handle(event);
        return ResponseEntity.ok().build();
    }
}
